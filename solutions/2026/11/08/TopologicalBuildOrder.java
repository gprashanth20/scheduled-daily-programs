import java.util.*;

/**
 * Kahn's algorithm for a dependency/build order. A min-heap makes the order the lexicographically
 * smallest valid one (deterministic builds); leftover nodes reveal a cycle.
 * Time: O((V + E) log V)
 */
public class TopologicalBuildOrder {
    /** Returns a valid order, or an empty list when the graph has a cycle. */
    static List<Integer> order(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        int[] indegree = new int[n];
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            indegree[e[1]]++;
        }
        PriorityQueue<Integer> ready = new PriorityQueue<>();
        for (int i = 0; i < n; i++) if (indegree[i] == 0) ready.add(i);

        List<Integer> result = new ArrayList<>();
        while (!ready.isEmpty()) {
            int u = ready.poll();
            result.add(u);
            for (int v : adj.get(u)) if (--indegree[v] == 0) ready.add(v);
        }
        return result.size() == n ? result : List.of();
    }

    static boolean isValid(List<Integer> order, int[][] edges) {
        int[] pos = new int[order.size()];
        for (int i = 0; i < order.size(); i++) pos[order.get(i)] = i;
        for (int[] e : edges) if (pos[e[0]] >= pos[e[1]]) return false;
        return true;
    }

    public static void main(String[] args) {
        int[][] dag = {{5, 2}, {5, 0}, {4, 0}, {4, 1}, {2, 3}, {3, 1}};
        List<Integer> o = order(6, dag);
        if (!o.equals(List.of(4, 5, 0, 2, 3, 1)) || !isValid(o, dag)) throw new AssertionError(o.toString());
        System.out.println("Build order: " + o);

        List<Integer> cyc = order(3, new int[][]{{0, 1}, {1, 2}, {2, 0}});
        if (!cyc.isEmpty()) throw new AssertionError("cycle should be detected");
        System.out.println("Cycle detected, no valid order.");
    }
}
