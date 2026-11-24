import java.util.*;

/**
 * Counts inversions (pairs i<j with a[i] > a[j]) in O(n log n) using a Fenwick tree (BIT)
 * over compressed values. Verified against an O(n^2) brute force.
 */
public class InversionCountFenwick {
    static long countInversions(int[] a) {
        int[] sorted = Arrays.stream(a).distinct().sorted().toArray();
        int m = sorted.length;
        long[] bit = new long[m + 1];
        long inversions = 0;
        for (int i = a.length - 1; i >= 0; i--) {
            int rank = Arrays.binarySearch(sorted, a[i]) + 1;   // 1-based
            for (int x = rank - 1; x > 0; x -= x & -x) inversions += bit[x]; // strictly smaller to the right
            for (int x = rank; x <= m; x += x & -x) bit[x]++;
        }
        return inversions;
    }

    static long brute(int[] a) {
        long c = 0;
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length; j++) if (a[i] > a[j]) c++;
        return c;
    }

    public static void main(String[] args) {
        if (countInversions(new int[]{2, 4, 1, 3, 5}) != 3) throw new AssertionError();
        if (countInversions(new int[]{5, 4, 3, 2, 1}) != 10) throw new AssertionError();
        if (countInversions(new int[]{}) != 0) throw new AssertionError();

        Random rnd = new Random(11);
        for (int t = 0; t < 3000; t++) {
            int[] a = new int[rnd.nextInt(40)];
            for (int i = 0; i < a.length; i++) a[i] = rnd.nextInt(20) - 10;
            if (countInversions(a) != brute(a)) throw new AssertionError(Arrays.toString(a));
        }
        System.out.println("Fenwick inversion counts match brute force on 3000 random arrays.");
    }
}
