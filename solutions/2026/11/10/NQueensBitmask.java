import java.util.*;

/**
 * N-Queens with bitmasks: three integers track attacked columns and both diagonals, so each
 * placement is O(1). Counts all solutions and prints the first board found.
 */
public class NQueensBitmask {
    static int count(int n) {
        return solve(n, 0, 0, 0, 0);
    }

    private static int solve(int n, int row, int cols, int d1, int d2) {
        if (row == n) return 1;
        int free = ~(cols | d1 | d2) & ((1 << n) - 1);
        int total = 0;
        while (free != 0) {
            int bit = free & -free;
            free -= bit;
            total += solve(n, row + 1, cols | bit, (d1 | bit) << 1, (d2 | bit) >> 1);
        }
        return total;
    }

    static int[] firstSolution(int n) {
        int[] placement = new int[n];
        return place(n, 0, 0, 0, 0, placement) ? placement : null;
    }

    private static boolean place(int n, int row, int cols, int d1, int d2, int[] out) {
        if (row == n) return true;
        int free = ~(cols | d1 | d2) & ((1 << n) - 1);
        while (free != 0) {
            int bit = free & -free;
            free -= bit;
            out[row] = Integer.numberOfTrailingZeros(bit);
            if (place(n, row + 1, cols | bit, (d1 | bit) << 1, (d2 | bit) >> 1, out)) return true;
        }
        return false;
    }

    static boolean valid(int[] q) {
        for (int i = 0; i < q.length; i++)
            for (int j = i + 1; j < q.length; j++)
                if (q[i] == q[j] || Math.abs(q[i] - q[j]) == j - i) return false;
        return true;
    }

    public static void main(String[] args) {
        int[] expected = {1, 0, 0, 2, 10, 4, 40, 92, 352, 724};
        for (int n = 1; n <= 10; n++) {
            if (count(n) != expected[n - 1]) throw new AssertionError("n=" + n);
        }
        int[] q = firstSolution(8);
        if (q == null || !valid(q)) throw new AssertionError();
        System.out.println("Solutions for n=1..10: " + Arrays.toString(expected));
        for (int r = 0; r < 8; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < 8; c++) row.append(q[r] == c ? "Q " : ". ");
            System.out.println(row.toString().stripTrailing());
        }
    }
}
