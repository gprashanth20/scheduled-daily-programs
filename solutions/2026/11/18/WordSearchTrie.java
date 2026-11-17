import java.util.*;

/**
 * Word Search II: find all dictionary words on a grid (adjacent cells, no cell reused per word).
 * A trie prunes the DFS so every word is searched in a single pass over the board.
 * Time: O(R*C*4*3^(L-1))   Space: O(total word characters)
 */
public class WordSearchTrie {
    static class Node {
        Node[] next = new Node[26];
        String word;
    }

    static List<String> findWords(char[][] board, String[] words) {
        Node root = new Node();
        for (String w : words) {
            Node cur = root;
            for (char ch : w.toCharArray()) {
                int i = ch - 'a';
                if (cur.next[i] == null) cur.next[i] = new Node();
                cur = cur.next[i];
            }
            cur.word = w;
        }
        List<String> found = new ArrayList<>();
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < board[0].length; c++)
                dfs(board, r, c, root, found);
        return found;
    }

    private static void dfs(char[][] b, int r, int c, Node parent, List<String> out) {
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length) return;
        char ch = b[r][c];
        if (ch == '#') return;
        Node node = parent.next[ch - 'a'];
        if (node == null) return;
        if (node.word != null) {
            out.add(node.word);
            node.word = null; // avoid duplicates
        }
        b[r][c] = '#';
        dfs(b, r + 1, c, node, out);
        dfs(b, r - 1, c, node, out);
        dfs(b, r, c + 1, node, out);
        dfs(b, r, c - 1, node, out);
        b[r][c] = ch;
    }

    public static void main(String[] args) {
        char[][] board = {
                {'o', 'a', 'a', 'n'},
                {'e', 't', 'a', 'e'},
                {'i', 'h', 'k', 'r'},
                {'i', 'f', 'l', 'v'}
        };
        List<String> res = findWords(board, new String[]{"oath", "pea", "eat", "rain"});
        Collections.sort(res);
        if (!res.equals(List.of("eat", "oath"))) throw new AssertionError(res.toString());
        System.out.println("Found: " + res);
    }
}
