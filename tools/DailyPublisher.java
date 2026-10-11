import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Picks the next {@value #PER_DAY} Java programs and places them under
 * solutions/yyyy/MM/dd/. Programs come from bank/ first; once the bank is used
 * up, they are generated through the Anthropic API (needs ANTHROPIC_API_KEY).
 *
 * Run with:  java tools/DailyPublisher.java [repoRoot]
 */
public class DailyPublisher {
    static final int PER_DAY = 2;
    static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    static final Pattern CLASS_NAME = Pattern.compile("public\\s+(?:final\\s+)?class\\s+(\\w+)");

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : ".").toAbsolutePath().normalize();
        LocalDate today = LocalDate.now(IST);
        Path outDir = root.resolve("solutions")
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .resolve(String.format("%02d", today.getDayOfMonth()));

        if (Files.isDirectory(outDir) && hasJava(outDir)) {
            System.out.println("Already published for " + today + ", nothing to do.");
            return;
        }
        Files.createDirectories(outDir);

        List<Path> bank = listBank(root.resolve("bank"));
        Path stateFile = root.resolve("state").resolve("next-index.txt");
        int next = Files.exists(stateFile) ? Integer.parseInt(Files.readString(stateFile).trim()) : 0;

        List<String> published = new ArrayList<>();
        for (int i = 0; i < PER_DAY; i++, next++) {
            String className;
            if (next < bank.size()) {
                Path src = bank.get(next);
                String code = Files.readString(src);
                className = classNameOf(code);
                Files.writeString(outDir.resolve(className + ".java"), code);
            } else {
                String code = generateWithClaude(knownNames(root));
                className = classNameOf(code);
                Path target = outDir.resolve(className + ".java");
                if (Files.exists(target)) {
                    throw new IllegalStateException("Generated class name clashes: " + className);
                }
                Files.writeString(target, code);
            }
            published.add(className);
            System.out.println("Published " + className);
        }

        Files.createDirectories(stateFile.getParent());
        Files.writeString(stateFile, String.valueOf(next));
        appendLog(root.resolve("LOG.md"), today, published);
    }

    static boolean hasJava(Path dir) throws IOException {
        try (Stream<Path> s = Files.list(dir)) {
            return s.anyMatch(p -> p.toString().endsWith(".java"));
        }
    }

    static List<Path> listBank(Path bankDir) throws IOException {
        if (!Files.isDirectory(bankDir)) return List.of();
        try (Stream<Path> s = Files.list(bankDir)) {
            return s.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
    }

    static String classNameOf(String code) {
        Matcher m = CLASS_NAME.matcher(code);
        if (!m.find()) throw new IllegalStateException("No public class found in program");
        return m.group(1);
    }

    static List<String> knownNames(Path root) throws IOException {
        List<String> names = new ArrayList<>();
        for (Path dir : List.of(root.resolve("bank"), root.resolve("solutions"))) {
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> s = Files.walk(dir)) {
                s.filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> names.add(p.getFileName().toString().replace(".java", "")));
            }
        }
        return names;
    }

    static void appendLog(Path log, LocalDate date, List<String> names) throws IOException {
        if (!Files.exists(log)) {
            Files.writeString(log, "# Daily log\n\n| Date | Program 1 | Program 2 |\n|---|---|---|\n");
        }
        String row = "| " + date + " | " + String.join(" | ", names) + " |\n";
        Files.writeString(log, row, StandardOpenOption.APPEND);
    }

    // ---- Claude fallback -------------------------------------------------

    static String generateWithClaude(List<String> avoid) throws Exception {
        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "The bank/ folder is used up. Add more files to bank/ or set the ANTHROPIC_API_KEY secret.");
        }
        String model = System.getenv("ANTHROPIC_MODEL");
        if (model == null || model.isBlank()) model = "claude-sonnet-5-5";
        String prompt = "Write ONE complete, self-contained Java 17 program (single file, no external libraries) "
                + "that solves a non-trivial data structures / algorithms problem (e.g. graphs, DP, trees, "
                + "strings, geometry, concurrency primitives, system-design data structures). "
                + "Requirements: a public class with a main method; a short comment on the problem, approach and "
                + "time/space complexity; a few self-checking tests in main that throw an exception on failure "
                + "and print results on success. Do NOT reuse any of these class names: "
                + String.join(", ", avoid) + ". "
                + "Reply with the raw Java source only, no markdown fences, no explanation.";
        String body = "{\"model\":\"" + model + "\",\"max_tokens\":4000,\"messages\":[{\"role\":\"user\",\"content\":"
                + jsonString(prompt) + "}]}";

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.anthropic.com/v1/messages"))
                .timeout(Duration.ofMinutes(3))
                .header("x-api-key", key)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("Anthropic API error " + resp.statusCode() + ": " + resp.body());
        }
        String code = extractText(resp.body()).strip();
        if (code.startsWith("```")) {
            code = code.replaceFirst("^```\\w*\\s*", "").replaceFirst("\\s*```$", "");
        }
        return code + "\n";
    }

    static String jsonString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    /** Pulls the first "text" string value out of the Messages API response. */
    static String extractText(String json) {
        int i = json.indexOf("\"text\":\"");
        if (i < 0) throw new IllegalStateException("Unexpected API response: " + json);
        i += 8;
        StringBuilder sb = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '"') break;
            if (c != '\\') { sb.append(c); continue; }
            char e = json.charAt(i++);
            switch (e) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> { sb.append((char) Integer.parseInt(json.substring(i, i + 4), 16)); i += 4; }
                default -> sb.append(e); // \" \\ \/
            }
        }
        return new String(sb.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
