package org.example;

import java.util.Objects;
import java.util.Set;

public class WriterThread extends Thread {

    private final MyBlockingQueue queue;
    private final MyList<Participant> resultList;
    private final Set<Long> listOfIdThatAreEliminated;

    public WriterThread(MyBlockingQueue queue, MyList<Participant> resultList, Set<Long> listOfIdThatAreEliminated) {
        this.queue = queue;
        this.resultList = resultList;
        this.listOfIdThatAreEliminated = listOfIdThatAreEliminated;
    }

    public void run() {
        Participant participant = null;
        try {
            participant = queue.poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (queue.size() > 0) {
            Node<Participant> currentNode = resultList.getHead().next;
            boolean gasit = false;
            while (currentNode != resultList.getTail()) {
                if (Objects.equals(currentNode.data.getIdParticipant(), participant.getIdParticipant())) {
                    currentNode.lock.lock();
                    try {
                        if (participant.getPoints() > 0) {
                            currentNode.data.setPoints(currentNode.data.getPoints() + participant.getPoints());
                            gasit = true;
                        } else if (participant.getPoints() < 0) {
                            resultList.delete(currentNode.data);
                            listOfIdThatAreEliminated.add(participant.getIdParticipant());
                            gasit = true;
                        }
                    } finally {
                        currentNode.lock.unlock();
                    }
                    break;
                }
                currentNode = currentNode.next;
            }

            if (!listOfIdThatAreEliminated.contains(participant.getIdParticipant()) && !gasit) {
                resultList.insert(participant);
            }
            try {
                participant = queue.poll();
//                System.out.println(participant);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
