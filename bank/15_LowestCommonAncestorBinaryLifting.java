import java.util.*;

/**
 * Lowest common ancestor with binary lifting: up[j][v] is the 2^j-th ancestor of v.
 * Preprocessing O(n log n), each query O(log n). Also gives tree distance between nodes.
 */
public class LowestCommonAncestorBinaryLifting {
    private final int[][] up;
    private final int[] depth;
    private final int log;

    LowestCommonAncestorBinaryLifting(int n, int[][] edges, int root) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }
        int l = 1;
        while ((1 << l) < n) l++;
        log = l;
        up = new int[log][n];
        depth = new int[n];

        boolean[] seen = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(root);
        seen[root] = true;
        up[0][root] = root;
        while (!stack.isEmpty()) {
            int u = stack.pop();
            for (int v : adj.get(u)) {
                if (seen[v]) continue;
                seen[v] = true;
                depth[v] = depth[u] + 1;
                up[0][v] = u;
                stack.push(v);
            }
        }
        for (int j = 1; j < log; j++)
            for (int v = 0; v < n; v++) up[j][v] = up[j - 1][up[j - 1][v]];
    }

    int lca(int a, int b) {
        if (depth[a] < depth[b]) { int t = a; a = b; b = t; }
        int diff = depth[a] - depth[b];
        for (int j = 0; j < log; j++) if ((diff >> j & 1) == 1) a = up[j][a];
        if (a == b) return a;
        for (int j = log - 1; j >= 0; j--) {
            if (up[j][a] != up[j][b]) {
                a = up[j][a];
                b = up[j][b];
            }
        }
        return up[0][a];
    }

    int distance(int a, int b) {
        return depth[a] + depth[b] - 2 * depth[lca(a, b)];
    }

    public static void main(String[] args) {
        int[][] edges = {{0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 5}, {4, 6}, {4, 7}};
        LowestCommonAncestorBinaryLifting t = new LowestCommonAncestorBinaryLifting(8, edges, 0);
        if (t.lca(6, 7) != 4 || t.lca(3, 6) != 1 || t.lca(5, 6) != 0 || t.lca(7, 4) != 4) throw new AssertionError();
        if (t.distance(6, 5) != 5 || t.distance(3, 3) != 0) throw new AssertionError();
        System.out.println("LCA(6,7)=" + t.lca(6, 7) + ", LCA(3,6)=" + t.lca(3, 6) + ", dist(6,5)=" + t.distance(6, 5));
    }
}
