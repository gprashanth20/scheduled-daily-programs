import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Picks the next {@value #PER_DAY} Java programs from bank/ and places them under
 * solutions/yyyy/MM/dd/. When the bank is used up it starts again from the first program.
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

        List<Path> bank = listBank(root.resolve("bank"));
        if (bank.isEmpty()) throw new IllegalStateException("bank/ has no .java files");

        Path stateFile = root.resolve("state").resolve("next-index.txt");
        int next = Files.exists(stateFile) ? Integer.parseInt(Files.readString(stateFile).trim()) : 0;

        Files.createDirectories(outDir);
        List<String> published = new ArrayList<>();
        for (int i = 0; i < PER_DAY; i++, next++) {
            Path src = bank.get(next % bank.size());   // wraps around after the last program
            String code = Files.readString(src);
            String className = classNameOf(code);
            Path target = outDir.resolve(className + ".java");
            if (Files.exists(target)) {
                throw new IllegalStateException("bank/ has fewer than " + PER_DAY + " distinct programs");
            }
            Files.writeString(target, code);
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

    static void appendLog(Path log, LocalDate date, List<String> names) throws IOException {
        if (!Files.exists(log)) {
            Files.writeString(log, "# Daily log\n\n| Date | Program 1 | Program 2 |\n|---|---|---|\n");
        }
        String row = "| " + date + " | " + String.join(" | ", names) + " |\n";
        Files.writeString(log, row, StandardOpenOption.APPEND);
    }
}
