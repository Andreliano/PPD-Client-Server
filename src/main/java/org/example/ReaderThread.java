package org.example;

import java.util.List;
import java.util.Queue;

public class ReaderThread implements Runnable {

    @Override
    public void run() {
        for (var participant : Constants.participantsList) {
            try {
                Constants.myBlockingQueue.add(participant);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
