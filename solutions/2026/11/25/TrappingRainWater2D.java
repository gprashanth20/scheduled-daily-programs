import java.util.*;

/**
 * Trapping rain water on a 2D height map. Process cells from the boundary inward with a
 * min-heap: the lowest wall seen so far bounds the water level of its unvisited neighbours.
 * Time: O(R*C log(R*C))
 */
public class TrappingRainWater2D {
    static int trap(int[][] h) {
        int rows = h.length, cols = h[0].length;
        if (rows < 3 || cols < 3) return 0;
        boolean[][] seen = new boolean[rows][cols];
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (r == 0 || c == 0 || r == rows - 1 || c == cols - 1) {
                    seen[r][c] = true;
                    pq.add(new int[]{h[r][c], r, c});
                }
        int water = 0;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            for (int[] d : dirs) {
                int nr = cur[1] + d[0], nc = cur[2] + d[1];
                if (nr < 0 || nc < 0 || nr >= rows || nc >= cols || seen[nr][nc]) continue;
                seen[nr][nc] = true;
                water += Math.max(0, cur[0] - h[nr][nc]);
                pq.add(new int[]{Math.max(cur[0], h[nr][nc]), nr, nc});
            }
        }
        return water;
    }

    public static void main(String[] args) {
        int[][] a = {{1, 4, 3, 1, 3, 2}, {3, 2, 1, 3, 2, 4}, {2, 3, 3, 2, 3, 1}};
        int[][] b = {{3, 3, 3, 3, 3}, {3, 2, 2, 2, 3}, {3, 2, 1, 2, 3}, {3, 2, 2, 2, 3}, {3, 3, 3, 3, 3}};
        if (trap(a) != 4) throw new AssertionError("a=" + trap(a));
        if (trap(b) != 10) throw new AssertionError("b=" + trap(b));
        System.out.println("Trapped water: " + trap(a) + " and " + trap(b));
    }
}
