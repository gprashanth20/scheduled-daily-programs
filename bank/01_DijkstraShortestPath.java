import java.util.*;

/**
 * Dijkstra's single-source shortest path with a binary heap and path reconstruction.
 * Time: O((V + E) log V)   Space: O(V + E)
 */
public class DijkstraShortestPath {
    record Edge(int to, int weight) {}

    static final long INF = Long.MAX_VALUE / 4;

    static long[] shortestPaths(List<List<Edge>> graph, int source, int[] parent) {
        long[] dist = new long[graph.size()];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[source] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        pq.add(new long[]{0, source});
        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            int u = (int) cur[1];
            if (cur[0] > dist[u]) continue; // stale entry
            for (Edge e : graph.get(u)) {
                long nd = dist[u] + e.weight();
                if (nd < dist[e.to()]) {
                    dist[e.to()] = nd;
                    parent[e.to()] = u;
                    pq.add(new long[]{nd, e.to()});
                }
            }
        }
        return dist;
    }

    static List<Integer> path(int[] parent, int target) {
        LinkedList<Integer> p = new LinkedList<>();
        for (int v = target; v != -1; v = parent[v]) p.addFirst(v);
        return p;
    }

    static void addUndirected(List<List<Edge>> g, int a, int b, int w) {
        g.get(a).add(new Edge(b, w));
        g.get(b).add(new Edge(a, w));
    }

    public static void main(String[] args) {
        int n = 6;
        List<List<Edge>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        addUndirected(g, 0, 1, 7);
        addUndirected(g, 0, 2, 9);
        addUndirected(g, 0, 5, 14);
        addUndirected(g, 1, 2, 10);
        addUndirected(g, 1, 3, 15);
        addUndirected(g, 2, 3, 11);
        addUndirected(g, 2, 5, 2);
        addUndirected(g, 3, 4, 6);
        addUndirected(g, 4, 5, 9);

        int[] parent = new int[n];
        long[] dist = shortestPaths(g, 0, parent);
        if (!Arrays.equals(dist, new long[]{0, 7, 9, 20, 20, 11})) throw new AssertionError(Arrays.toString(dist));
        if (!path(parent, 4).equals(List.of(0, 2, 5, 4))) throw new AssertionError(path(parent, 4).toString());
        System.out.println("Distances from 0: " + Arrays.toString(dist));
        System.out.println("Path 0 -> 4: " + path(parent, 4));
    }
}
