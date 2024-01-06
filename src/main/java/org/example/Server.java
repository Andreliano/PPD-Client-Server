package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private static int pReaders;

    private static int pWriters;

    private static int deltaT; // ms

    private static boolean found1 = false;

    private static boolean found2 = false;

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
                Socket clientSocket = serverSocket.accept();
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
                    if ("CURRENT_SCORES".equals(participants.toString())) {
                        Map<Long, Integer> scores;
                        if (needToRecalculateScores()) {
                            Future<Map<Long, Integer>> futureScores = calculateTotalScoresAsync();
                            scores = futureScores.get();
                        } else {
                            scores = Constants.currentCountryScores;
                        }
                        ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        outputStream.writeObject(scores);
                        outputStream.flush();

                    } else if ("FINAL_SCORES".equals(participants.toString())) {
                        System.out.println("PORT: " + port);
                        Constants.clientsOutputStreams.add(clientSocket.getOutputStream());
                    }
                } else if (participants instanceof Integer) {
                    Constants.totalParticipantsPerCountry1.put((long) (port / 10000), (Integer) participants);
                }

                if (Constants.numberOfFinishedConsumers.get() == pWriters && !found1) {

                    found1 = true;
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

                    Future<Map<Long, Integer>> futureScores = calculateTotalScoresAsync();
                    Future<SharedLinkedList> futureRanking = sortParticipantsAsync();
                    System.out.println("FINAL_SCORES: " + futureScores.get());
                    writeCountriesScoresToFile(futureScores.get());
                    writeFinalRankingToFile(futureRanking.get());
                    Constants.finalInformationAreWritingToFile.set(true);
                }

                if (!found2 && Constants.clientsOutputStreams.size() == 5 && Constants.finalInformationAreWritingToFile.get()) {
                    found2 = true;
                    Map<Long, Integer> scores = readCountriesScoresFromFile();
                    List<Participant> ranking = readFinalRankingFromFile();
                    for (OutputStream stream : Constants.clientsOutputStreams) {
                        try {
                            ObjectOutputStream outputStream = new ObjectOutputStream(stream);
                            outputStream.writeObject(scores);
                            outputStream.flush();
                            outputStream.writeObject(ranking);
                            outputStream.flush();
                            outputStream.close();

                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }

            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    private static Future<SharedLinkedList> sortParticipantsAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<SharedLinkedList> task = Server::sortParticipants;
        return executor.submit(task);
    }

    private static boolean needToRecalculateScores() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - Constants.lastUpdateTime.get()) > deltaT;
    }

    private static Map<Long, Integer> calculateTotalScores() {
        Map<Long, Integer> countryScores = new HashMap<>();

//        for (Participant participant : Constants.ranking) {
//            if (!Constants.disqualifiedCompetitors.contains(participant.getIdParticipant())) {
//                long countryId = participant.getIdCountry();
//                int newScore = countryScores.getOrDefault(countryId, 0) + participant.getPoints();
//                countryScores.put(countryId, newScore);
//            }
//        }

        Node<Tuple<Long, Long, Integer>> head = Constants.ranking.getHead().getNext();
        while (head.getNext() != null) {
            if (!Constants.disqualifiedCompetitors.contains(head.data.getId())) {
                long countryId = head.data.getCountry();
                int newScore = countryScores.getOrDefault(countryId, 0) + head.data.getScore();
                countryScores.put(countryId, newScore);
            }
            head = head.getNext();
        }


        Constants.lastUpdateTime.set(System.currentTimeMillis());

        return countryScores;
    }

    private static SharedLinkedList sortParticipants() {
        Constants.ranking.mergeSort();
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

    private static void writeCountriesScoresToFile(Map<Long, Integer> scores) {
        try {
            File file = new File("src\\main\\resources\\CountriesScores.txt");
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            for (Map.Entry<Long, Integer> score : scores.entrySet()) {
                writer.write(score.getKey() + " " + score.getValue());
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFinalRankingToFile(SharedLinkedList ranking) {
        try {
            File file = new File("src\\main\\resources\\Ranking.txt");
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            Node<Tuple<Long, Long, Integer>> head = Constants.ranking.getHead().getNext();
            while (head.getNext() != null) {
                writer.write(head.data.getId() + " " + head.data.getScore() + " " + head.data.getCountry());
                writer.newLine();
                head = head.getNext();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<Long, Integer> readCountriesScoresFromFile() {
        String filename = "src\\main\\resources\\CountriesScores.txt";
        Map<Long, Integer> scores = new HashMap<>();
        try {
            FileReader fileReader = new FileReader(filename);
            try (BufferedReader br
                         = new BufferedReader(fileReader)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] countriesScores = line.split(" ");
                    scores.put(Long.parseLong(countriesScores[0]), Integer.parseInt(countriesScores[1]));
                }

                return scores;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<Participant> readFinalRankingFromFile() {
        String filename = "src\\main\\resources\\Ranking.txt";
        List<Participant> ranking = new LinkedList<>();
        try {
            FileReader fileReader = new FileReader(filename);
            try (BufferedReader br
                         = new BufferedReader(fileReader)) {
                Participant participant;
                String line;
                while ((line = br.readLine()) != null) {
                    String[] participantInformation = line.split(" ");
                    participant = new Participant();
                    participant.setIdParticipant(Long.parseLong(participantInformation[0]));
                    participant.setPoints(Integer.parseInt(participantInformation[1]));
                    participant.setIdCountry(Long.parseLong(participantInformation[2]));
                    ranking.add(participant);
                }

                return ranking;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}