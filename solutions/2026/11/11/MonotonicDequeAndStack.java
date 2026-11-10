import java.util.*;

/**
 * Monotonic data structures:
 *  - Sliding window maximum via a decreasing deque of indices: O(n)
 *  - Largest rectangle in a histogram via an increasing stack: O(n)
 * Both are cross-checked against brute force.
 */
public class MonotonicDequeAndStack {
    static int[] slidingMax(int[] a, int k) {
        int n = a.length;
        int[] out = new int[n - k + 1];
        Deque<Integer> dq = new ArrayDeque<>(); // indices, values strictly decreasing
        for (int i = 0; i < n; i++) {
            while (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();
            while (!dq.isEmpty() && a[dq.peekLast()] <= a[i]) dq.pollLast();
            dq.addLast(i);
            if (i >= k - 1) out[i - k + 1] = a[dq.peekFirst()];
        }
        return out;
    }

    static long largestRectangle(int[] h) {
        Deque<Integer> stack = new ArrayDeque<>();
        long best = 0;
        for (int i = 0; i <= h.length; i++) {
            int cur = i == h.length ? 0 : h[i];
            while (!stack.isEmpty() && h[stack.peek()] >= cur) {
                long height = h[stack.pop()];
                int left = stack.isEmpty() ? -1 : stack.peek();
                best = Math.max(best, height * (i - left - 1));
            }
            stack.push(i);
        }
        return best;
    }

    static int[] bruteSliding(int[] a, int k) {
        int[] out = new int[a.length - k + 1];
        for (int i = 0; i < out.length; i++) {
            int m = Integer.MIN_VALUE;
            for (int j = i; j < i + k; j++) m = Math.max(m, a[j]);
            out[i] = m;
        }
        return out;
    }

    static long bruteRect(int[] h) {
        long best = 0;
        for (int i = 0; i < h.length; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = i; j < h.length; j++) {
                min = Math.min(min, h[j]);
                best = Math.max(best, (long) min * (j - i + 1));
            }
        }
        return best;
    }

    public static void main(String[] args) {
        int[] r = slidingMax(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3);
        if (!Arrays.equals(r, new int[]{3, 3, 5, 5, 6, 7})) throw new AssertionError(Arrays.toString(r));
        if (largestRectangle(new int[]{2, 1, 5, 6, 2, 3}) != 10) throw new AssertionError();

        Random rnd = new Random(3);
        for (int t = 0; t < 5000; t++) {
            int n = 1 + rnd.nextInt(15);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(41) - 20;
            int k = 1 + rnd.nextInt(n);
            if (!Arrays.equals(slidingMax(a, k), bruteSliding(a, k))) throw new AssertionError(Arrays.toString(a));
            int[] h = new int[n];
            for (int i = 0; i < n; i++) h[i] = rnd.nextInt(10);
            if (largestRectangle(h) != bruteRect(h)) throw new AssertionError(Arrays.toString(h));
        }
        System.out.println("Sliding max " + Arrays.toString(r) + "; both algorithms match brute force.");
    }
}
