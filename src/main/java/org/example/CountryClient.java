package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            List<Participant> subParticipants;
            int start;
            int end;
            int size = participants.size();
            Socket clientSocket;
            ObjectOutputStream outputStream;
            ArrayList<Participant> serializableSubList;
            int participantsPerProblem = size / 10;

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
                if(end >= participantsPerProblem) {
                    clientSocket = new Socket();
                    clientSocket.bind(new InetSocketAddress(clientPort));
                    clientSocket.connect(new InetSocketAddress(serverName, serverPort));
                    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    outputStream.writeObject("clasament");
                    outputStream.flush();
                    outputStream.close();
                    clientSocket.close();
                    clientPort++;
                    try (ServerSocket serverSocket = new ServerSocket(clientPort - 1)) {
                        try (Socket cc = serverSocket.accept()) {
                            ObjectInputStream inputStream = new ObjectInputStream(cc.getInputStream());
                            Object response = inputStream.readObject();
                            if (response instanceof List) {
                                List<String> list = (List<String>) response;
                                System.out.println("Country" + countryIndex + ":");
                                for (var i : list) {
                                    System.out.println(i);
                                }
                                System.out.println();
                            }
                        }
                    }
                }
            }
            clientSocket = new Socket();
            clientSocket.bind(new InetSocketAddress(clientPort));
            clientSocket.connect(new InetSocketAddress(serverName, serverPort));
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.writeObject("GATA");
            outputStream.flush();
            outputStream.close();
            clientSocket.close();

        } catch (IOException | InterruptedException | ClassNotFoundException e) {
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
