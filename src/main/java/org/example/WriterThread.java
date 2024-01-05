package org.example;

import java.util.Iterator;

public class WriterThread extends Thread {

    @Override
    public void run() {
        Participant participant;
        while (Constants.myBlockingQueue.size() > 0 || !isReadingFinished()) {
            try {
                participant = Constants.myBlockingQueue.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            synchronized (Constants.ranking) {
                boolean found = false;
                Iterator<Participant> iterator = Constants.ranking.iterator();
                while (iterator.hasNext()) {
                    Participant par = iterator.next();
                    if (participant.getIdParticipant().equals(par.getIdParticipant())) {
                        if (participant.getPoints() == -1) {
                            Constants.disqualifiedCompetitors.add(par.getIdParticipant());
                            iterator.remove();
                        } else {
                            if (!Constants.disqualifiedCompetitors.contains(par.getIdParticipant())) {
                                par.setPoints(par.getPoints() + participant.getPoints());
                            }
                        }
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (participant.getPoints() == -1) {
                        Constants.disqualifiedCompetitors.add(participant.getIdParticipant());
                    }
                    if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
                        Constants.ranking.add(participant);
                    }
                }
            }

//            if (participant != null) {
//                boolean found = false;
//                Node<Participant> par = Constants.ranking.getHead().next;
//                while (par != Constants.ranking.getTail()) {
//                    if (par != null) {
//                        par.lock.lock();
//                        if (participant.getIdParticipant().equals(par.data.getIdParticipant())) {
//                            if (participant.getPoints() == -1) {
//                                Constants.disqualifiedCompetitors.add(par.data.getIdParticipant());
//                                Constants.ranking.delete(par.data);
//                            } else {
//                                if (!Constants.disqualifiedCompetitors.contains(par.data.getIdParticipant())) {
//                                    par.data.setPoints(par.data.getPoints() + participant.getPoints());
//                                    //this.ranking.setPoints(participant);
//                                }
//                            }
//                            found = true;
//                            break;
//                        }
//                        par.lock.unlock();
//                    }
//                    if(par != null) {
//                        par = par.next;
//                    }
//                }
//                if (!found) {
//                    if (participant.getPoints() == -1) {
//                        Constants.disqualifiedCompetitors.add(participant.getIdParticipant());
//                    }
//                    if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
//                        Constants.ranking.insert(participant);
//                    }
//                }
//            }
        }

        System.out.println("GATA WRITERUL");
        Constants.numberOfFinishedConsumers.getAndIncrement();

    }

    private boolean isReadingFinished() {
        if (bothMapsContainsAllCountriesIds()) {
           for (long countryId : Constants.totalParticipantsPerCountry1.keySet()) {
               if(!Constants.totalParticipantsPerCountry1.get(countryId).equals(Constants.totalParticipantsPerCountry2.get(countryId))){
                   return false;
               }
           }
            return true;
        }

        return false;
    }

    private boolean bothMapsContainsAllCountriesIds() {
        long i;
        for (i = 1; i <= 5; i++) {
            if(!Constants.totalParticipantsPerCountry1.containsKey(i) || !Constants.totalParticipantsPerCountry2.containsKey(i)) {
                return false;
            }
        }
        return true;
    }

}
