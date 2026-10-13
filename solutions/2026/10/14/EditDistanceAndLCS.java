import java.util.*;

/**
 * Two classic DP problems with solution reconstruction:
 *  - Levenshtein edit distance together with the actual edit script
 *  - Longest common subsequence (the sequence itself, not just its length)
 * Time: O(m*n)   Space: O(m*n)
 */
public class EditDistanceAndLCS {
    static int[][] editTable(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j - 1] + cost,
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        return dp;
    }

    static List<String> editScript(String a, String b) {
        int[][] dp = editTable(a, b);
        List<String> ops = new ArrayList<>();
        int i = a.length(), j = b.length();
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && a.charAt(i - 1) == b.charAt(j - 1) && dp[i][j] == dp[i - 1][j - 1]) {
                i--; j--;
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                ops.add("replace '" + a.charAt(i - 1) + "' with '" + b.charAt(j - 1) + "' at " + (i - 1));
                i--; j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
                ops.add("delete '" + a.charAt(i - 1) + "' at " + (i - 1));
                i--;
            } else {
                ops.add("insert '" + b.charAt(j - 1) + "' before " + i);
                j--;
            }
        }
        Collections.reverse(ops);
        return ops;
    }

    static String lcs(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1] + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);
        StringBuilder sb = new StringBuilder();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) { sb.append(a.charAt(i - 1)); i--; j--; }
            else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        return sb.reverse().toString();
    }

    public static void main(String[] args) {
        int d = editTable("kitten", "sitting")[6][7];
        List<String> script = editScript("kitten", "sitting");
        if (d != 3 || script.size() != 3) throw new AssertionError("distance=" + d + " script=" + script);
        System.out.println("kitten -> sitting needs " + d + " edits: " + script);

        String s = lcs("AGGTAB", "GXTXAYB");
        if (!s.equals("GTAB")) throw new AssertionError(s);
        System.out.println("LCS(AGGTAB, GXTXAYB) = " + s);

        if (editTable("", "abc")[0][3] != 3 || editTable("same", "same")[4][4] != 0) throw new AssertionError();
        System.out.println("Edge cases ok.");
    }
}
