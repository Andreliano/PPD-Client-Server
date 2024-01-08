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
            Node<Tuple<Long, Long, Integer>> currentNode = Constants.ranking.getHead().next;
            boolean gasit = false;
            while (currentNode != null) {
                if (Objects.equals(currentNode.data.getId(), participant.getIdParticipant())) {
                    if (participant.getPoints() >= 0) {
                        currentNode.data.setScore(currentNode.data.getScore() + participant.getPoints());
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
                Constants.ranking.insert(new Tuple<>(participant.getIdCountry(), participant.getIdParticipant(), participant.getPoints()));
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
