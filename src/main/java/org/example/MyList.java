package org.example;

import java.util.Comparator;

public class MyList<T> {
    private final Node<T> head;
    private final Node<T> tail;

    public MyList() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.next = tail;
    }

    public Node<T> getHead() {
        return head;
    }

    public Node<T> getTail() {
        return tail;
    }

    public void insert(T data) {
        Node<T> newNode = new Node<>(data);

        head.lock.lock();
        try {
            Node<T> current = head;
            Node<T> next = current.next;

            next.lock.lock();
            try {
                current.next = newNode;
                newNode.next = next;
            } finally {
                next.lock.unlock();
            }
        } finally {
            head.lock.unlock();
        }
    }

    public void delete(T data) {
        head.lock.lock();
        try {
            Node<T> current = head;
            Node<T> next = current.next;

            next.lock.lock();
            try {
                while (next != tail && !next.data.equals(data)) {
                    current = next;
                    next = next.next;
                    next.lock.lock();
                    current.lock.unlock();
                }

                if (next != tail) {
                    current.next = next.next;
                }
            } finally {
                next.lock.unlock();
            }
        } finally {
            head.lock.unlock();
        }
    }

    public void printList() {
        Node<T> current = head.next;
        while (current != tail) {
            System.out.println(current.data.toString());
            current = current.next;
        }
    }

    public void sort(Comparator<T> comparator) {
        Node<T> current = head.next;
        Node<T> next;

        while (current != tail) {
            next = current.next;

            while (next != tail) {
                current.lock.lock();
                next.lock.lock();

                try {
                    if (comparator.compare(current.data, next.data) < 0) {
                        // Swap the data of current and next nodes
                        T temp = current.data;
                        current.data = next.data;
                        next.data = temp;
                    }
                } finally {
                    next.lock.unlock();
                    current.lock.unlock();
                }

                next = next.next;
            }

            current = current.next;
        }
    }
}
