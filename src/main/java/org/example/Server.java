package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
public class Server {

    private static int pReaders;

    private static int pWriters;

    private static int deltaT; // ms

    private final static AtomicInteger nr = new AtomicInteger(0);

    private static Comparator<Participant> getComparator() {
        return Comparator
                .comparing(Participant::getPoints)
                .thenComparing(Participant::getIdParticipant);
    }

    public static void main(String[] args) throws IOException {
        initialiseServer(args);
        ExecutorService executorReaders = Executors.newFixedThreadPool(pReaders);

        int serverPort = 60000;
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            ObjectInputStream inputStream;
            Object participants;
            WriterThread[] writerThreads = new WriterThread[pWriters];
//            QueueByMe myBlockingQueue = new QueueByMe();

            Set<Long> listOfIdThatAreEliminated = new HashSet<>();
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    // if the port is between 10000 and 19999 => client from country 1
                    // if the port is between 20000 and 29999 => client from country 2
                    // ...
//                    System.out.println("Client connected with: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    participants = inputStream.readObject();
                    if (participants instanceof List) {
                        Constants.participantsList = (List<Participant>) participants;
                        ReaderThread readerThread = new ReaderThread();
                        executorReaders.submit(readerThread);


                        for (int i = 0; i < pWriters; i++) {
                            writerThreads[i] = new WriterThread();
                            writerThreads[i].start();
                        }

                    } else if(participants instanceof String message){
                        if(message.equals("clasament")) {
//                        nr.incrementAndGet();
                            if(needToRecalculateScores()) {
                                Future<Map<Integer, Integer>> countryRating = calculateTotalScoresAsync();
                                List<Map.Entry<Integer, Integer>> list = new ArrayList<>(countryRating.get().entrySet());
                                list.sort(Map.Entry.<Integer, Integer>comparingByValue().reversed());
                                Constants.serializableSubList = new ArrayList<>();
                                for (var i : list) {
                                    Constants.serializableSubList.add(i.getKey() + ":" + i.getValue());
                                }
                            }
                            try (Socket cs = new Socket()) {
                                cs.connect(new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort()));
                                ObjectOutputStream outputStream = new ObjectOutputStream(cs.getOutputStream());
                                outputStream.writeObject(Constants.serializableSubList);
                                outputStream.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }else{
                            Constants.counter.incrementAndGet();
                        }
                    }
                    if (Constants.counter.get() == 4) {
                        Constants.ranking.mergeSort();
                        writeFinalRankingToFile(Constants.ranking, Constants.ParticipantRankingFile);
                        writeToFile(Constants.serializableSubList, Constants.CountryRankingFile);
                        executorReaders.shutdown();
                        for (int i = 0; i < pWriters; i++) {
                            try {
                                writerThreads[i].join();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    break;
                } catch (ClassNotFoundException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private static Future<Map<Integer, Integer>> calculateTotalScoresAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Map<Integer, Integer>> task = Server::calculateCountryRating;
        return executor.submit(task);
    }

    private static Map<Integer, Integer> calculateCountryRating() {
        Map<Integer, Integer> countryRating = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            Node<Tuple<Long, Long, Integer>> currentNode = Constants.ranking.getHead().next;
            while (currentNode != null) {
                if (currentNode.data.getCountry() == i) {
                    if (countryRating.get(i) == null) {
                        countryRating.put(i, currentNode.data.getScore());
                    } else {
                        int updatedPoints = countryRating.get(i) + currentNode.data.getScore();
                        countryRating.put(i, updatedPoints);
                    }
                }

                currentNode = currentNode.next;
            }
        }
        return countryRating;
    }

        private static void writeFinalRankingToFile(SharedLinkedList ranking, String fileName) {
            try {
                File file = new File(fileName);
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
    private static void writeToFile(List<String> myList, String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            for(var i : myList){
                fileWriter.write(i + "\n");
            }

            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Eroare la crearea fișierului " + fileName);
            e.printStackTrace();
        }
    }

    private static boolean needToRecalculateScores() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - Constants.lastUpdateTime.get()) > deltaT;
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