import java.util.*;

/**
 * LRU cache with O(1) get/put: a HashMap for lookup plus a hand-built doubly linked list
 * (sentinel head/tail) ordering entries by recency.
 */
public class LRUCacheLinkedList {
    private static class Node {
        int key, value;
        Node prev, next;
        Node(int k, int v) { key = k; value = v; }
    }

    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node head = new Node(0, 0), tail = new Node(0, 0);

    LRUCacheLinkedList(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    int get(int key) {
        Node n = map.get(key);
        if (n == null) return -1;
        unlink(n);
        pushFront(n);
        return n.value;
    }

    void put(int key, int value) {
        Node n = map.get(key);
        if (n != null) {
            n.value = value;
            unlink(n);
            pushFront(n);
            return;
        }
        if (map.size() == capacity) {
            Node lru = tail.prev;
            unlink(lru);
            map.remove(lru.key);
        }
        n = new Node(key, value);
        map.put(key, n);
        pushFront(n);
    }

    private void unlink(Node n) {
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }

    private void pushFront(Node n) {
        n.next = head.next;
        n.prev = head;
        head.next.prev = n;
        head.next = n;
    }

    private static void check(int actual, int expected) {
        if (actual != expected) throw new AssertionError("expected " + expected + " got " + actual);
    }

    public static void main(String[] args) {
        LRUCacheLinkedList c = new LRUCacheLinkedList(2);
        c.put(1, 1);
        c.put(2, 2);
        check(c.get(1), 1);
        c.put(3, 3);   // evicts 2
        check(c.get(2), -1);
        c.put(4, 4);   // evicts 1
        check(c.get(1), -1);
        check(c.get(3), 3);
        check(c.get(4), 4);
        System.out.println("LRU cache behaves correctly.");
    }
}
