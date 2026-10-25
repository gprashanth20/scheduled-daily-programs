import java.util.*;

/**
 * Kruskal's minimum spanning tree using a Union-Find (disjoint set) with
 * path compression and union by rank. Time: O(E log E)
 */
public class KruskalMST {
    record Edge(int u, int v, int w) {}

    static class DisjointSet {
        final int[] parent, rank;

        DisjointSet(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            while (parent[x] != x) {
                parent[x] = parent[parent[x]]; // path halving
                x = parent[x];
            }
            return x;
        }

        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;
            if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            if (rank[ra] == rank[rb]) rank[ra]++;
            return true;
        }
    }

    /** Returns the chosen edges; fewer than n-1 edges means the graph is disconnected. */
    static List<Edge> mst(int n, List<Edge> edges) {
        List<Edge> sorted = new ArrayList<>(edges);
        sorted.sort(Comparator.comparingInt(Edge::w));
        DisjointSet ds = new DisjointSet(n);
        List<Edge> chosen = new ArrayList<>();
        for (Edge e : sorted) {
            if (ds.union(e.u(), e.v())) {
                chosen.add(e);
                if (chosen.size() == n - 1) break;
            }
        }
        return chosen;
    }

    public static void main(String[] args) {
        List<Edge> edges = List.of(
                new Edge(0, 1, 10), new Edge(0, 2, 6), new Edge(0, 3, 5),
                new Edge(1, 3, 15), new Edge(2, 3, 4));
        List<Edge> tree = mst(4, edges);
        int total = tree.stream().mapToInt(Edge::w).sum();
        if (total != 19 || tree.size() != 3) throw new AssertionError("total=" + total);
        System.out.println("MST edges: " + tree + " total weight " + total);

        List<Edge> split = mst(4, List.of(new Edge(0, 1, 1), new Edge(2, 3, 1)));
        if (split.size() == 3) throw new AssertionError("disconnected graph must not yield a spanning tree");
        System.out.println("Disconnected graph correctly yields a spanning forest of " + split.size() + " edges.");
    }
}
