import java.util.*;

/**
 * Consistent hashing with virtual nodes on a TreeMap ring. Adding a node only moves the keys
 * that now fall in its arcs (about 1/N of them) instead of reshuffling everything.
 */
public class ConsistentHashRing {
    private final TreeMap<Long, String> ring = new TreeMap<>();
    private final int virtualNodes;

    ConsistentHashRing(int virtualNodes) {
        this.virtualNodes = virtualNodes;
    }

    void addNode(String node) {
        for (int i = 0; i < virtualNodes; i++) ring.put(hash(node + "#" + i), node);
    }

    void removeNode(String node) {
        for (int i = 0; i < virtualNodes; i++) ring.remove(hash(node + "#" + i));
    }

    String lookup(String key) {
        Map.Entry<Long, String> e = ring.ceilingEntry(hash(key));
        return (e != null ? e : ring.firstEntry()).getValue();   // wrap around the ring
    }

    /** FNV-1a followed by a 64-bit finalizer so similar strings spread evenly. */
    static long hash(String s) {
        long h = 0xcbf29ce484222325L;
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x100000001b3L;
        }
        h ^= h >>> 33; h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33; h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    public static void main(String[] args) {
        ConsistentHashRing ring = new ConsistentHashRing(150);
        for (String n : List.of("cache-a", "cache-b", "cache-c", "cache-d")) ring.addNode(n);

        int keys = 20000;
        String[] before = new String[keys];
        for (int i = 0; i < keys; i++) before[i] = ring.lookup("user:" + i);

        ring.addNode("cache-e");
        int moved = 0;
        for (int i = 0; i < keys; i++) {
            String now = ring.lookup("user:" + i);
            if (!now.equals(before[i])) {
                moved++;
                if (!now.equals("cache-e")) throw new AssertionError("key moved to an old node");
            }
        }
        double fraction = moved / (double) keys;
        if (fraction > 0.40) throw new AssertionError("too many keys moved: " + fraction);
        System.out.printf("Adding a 5th node moved %.1f%% of keys (ideal 20%%)%n", fraction * 100);

        ring.removeNode("cache-e");
        for (int i = 0; i < keys; i++)
            if (!ring.lookup("user:" + i).equals(before[i])) throw new AssertionError("removal did not restore mapping");
        System.out.println("Removing the node restored every original mapping.");
    }
}
