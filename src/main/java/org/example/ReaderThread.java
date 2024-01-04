package org.example;

import java.util.List;

public class ReaderThread implements Runnable{

    private final List<Participant> participantList;
    private final MyBlockingQueue myBlockingQueue;

    public ReaderThread(List<Participant> participantList, MyBlockingQueue myBlockingQueue) {
        this.participantList = participantList;
        this.myBlockingQueue = myBlockingQueue;
    }

    @Override
    public void run() {
        for(var i : participantList){
            try {
                myBlockingQueue.add(i);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
