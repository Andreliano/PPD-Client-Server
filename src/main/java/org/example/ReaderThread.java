package org.example;

import java.util.List;
import java.util.Queue;

public class ReaderThread implements Runnable {

    private final List<Participant> participantList;
    private final MyBlockingQueue myBlockingQueue;

    public ReaderThread(List<Participant> participantList, MyBlockingQueue myBlockingQueue) {
        this.participantList = participantList;
        this.myBlockingQueue = myBlockingQueue;
    }

    @Override
    public void run() {
        for (var participant : participantList) {
            try {
                myBlockingQueue.add(participant);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
