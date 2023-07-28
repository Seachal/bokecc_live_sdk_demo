package com.bokecc.livemodule.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Lru算法 map + 双向链表
 * Created by dds on 2021/3/31.
 */
public class LRUCache {

    private static class CacheNode {
        CacheNode prev;
        CacheNode next;
        int key;
        int value;

        public CacheNode(int key, int value) {
            this.key = key;
            this.value = value;
            this.prev = null;
            this.next = null;
        }
    }

    private final int capacity;
    private final Map<Integer, CacheNode> valNodeMap = new HashMap<>();
    private final CacheNode head = new CacheNode(-1, -1);
    private final CacheNode tail = new CacheNode(-1, -1);

    public LRUCache(int capacity) {
        this.capacity = capacity;
        tail.prev = head;
        head.next = tail;
    }

    public int get(int key) {
        if (!valNodeMap.containsKey(key)) {
            return -1;
        }
        CacheNode current = valNodeMap.get(key);
        current.prev.next = current.next;
        current.next.prev = current.prev;
        moveToTail(current);
        return valNodeMap.get(key).value;
    }

    public void put(int key, int value) {
        if (get(key) != -1) {
            valNodeMap.get(key).value = value;
            return;
        }

        if (valNodeMap.size() == capacity) {
            valNodeMap.remove(head.next.key);
            head.next = head.next.next;
            head.next.prev = head;
        }

        CacheNode insert = new CacheNode(key, value);
        valNodeMap.put(key, insert);
        moveToTail(insert);
    }

    private void moveToTail(CacheNode current) {
        current.prev = tail.prev;
        tail.prev = current;
        current.prev.next = current;
        current.next = tail;
    }
}
