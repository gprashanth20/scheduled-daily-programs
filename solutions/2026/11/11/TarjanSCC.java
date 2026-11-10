import java.util.*;

/**
 * Tarjan's strongly connected components in a single DFS using discovery times and low-links.
 * Time: O(V + E)
 */
public class TarjanSCC {
    private final List<List<Integer>> adj;
    private final int[] disc, low;
    private final boolean[] onStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final List<List<Integer>> components = new ArrayList<>();
    private int timer = 0;

    TarjanSCC(int n, int[][] edges) {
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);
        disc = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        Arrays.fill(disc, -1);
    }

    List<List<Integer>> run() {
        for (int v = 0; v < adj.size(); v++) if (disc[v] == -1) dfs(v);
        for (List<Integer> c : components) Collections.sort(c);
        components.sort(Comparator.comparingInt(c -> c.get(0)));
        return components;
    }

    private void dfs(int u) {
        disc[u] = low[u] = timer++;
        stack.push(u);
        onStack[u] = true;
        for (int v : adj.get(u)) {
            if (disc[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
        if (low[u] == disc[u]) { // u is the root of an SCC
            List<Integer> comp = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                comp.add(w);
            } while (w != u);
            components.add(comp);
        }
    }

    public static void main(String[] args) {
        int[][] edges = {{0, 1}, {1, 2}, {2, 0}, {1, 3}, {3, 4}, {4, 5}, {5, 3}, {5, 6}};
        List<List<Integer>> scc = new TarjanSCC(7, edges).run();
        List<List<Integer>> expected = List.of(List.of(0, 1, 2), List.of(3, 4, 5), List.of(6));
        if (!scc.equals(expected)) throw new AssertionError(scc.toString());
        System.out.println("SCCs: " + scc);

        List<List<Integer>> dag = new TarjanSCC(3, new int[][]{{0, 1}, {1, 2}}).run();
        if (dag.size() != 3) throw new AssertionError("a DAG has one SCC per node");
        System.out.println("A DAG yields singleton components: " + dag);
    }
}
