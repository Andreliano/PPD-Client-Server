package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private static int pReaders;

    private static int pWriters;

    private static int deltaT; // ms

    private static boolean ok = false;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        initialiseServer(args);
        ExecutorService threadPoolReaders = Executors.newFixedThreadPool(pReaders);
        WriterThread[] writerThreads = new WriterThread[pWriters];

        int serverPort = 60000;
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            ObjectInputStream inputStream;
            Object participants;
            int port;

            for (int i = 0; i < pWriters; i++) {
                writerThreads[i] = new WriterThread();
                writerThreads[i].start();
            }

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    // if the port is between 10000 and 19999 => client from country 1
                    // if the port is between 20000 and 29999 => client from country 2
                    // ...

                    // System.out.println("Client connected with: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    port = clientSocket.getPort();
                    inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    participants = inputStream.readObject();
                    if (participants instanceof List) {
                        List<Participant> subParticipants = (List<Participant>) participants;
                        setCountryIdForParticipantsSubListByPort(subParticipants, port);
                        ReaderThread readerThread = new ReaderThread(subParticipants, port / 10000);
                        threadPoolReaders.submit(readerThread);
                    } else if (participants instanceof String) {
                        if ("REQUEST".equals(participants.toString())) {
//                            if (needToRecalculateScores()) {
//                                Future<Map<Long, Integer>> futureScores = calculateTotalScoresAsync();
//                                System.out.println(futureScores.get());
//                            }
                        }
                    } else if (participants instanceof Integer) {
                        Constants.totalParticipantsPerCountry1.put((long) (port / 10000), (Integer) participants);
                    }

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                if (Constants.numberOfFinishedConsumers.get() == pWriters) {

                    threadPoolReaders.shutdown();
                    try {
                        if (!threadPoolReaders.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                            threadPoolReaders.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        threadPoolReaders.shutdownNow();
                    }

                    for (int i = 0; i < pWriters; i++) {
                        writerThreads[i].join();
                    }

                    if (needToRecalculateScores()) {
                        Future<Map<Long, Integer>> futureScores = calculateTotalScoresAsync();
                        System.out.println(futureScores.get());
                        Future<List<Participant>> futureRanking = sortParticipantsAsync();
                        for(Participant participant : futureRanking.get()) {
                            System.out.println(participant);
                        }
                    }

//                    Constants.ranking.sort(Comparator.comparing(Participant::getPoints).reversed()
//                            .thenComparing(Participant::getIdParticipant));
//                    for(Participant participant : Constants.ranking) {
//                        System.out.println(participant);
//                    }

                }

            }

        }
    }

    private static void setCountryIdForParticipantsSubListByPort(List<Participant> subParticipants, int port) {
        for (Participant participant : subParticipants) {
            participant.setIdCountry((long) (port / 10000));
        }
    }

    private static Future<Map<Long, Integer>> calculateTotalScoresAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Map<Long, Integer>> task = Server::calculateTotalScores;
        return executor.submit(task);
    }

    private static Future<List<Participant>> sortParticipantsAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<Participant>> task = Server::sortParticipants;
        return executor.submit(task);
    }

    private static boolean needToRecalculateScores() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - Constants.lastUpdateTime.get()) > deltaT;
    }

    private static Map<Long, Integer> calculateTotalScores() {
        Map<Long, Integer> countryScores = new HashMap<>();

//        Node<Participant> current = Constants.ranking.getHead().next;
//
//        while (current != Constants.ranking.getTail()) {
//            Participant participant = current.data;
//            if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
//                long countryId = participant.getIdCountry();
//                int newScore = countryScores.getOrDefault(countryId, 0) + participant.getPoints();
//                countryScores.put(countryId, newScore);
//            }
//            current = current.next;
//        }

        for (Participant participant : Constants.ranking) {
            if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
                long countryId = participant.getIdCountry();
                int newScore = countryScores.getOrDefault(countryId, 0) + participant.getPoints();
                countryScores.put(countryId, newScore);
            }
        }


        Constants.lastUpdateTime.set(System.currentTimeMillis());

        return countryScores;
    }

    private static List<Participant> sortParticipants() {
        Constants.ranking.sort(Comparator.comparing(Participant::getPoints).reversed()
                .thenComparing(Participant::getIdParticipant));
        return Constants.ranking;
    }

    private static void initialiseServer(String[] args) {
        // In script: Server p_r p_w delta_t
        // pReaders = Integer.parseInt(args[0]);
        // pWriters = Integer.parseInt(args[1]);
        // deltaT = Integer.parseInt(args[2]);

        pReaders = 4;
        pWriters = 4;
        deltaT = 2;
    }

}