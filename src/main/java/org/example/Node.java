package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node<T> {
    T data;
    Node<T> next;
    Lock lock;

    public Node(T data) {
        this.data = data;
        this.next = null;
        this.lock = new ReentrantLock();
    }
}