package org.example;

import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Node<T> {
    T data;
    Node<T> next;

    public Node(T data) {
        this.data = data;
        this.next = null;
    }
}

class MyList<T> {
    private final Node<T> head;
    private final Lock lock;

    public MyList() {
        head = new Node<>(null);
        lock = new ReentrantLock();
    }

    public Node<T> getHead() {
        return head;
    }

    public Lock getLock() {
        return lock;
    }

    public void insert(T data) {
        Node<T> newNode = new Node<>(data);
        lock.lock();
        try {
            newNode.next = head.next;
            head.next = newNode;
        } finally {
            lock.unlock();
        }
    }

    public void delete(T data) {
        lock.lock();
        try {
            Node<T> current = head;
            Node<T> next = current.next;

            while (next != null && !next.data.equals(data)) {
                current = next;
                next = next.next;
            }

            if (next != null) {
                current.next = next.next;
            }
        } finally {
            lock.unlock();
        }
    }

    public void printList() {
        lock.lock();
        try {
            Node<T> current = head.next;
            int nr = 0;
            while (current != null) {
                nr++;
                current = current.next;
            }
            System.out.println(nr);
        } finally {
            lock.unlock();
        }
    }

    public void sort(Comparator<T> comparator) {
        lock.lock();
        try {
            boolean swapped;
            Node<T> current;
            Node<T> next = null;

            do {
                swapped = false;
                current = head;

                while (current.next != next) {
                    Node<T> currentNext = current.next;
                    Node<T> currentNextNext = currentNext.next;

                    if (currentNextNext!=null && comparator.compare(currentNext.data, currentNextNext.data) > 0) {
                        // Swap the data of currentNext and currentNextNext nodes
                        T temp = currentNext.data;
                        currentNext.data = currentNextNext.data;
                        currentNextNext.data = temp;
                        swapped = true;
                    }

                    current = currentNext;
                }

                next = current;
            } while (swapped);
        } finally {
            lock.unlock();
        }
    }
}
