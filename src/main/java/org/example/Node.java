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

    public Node<T> getNext() {
        return this.next;
    }
    public Lock getLock() {
        return lock;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }

    public T getData() {
        return data;
    }
}