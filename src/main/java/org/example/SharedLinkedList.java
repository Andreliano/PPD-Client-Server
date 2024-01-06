package org.example;

import java.io.Serializable;

public class SharedLinkedList implements Serializable {
    private final Node<Tuple<Long, Long, Integer>> head;

    public SharedLinkedList() {
        this.head = new Node<>(null);
    }

    public Node<Tuple<Long, Long, Integer>> getHead() {
        return head;
    }

    @Override
    public String toString() {
        Node<Tuple<Long, Long, Integer>> head = getHead().getNext();
        StringBuilder output = new StringBuilder();
        while (head.getNext() != null) {
            output.append(head);
            head = head.getNext();
        }
        return output.toString();
    }

    public void insert(Tuple<Long, Long, Integer> dataToInsert) {
        Node<Tuple<Long, Long, Integer>> newNode = new Node<>(dataToInsert);

        if (head.getNext() == null) {
            head.setNext(newNode);
            return;
        }

        Node<Tuple<Long, Long, Integer>> current;
        head.getLock().lock();
        try {
            current = head.getNext();
        } finally {
            head.getLock().unlock();
        }
        Node<Tuple<Long, Long, Integer>> previous = null;
        boolean updated = false;
        while (current != null) {
            current.getLock().lock();
            try {
                if (current.getData().getId().equals(dataToInsert.getId())) {
                    updated = true;
                    break;
                }
                previous = current;
                current = current.getNext();
            } finally {
                if (updated) {
                    current.getLock().unlock();
                } else {
                    previous.getLock().unlock();
                }
            }
        }

        if (updated) {
            current.getLock().lock();
            try {
                current.getData().setScore(current.getData().getScore() + dataToInsert.getScore());
            } finally {
                current.getLock().unlock();
            }
            return;
        }

        previous.getLock().lock();
        try {
            if (previous.getNext() == null) {
                previous.setNext(newNode);
            }
        } finally {
            previous.getLock().unlock();
        }
    }

    public void delete(Tuple<Long, Long, Integer> dataToDelete) {
        Node<Tuple<Long, Long, Integer>> current;
        head.getLock().lock();
        try {
            current = head.getNext();
        } finally {
            head.getLock().unlock();
        }
        Node<Tuple<Long, Long, Integer>> previous = head;

        while (current != null) {
            previous.getLock().lock();
            try {
                current.getLock().lock();
                try {
                    if ((current.getData().getId().equals(dataToDelete.getId()))) {
                        previous.setNext(current.getNext());
                        break;
                    }
                } finally {
                    current.getLock().unlock();
                }
            } finally {
                previous.getLock().unlock();
                previous = current;
                current = current.getNext();
            }
        }
    }

    private Node<Tuple<Long, Long, Integer>> getMiddle(Node<Tuple<Long, Long, Integer>> head) {
        if (head == null) {
            return null;
        }

        Node<Tuple<Long, Long, Integer>> slow = head, fast = head;

        while (fast.getNext() != null && fast.getNext().getNext() != null) {
            slow = slow.getNext();
            fast = fast.getNext().getNext();
        }

        return slow;
    }

    public void mergeSort() {
        this.head.setNext(sort(this.head.getNext()));
    }

    private Node<Tuple<Long, Long, Integer>> sort(Node<Tuple<Long, Long, Integer>> head) {
        if (head == null || head.getNext() == null) {
            return head;
        }

        Node<Tuple<Long, Long, Integer>> middle = getMiddle(head);
        Node<Tuple<Long, Long, Integer>> nextToMiddle = middle.getNext();

        middle.setNext(null);

        Node<Tuple<Long, Long, Integer>> left = sort(head);
        Node<Tuple<Long, Long, Integer>> right = sort(nextToMiddle);

        return merge(left, right);
    }

    private Node<Tuple<Long, Long, Integer>> merge(Node<Tuple<Long, Long, Integer>> left, Node<Tuple<Long, Long, Integer>> right) {
        Node<Tuple<Long, Long, Integer>> result = new Node<>(new Tuple<>(-1L, -1L, -1)); // Dummy node to simplify the code
        Node<Tuple<Long, Long, Integer>> current = result;

        while (left != null && right != null) {
            if (left.getData().getScore() > right.getData().getScore()) {
                current.setNext(left);
                left = left.getNext();
            } else if (left.getData().getScore() < right.getData().getScore()) {
                current.setNext(right);
                right = right.getNext();
            } else if (left.getData().getId().compareTo(right.getData().getId()) < 0) {
                current.setNext(left);
                left = left.getNext();
            } else {
                current.setNext(right);
                right = right.getNext();
            }
            current = current.getNext();
        }

        if (left != null) {
            current.setNext(left);
        }

        if (right != null) {
            current.setNext(right);
        }

        return result.getNext(); // Skip the dummy node
    }
}
