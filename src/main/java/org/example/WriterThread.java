package org.example;

public class WriterThread extends Thread {

    @Override
    public void run() {
        Participant participant;
        while (Constants.myBlockingQueue.size() > 0 || Constants.counter.get() < 5) {
            try {
                participant = Constants.myBlockingQueue.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (participant != null) {
                boolean found = false;
                Node<Participant> par = Constants.ranking.getHead().next;
                while (par != Constants.ranking.getTail()) {
                    if (par != null) {
                        par.lock.lock();
                        if (participant.getIdParticipant().equals(par.data.getIdParticipant())) {
                            if (participant.getPoints() == -1) {
                                Constants.disqualifiedCompetitors.add(par.data.getIdParticipant());
                                Constants.ranking.delete(par.data);
                            } else {
                                if (!Constants.disqualifiedCompetitors.contains(par.data.getIdParticipant())) {
                                    par.data.setPoints(par.data.getPoints() + participant.getPoints());
                                    //this.ranking.setPoints(participant);
                                }
                            }
                            found = true;
                            break;
                        }
                        par.lock.unlock();
                    }
                    if(par != null) {
                        par = par.next;
                    }
                }
                if (!found) {
                    if (participant.getPoints() == -1) {
                        Constants.disqualifiedCompetitors.add(participant.getIdParticipant());
                    }
                    if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
                        Constants.ranking.insert(participant);
                    }
                }
            }

        }

    }



}
