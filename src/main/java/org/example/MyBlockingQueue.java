package org.example;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue {
    private final ReentrantLock lock;

    private final Queue<Participant> participants;

    private final Condition notEmpty;

    private final Condition notFull;
    private static final int MAX_CAPACITY = 500;

    public MyBlockingQueue() {
        participants = new ArrayDeque<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    public void add(Participant participant) throws InterruptedException {
        lock.lock();
        try {
            while (participants.size() == MAX_CAPACITY) {
                notFull.await();
            }
            participants.add(participant);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public Participant poll() throws InterruptedException {
        lock.lock();
        try {
            while (participants.isEmpty()) {
                notEmpty.await();
            }
            Participant participant = participants.poll();
            notFull.signal();
            return participant;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return participants.size();
        } finally {
            lock.unlock();
        }
    }

}
