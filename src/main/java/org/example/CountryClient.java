package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CountryClient extends Thread {

    private static int deltaX; // sec

    private final String serverName = "localhost";

    private final int serverPort = 60000;

    private final int countryIndex;

    public CountryClient(int countryIndex) {
        this.countryIndex = countryIndex;
    }


    @Override
    public void run() {
        int clientPort = this.countryIndex * 10000;
        deltaX = 1;
        try {
            List<Participant> participants = getAllParticipantsByCountry();
            // Constants.totalParticipantsPerCountry1.put((long) countryIndex, participants.size());
            //System.out.println("Map1->" + countryIndex + ": " + Constants.totalParticipantsPerCountry1.get((long) countryIndex));
            List<Participant> subParticipants;
            int start;
            int end;
            int size = participants.size();
            Socket clientSocket;
            ObjectOutputStream outputStream;
            ArrayList<Participant> serializableSubList;

            clientSocket = new Socket();
            clientSocket.bind(new InetSocketAddress(clientPort));
            clientSocket.connect(new InetSocketAddress(serverName, serverPort));
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.writeObject(participants.size());
            outputStream.flush();
            Thread.sleep(deltaX * 1000L);
            outputStream.close();
            clientSocket.close();
            clientPort++;

            for (start = 0; start < size; start += 20) {
                clientSocket = new Socket();
                clientSocket.bind(new InetSocketAddress(clientPort));
                clientSocket.connect(new InetSocketAddress(serverName, serverPort));
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                end = Math.min(start + 20, size);
                subParticipants = participants.subList(start, end);
                serializableSubList = new ArrayList<>(subParticipants);
                outputStream.writeObject(serializableSubList);
                outputStream.flush();
                Thread.sleep(deltaX * 1000L);
                outputStream.close();
                clientSocket.close();
                clientPort++;
            }

            clientSocket = new Socket();
            clientSocket.bind(new InetSocketAddress(clientPort));
            clientSocket.connect(new InetSocketAddress(serverName, serverPort));
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.writeObject("REQUEST");
            outputStream.flush();
            Thread.sleep(deltaX * 1000L);
            outputStream.close();
            clientSocket.close();


        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Participant> getAllParticipantsByCountry() throws FileNotFoundException {
        int j;
        String filename;
        List<Participant> participants = new ArrayList<>();
        for (j = 1; j <= 10; j++) {
            filename = "src\\main\\resources\\RezultateC" + this.countryIndex + "_P" + j + ".txt";
            FileReader fileReader = new FileReader(filename);
            try (BufferedReader br
                         = new BufferedReader(fileReader)) {
                String line;
                Participant participant;
                while ((line = br.readLine()) != null) {
                    String[] participantInformation = line.split(" ");
                    participant = new Participant();
                    participant.setIdParticipant(Long.parseLong(participantInformation[0]));
                    participant.setIdCountry(Long.parseLong(participantInformation[1]));
                    participant.setPoints(Integer.parseInt(participantInformation[2]));
                    participants.add(participant);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return participants;
    }
}
