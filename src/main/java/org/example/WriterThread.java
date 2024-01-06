package org.example;


import java.util.Objects;
import java.util.Set;

public class WriterThread extends Thread {


    public void run() {
        Participant participant = null;
        try {
            participant = Constants.myBlockingQueue.poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (Constants.myBlockingQueue.size() > 0) {
            Node<Participant> currentNode = Constants.ranking.getHead().next;
            boolean gasit = false;
            while (currentNode != null) {
                if (Objects.equals(currentNode.data.getIdParticipant(), participant.getIdParticipant())) {
                    if (participant.getPoints() >= 0) {
                        currentNode.data.setPoints(currentNode.data.getPoints() + participant.getPoints());
                        gasit = true;
                    } else if (participant.getPoints() < 0) {
                        Constants.ranking.delete(currentNode.data);
                        Constants.disqualifiedCompetitors.add(participant.getIdParticipant());
                        gasit = true;
                    }
                    break;
                }
                currentNode = currentNode.next;
            }

            if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant()) && !gasit) {
                Constants.ranking.insert(participant);
            }
            try {
                participant = Constants.myBlockingQueue.poll();
                System.out.println(Constants.myBlockingQueue.size());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
