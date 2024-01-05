package org.example;

import java.util.List;

public class ReaderThread extends Thread {

    private final List<Participant> participants;

    public ReaderThread(List<Participant> participants) {
        this.participants = participants;
    }

    @Override
    public void run() {
        for (Participant participant : participants) {
            try {
                Constants.myBlockingQueue.add(participant);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
