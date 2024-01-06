package org.example;

import java.util.List;

public class ReaderThread extends Thread {

    private final List<Participant> participants;

    private final long countryId;

    public ReaderThread(List<Participant> participants,
                        long countryId) {
        this.participants = participants;
        this.countryId = countryId;
    }

    @Override
    public void run() {
        for (Participant participant : participants) {
            Constants.myBlockingQueue.add(participant);
        }

        Constants.totalParticipantsPerCountry2.put(countryId, Constants.totalParticipantsPerCountry2.getOrDefault(countryId, 0) + participants.size());
//        System.out.println("Map2->" + countryId + ": " + Constants.totalParticipantsPerCountry2.get(countryId));
//        System.out.println("Map1->" + countryId + ": " + Constants.totalParticipantsPerCountry1.get(countryId));
    }

}
