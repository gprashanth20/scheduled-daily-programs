import java.util.*;

/**
 * Two linear-time substring search algorithms:
 *  - KMP: failure function (longest proper prefix that is also a suffix) avoids re-scanning text.
 *  - Rabin-Karp: rolling polynomial hash, verified on hash hits to rule out collisions.
 * Both return every match index. Time: O(n + m) expected.
 */
public class KmpAndRabinKarp {
    static int[] failure(String p) {
        int[] f = new int[p.length()];
        for (int i = 1, k = 0; i < p.length(); i++) {
            while (k > 0 && p.charAt(i) != p.charAt(k)) k = f[k - 1];
            if (p.charAt(i) == p.charAt(k)) k++;
            f[i] = k;
        }
        return f;
    }

    static List<Integer> kmp(String text, String p) {
        List<Integer> hits = new ArrayList<>();
        if (p.isEmpty()) return hits;
        int[] f = failure(p);
        for (int i = 0, k = 0; i < text.length(); i++) {
            while (k > 0 && text.charAt(i) != p.charAt(k)) k = f[k - 1];
            if (text.charAt(i) == p.charAt(k)) k++;
            if (k == p.length()) {
                hits.add(i - k + 1);
                k = f[k - 1];
            }
        }
        return hits;
    }

    static List<Integer> rabinKarp(String text, String p) {
        final long base = 257, mod = 1_000_000_007L;
        List<Integer> hits = new ArrayList<>();
        int n = text.length(), m = p.length();
        if (m == 0 || m > n) return hits;
        long high = 1;
        for (int i = 1; i < m; i++) high = high * base % mod;
        long hp = 0, ht = 0;
        for (int i = 0; i < m; i++) {
            hp = (hp * base + p.charAt(i)) % mod;
            ht = (ht * base + text.charAt(i)) % mod;
        }
        for (int i = 0; ; i++) {
            if (hp == ht && text.startsWith(p, i)) hits.add(i);
            if (i + m >= n) break;
            ht = ((ht - text.charAt(i) * high % mod + mod) * base + text.charAt(i + m)) % mod;
        }
        return hits;
    }

    static List<Integer> naive(String text, String p) {
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i + p.length() <= text.length(); i++) if (text.startsWith(p, i)) hits.add(i);
        return hits;
    }

    public static void main(String[] args) {
        String text = "ababcabcabababd";
        if (!kmp(text, "ababd").equals(List.of(10))) throw new AssertionError();
        if (!rabinKarp(text, "ababd").equals(List.of(10))) throw new AssertionError();
        if (!kmp("aaaaa", "aa").equals(List.of(0, 1, 2, 3))) throw new AssertionError();
        if (!rabinKarp("aaaaa", "aa").equals(List.of(0, 1, 2, 3))) throw new AssertionError();

        Random rnd = new Random(1);
        for (int t = 0; t < 5000; t++) {
            String s = randomString(rnd, 1 + rnd.nextInt(30));
            String p = randomString(rnd, 1 + rnd.nextInt(4));
            List<Integer> expected = naive(s, p);
            if (!kmp(s, p).equals(expected) || !rabinKarp(s, p).equals(expected)) {
                throw new AssertionError("text=" + s + " pattern=" + p);
            }
        }
        System.out.println("KMP and Rabin-Karp agree with naive search on 5000 random cases.");
    }

    static String randomString(Random r, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append((char) ('a' + r.nextInt(3)));
        return sb.toString();
    }
}
