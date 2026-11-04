import java.util.*;

/**
 * LFU cache with O(1) get/put: key->value, key->frequency, and a frequency->LinkedHashSet bucket
 * (insertion order inside a bucket gives LRU tie-breaking). minFreq tracks the eviction bucket.
 */
public class LFUCache {
    private final int capacity;
    private int minFreq;
    private final Map<Integer, Integer> values = new HashMap<>();
    private final Map<Integer, Integer> freqs = new HashMap<>();
    private final Map<Integer, LinkedHashSet<Integer>> buckets = new HashMap<>();

    public LFUCache(int capacity) {
        this.capacity = capacity;
    }

    public int get(int key) {
        if (!values.containsKey(key)) return -1;
        touch(key);
        return values.get(key);
    }

    public void put(int key, int value) {
        if (capacity == 0) return;
        if (values.containsKey(key)) {
            values.put(key, value);
            touch(key);
            return;
        }
        if (values.size() >= capacity) evict();
        values.put(key, value);
        freqs.put(key, 1);
        buckets.computeIfAbsent(1, f -> new LinkedHashSet<>()).add(key);
        minFreq = 1;
    }

    private void evict() {
        LinkedHashSet<Integer> set = buckets.get(minFreq);
        int victim = set.iterator().next();
        set.remove(victim);
        if (set.isEmpty()) buckets.remove(minFreq);
        values.remove(victim);
        freqs.remove(victim);
    }

    private void touch(int key) {
        int f = freqs.get(key);
        LinkedHashSet<Integer> set = buckets.get(f);
        set.remove(key);
        if (set.isEmpty()) {
            buckets.remove(f);
            if (minFreq == f) minFreq++;
        }
        freqs.put(key, f + 1);
        buckets.computeIfAbsent(f + 1, x -> new LinkedHashSet<>()).add(key);
    }

    private static void check(int actual, int expected) {
        if (actual != expected) throw new AssertionError("expected " + expected + " got " + actual);
    }

    public static void main(String[] args) {
        LFUCache c = new LFUCache(2);
        c.put(1, 1);
        c.put(2, 2);
        check(c.get(1), 1);
        c.put(3, 3);          // evicts key 2 (lowest frequency)
        check(c.get(2), -1);
        check(c.get(3), 3);
        c.put(4, 4);          // 1 and 3 tie on frequency; 1 is least recently used
        check(c.get(1), -1);
        check(c.get(3), 3);
        check(c.get(4), 4);
        System.out.println("LFU cache behaves correctly.");
    }
}
