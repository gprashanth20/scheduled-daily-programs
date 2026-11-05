import java.util.*;

/**
 * Segment tree with lazy propagation: range add + range sum, both O(log n).
 * Verified against a brute-force array on random operations.
 */
public class LazySegmentTree {
    private final int n;
    private final long[] sum;
    private final long[] lazy;

    LazySegmentTree(int[] a) {
        n = a.length;
        sum = new long[4 * n];
        lazy = new long[4 * n];
        build(1, 0, n - 1, a);
    }

    private void build(int node, int l, int r, int[] a) {
        if (l == r) { sum[node] = a[l]; return; }
        int m = (l + r) >>> 1;
        build(2 * node, l, m, a);
        build(2 * node + 1, m + 1, r, a);
        sum[node] = sum[2 * node] + sum[2 * node + 1];
    }

    private void apply(int node, int l, int r, long v) {
        sum[node] += v * (r - l + 1);
        lazy[node] += v;
    }

    private void push(int node, int l, int r) {
        if (lazy[node] == 0) return;
        int m = (l + r) >>> 1;
        apply(2 * node, l, m, lazy[node]);
        apply(2 * node + 1, m + 1, r, lazy[node]);
        lazy[node] = 0;
    }

    void add(int ql, int qr, long v) { add(1, 0, n - 1, ql, qr, v); }

    private void add(int node, int l, int r, int ql, int qr, long v) {
        if (qr < l || r < ql) return;
        if (ql <= l && r <= qr) { apply(node, l, r, v); return; }
        push(node, l, r);
        int m = (l + r) >>> 1;
        add(2 * node, l, m, ql, qr, v);
        add(2 * node + 1, m + 1, r, ql, qr, v);
        sum[node] = sum[2 * node] + sum[2 * node + 1];
    }

    long query(int ql, int qr) { return query(1, 0, n - 1, ql, qr); }

    private long query(int node, int l, int r, int ql, int qr) {
        if (qr < l || r < ql) return 0;
        if (ql <= l && r <= qr) return sum[node];
        push(node, l, r);
        int m = (l + r) >>> 1;
        return query(2 * node, l, m, ql, qr) + query(2 * node + 1, m + 1, r, ql, qr);
    }

    public static void main(String[] args) {
        Random rnd = new Random(42);
        int n = 200;
        int[] base = new int[n];
        for (int i = 0; i < n; i++) base[i] = rnd.nextInt(100) - 50;
        long[] brute = new long[n];
        for (int i = 0; i < n; i++) brute[i] = base[i];
        LazySegmentTree tree = new LazySegmentTree(base);

        for (int op = 0; op < 5000; op++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            int l = Math.min(a, b), r = Math.max(a, b);
            if (rnd.nextBoolean()) {
                long v = rnd.nextInt(21) - 10;
                tree.add(l, r, v);
                for (int i = l; i <= r; i++) brute[i] += v;
            } else {
                long expected = 0;
                for (int i = l; i <= r; i++) expected += brute[i];
                if (tree.query(l, r) != expected) throw new AssertionError("mismatch at op " + op);
            }
        }
        System.out.println("5000 random range add/sum operations match brute force.");
    }
}
