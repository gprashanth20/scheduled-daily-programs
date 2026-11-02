import java.util.*;

/**
 * Dinic's maximum flow: BFS builds a level graph, DFS pushes blocking flows using a
 * per-node edge pointer. Time: O(V^2 E) worst case, much faster in practice.
 */
public class DinicMaxFlow {
    private final int n;
    private final int[] head, next, to, level, iter;
    private final long[] cap;
    private int edgeCount = 0;

    DinicMaxFlow(int n, int maxEdges) {
        this.n = n;
        head = new int[n];
        level = new int[n];
        iter = new int[n];
        Arrays.fill(head, -1);
        next = new int[2 * maxEdges];
        to = new int[2 * maxEdges];
        cap = new long[2 * maxEdges];
    }

    void addEdge(int u, int v, long c) {
        to[edgeCount] = v; cap[edgeCount] = c; next[edgeCount] = head[u]; head[u] = edgeCount++;
        to[edgeCount] = u; cap[edgeCount] = 0; next[edgeCount] = head[v]; head[v] = edgeCount++;
    }

    private boolean bfs(int s, int t) {
        Arrays.fill(level, -1);
        Deque<Integer> q = new ArrayDeque<>();
        level[s] = 0;
        q.add(s);
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int e = head[u]; e != -1; e = next[e]) {
                if (cap[e] > 0 && level[to[e]] < 0) {
                    level[to[e]] = level[u] + 1;
                    q.add(to[e]);
                }
            }
        }
        return level[t] >= 0;
    }

    private long dfs(int u, int t, long pushed) {
        if (u == t) return pushed;
        for (; iter[u] != -1; iter[u] = next[iter[u]]) {
            int e = iter[u];
            int v = to[e];
            if (cap[e] > 0 && level[v] == level[u] + 1) {
                long d = dfs(v, t, Math.min(pushed, cap[e]));
                if (d > 0) {
                    cap[e] -= d;
                    cap[e ^ 1] += d;
                    return d;
                }
            }
        }
        return 0;
    }

    long maxFlow(int s, int t) {
        long flow = 0;
        while (bfs(s, t)) {
            System.arraycopy(head, 0, iter, 0, n);
            long f;
            while ((f = dfs(s, t, Long.MAX_VALUE)) > 0) flow += f;
        }
        return flow;
    }

    public static void main(String[] args) {
        // Classic CLRS network: source 0, sink 5, max flow 23.
        DinicMaxFlow g = new DinicMaxFlow(6, 10);
        g.addEdge(0, 1, 16);
        g.addEdge(0, 2, 13);
        g.addEdge(1, 2, 10);
        g.addEdge(2, 1, 4);
        g.addEdge(1, 3, 12);
        g.addEdge(3, 2, 9);
        g.addEdge(2, 4, 14);
        g.addEdge(4, 3, 7);
        g.addEdge(3, 5, 20);
        g.addEdge(4, 5, 4);
        long flow = g.maxFlow(0, 5);
        if (flow != 23) throw new AssertionError("flow=" + flow);
        System.out.println("Max flow = " + flow);

        DinicMaxFlow disconnected = new DinicMaxFlow(3, 1);
        disconnected.addEdge(0, 1, 5);
        if (disconnected.maxFlow(0, 2) != 0) throw new AssertionError();
        System.out.println("Disconnected sink gets zero flow.");
    }
}
