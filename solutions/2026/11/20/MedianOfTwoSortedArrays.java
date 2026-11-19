import java.util.*;

/**
 * Median of two sorted arrays in O(log(min(m, n))) by binary searching the partition of the
 * smaller array so that the left halves hold exactly half of all elements.
 */
public class MedianOfTwoSortedArrays {
    static double median(int[] a, int[] b) {
        if (a.length > b.length) return median(b, a);
        int m = a.length, n = b.length;
        int lo = 0, hi = m;
        while (lo <= hi) {
            int i = (lo + hi) >>> 1;          // elements taken from a
            int j = (m + n + 1) / 2 - i;      // elements taken from b
            long aLeft = i == 0 ? Long.MIN_VALUE : a[i - 1];
            long aRight = i == m ? Long.MAX_VALUE : a[i];
            long bLeft = j == 0 ? Long.MIN_VALUE : b[j - 1];
            long bRight = j == n ? Long.MAX_VALUE : b[j];
            if (aLeft <= bRight && bLeft <= aRight) {
                long leftMax = Math.max(aLeft, bLeft);
                if ((m + n) % 2 == 1) return leftMax;
                long rightMin = Math.min(aRight, bRight);
                return (leftMax + rightMin) / 2.0;
            } else if (aLeft > bRight) {
                hi = i - 1;
            } else {
                lo = i + 1;
            }
        }
        throw new IllegalArgumentException("inputs must be sorted and not both empty");
    }

    static double brute(int[] a, int[] b) {
        int[] all = new int[a.length + b.length];
        System.arraycopy(a, 0, all, 0, a.length);
        System.arraycopy(b, 0, all, a.length, b.length);
        Arrays.sort(all);
        int k = all.length;
        return k % 2 == 1 ? all[k / 2] : ((long) all[k / 2 - 1] + all[k / 2]) / 2.0;
    }

    static int[] randomSorted(Random r, int len) {
        int[] x = new int[len];
        for (int i = 0; i < len; i++) x[i] = r.nextInt(2001) - 1000;
        Arrays.sort(x);
        return x;
    }

    public static void main(String[] args) {
        if (median(new int[]{1, 3}, new int[]{2}) != 2.0) throw new AssertionError();
        if (median(new int[]{1, 2}, new int[]{3, 4}) != 2.5) throw new AssertionError();
        if (median(new int[]{}, new int[]{7}) != 7.0) throw new AssertionError();

        Random rnd = new Random(7);
        for (int t = 0; t < 20000; t++) {
            int[] a = randomSorted(rnd, rnd.nextInt(8));
            int[] b = randomSorted(rnd, rnd.nextInt(8));
            if (a.length + b.length == 0) continue;
            if (median(a, b) != brute(a, b)) {
                throw new AssertionError(Arrays.toString(a) + " / " + Arrays.toString(b));
            }
        }
        System.out.println("Median matches brute force on 20000 random cases.");
    }
}
