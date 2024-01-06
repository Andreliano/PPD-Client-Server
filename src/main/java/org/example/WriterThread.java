package org.example;

import java.util.Iterator;

public class WriterThread extends Thread {

    @Override
    public void run() {
        Participant participant;
        while (Constants.myBlockingQueue.size() > 0 || !isReadingFinished()) {
            participant = Constants.myBlockingQueue.poll();

//            if (participant != null) {
//                synchronized (Constants.ranking) {
//                    boolean found = false;
//                    Iterator<Participant> iterator = Constants.ranking.iterator();
//                    while (iterator.hasNext()) {
//                        Participant par = iterator.next();
//                        if (participant.getIdParticipant().equals(par.getIdParticipant())) {
//                            if (participant.getPoints() == -1) {
//                                Constants.disqualifiedCompetitors.add(par.getIdParticipant());
//                                iterator.remove();
//                            } else {
//                                if (!Constants.disqualifiedCompetitors.contains(par.getIdParticipant())) {
//                                    par.setPoints(par.getPoints() + participant.getPoints());
//                                }
//                            }
//                            found = true;
//                            break;
//                        }
//                    }
//
//                    if (!found) {
//                        if (participant.getPoints() == -1) {
//                            Constants.disqualifiedCompetitors.add(participant.getIdParticipant());
//                        }
//                        if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
//                            Constants.ranking.add(participant);
//                        }
//                    }
//                }
//            }

            if (participant != null) {
                boolean found = false;
                Node<Tuple<Long, Long, Integer>> par = Constants.ranking.getHead().getNext();
                while (par != null && par.getNext() != null) {
                    if (participant.getIdParticipant().equals(par.data.getId())) {
                        if (participant.getPoints() == -1) {
                            Constants.disqualifiedCompetitors.add(par.data.getId());
                            Constants.ranking.delete(par.data);
                        } else {
                            if (!Constants.disqualifiedCompetitors.contains(par.data.getId())) {
                                par.data.setScore(par.data.getScore() + participant.getPoints());
                            }
                        }
                        found = true;
                        break;
                    }
                    par = par.next;
                }

                if (!found) {
                    if (participant.getPoints() == -1) {
                        Constants.disqualifiedCompetitors.add(participant.getIdParticipant());
                    }
                    if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
                        Constants.ranking.insert(new Tuple<>(participant.getIdCountry(), participant.getIdParticipant(), participant.getPoints()));
                    }
                }
            }
        }

        System.out.println("GATA WRITERUL");
        Constants.numberOfFinishedConsumers.getAndIncrement();

    }

    private boolean isReadingFinished() {
        if (bothMapsContainsAllCountriesIds()) {
            for (long countryId : Constants.totalParticipantsPerCountry1.keySet()) {
                if (!Constants.totalParticipantsPerCountry1.get(countryId).equals(Constants.totalParticipantsPerCountry2.get(countryId))) {
                    return false;
                }
            }
            Constants.isReadingFinished.set(true);
            return true;
        }

        return false;
    }

    private boolean bothMapsContainsAllCountriesIds() {
        long i;
        for (i = 1; i <= 5; i++) {
            if (!Constants.totalParticipantsPerCountry1.containsKey(i) || !Constants.totalParticipantsPerCountry2.containsKey(i)) {
                return false;
            }
        }
        return true;
    }

}
