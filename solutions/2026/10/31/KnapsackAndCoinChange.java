import java.util.*;

/**
 * Classic DP trio:
 *  - 0/1 knapsack with reconstruction of the chosen items
 *  - Minimum number of coins for an amount
 *  - Number of distinct coin combinations for an amount
 */
public class KnapsackAndCoinChange {
    static int knapsack(int[] wt, int[] val, int cap, List<Integer> chosen) {
        int n = wt.length;
        int[][] dp = new int[n + 1][cap + 1];
        for (int i = 1; i <= n; i++)
            for (int w = 0; w <= cap; w++) {
                dp[i][w] = dp[i - 1][w];
                if (wt[i - 1] <= w) dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - wt[i - 1]] + val[i - 1]);
            }
        int w = cap;
        for (int i = n; i >= 1; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                chosen.add(i - 1);
                w -= wt[i - 1];
            }
        }
        Collections.reverse(chosen);
        return dp[n][cap];
    }

    static int minCoins(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int a = 1; a <= amount; a++)
            for (int c : coins)
                if (c <= a && dp[a - c] != Integer.MAX_VALUE) dp[a] = Math.min(dp[a], dp[a - c] + 1);
        return dp[amount] == Integer.MAX_VALUE ? -1 : dp[amount];
    }

    static long countWays(int[] coins, int amount) {
        long[] dp = new long[amount + 1];
        dp[0] = 1;
        for (int c : coins)               // coin loop outside => combinations, not permutations
            for (int a = c; a <= amount; a++) dp[a] += dp[a - c];
        return dp[amount];
    }

    public static void main(String[] args) {
        int[] wt = {1, 3, 4, 5}, val = {1, 4, 5, 7};
        List<Integer> items = new ArrayList<>();
        int best = knapsack(wt, val, 7, items);
        int w = 0, v = 0;
        for (int i : items) { w += wt[i]; v += val[i]; }
        if (best != 9 || w > 7 || v != 9) throw new AssertionError("best=" + best + " items=" + items);
        System.out.println("Knapsack best value " + best + " using items " + items);

        if (minCoins(new int[]{1, 2, 5}, 11) != 3) throw new AssertionError();
        if (minCoins(new int[]{2}, 3) != -1) throw new AssertionError();
        if (countWays(new int[]{1, 2, 5}, 11) != 11) throw new AssertionError();
        System.out.println("Coin change: min coins for 11 = 3, combinations = 11");
    }
}
