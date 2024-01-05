package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
            MyBlockingQueue myBlockingQueue = new MyBlockingQueue();
//            QueueByMe myBlockingQueue = new QueueByMe();
            MyList<Participant> participantMyList = new MyList<>();
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
                        List<Participant> subParticipants = (List<Participant>) participants;
//                        System.out.println(subParticipants);
                        ReaderThread readerThread = new ReaderThread(subParticipants, myBlockingQueue);
                        executorReaders.submit(readerThread);


                        for (int i = 0; i < pWriters; i++) {
                            writerThreads[i] = new WriterThread(myBlockingQueue, participantMyList, listOfIdThatAreEliminated);
                            writerThreads[i].start();
                        }

                    } else {
                        String message = (String) participants;
                        if(message.equals("clasament")) {
//                        nr.incrementAndGet();
                            Map<Integer, Integer> countryRating = calculateCountryRating(participantMyList);
                            List<Map.Entry<Integer, Integer>> list = new ArrayList<>(countryRating.entrySet());
                            list.sort(Map.Entry.comparingByValue());
                            ArrayList<String> serializableSubList = new ArrayList<>();
                            for (var i : list) {
                                serializableSubList.add(i.getKey() + ":" + i.getValue());
                            }
                            try (Socket cs = new Socket()) {
                                System.out.println("adress: " + clientSocket.getInetAddress() + " port: " + clientSocket.getPort());
                                cs.connect(new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort()));
                                ObjectOutputStream outputStream = new ObjectOutputStream(cs.getOutputStream());
                                outputStream.writeObject(serializableSubList);
                                outputStream.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }else{
                            nr.incrementAndGet();
                        }
                    }
                    if (nr.get() == 4) {
                        Map<Integer, Integer> countryRating = calculateCountryRating(participantMyList);
                        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(countryRating.entrySet());
                        list.sort(Map.Entry.comparingByValue());
                        for (var participant : list) {
                            System.out.println(participant);
                        }
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
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static Map<Integer, Integer> calculateCountryRating(MyList<Participant> myList) {
        Map<Integer, Integer> countryRating = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            Node<Participant> currentNode = myList.getHead().next;
            while (currentNode != myList.getTail()) {
                if (currentNode.data.getIdCountry() == i) {
                    if (countryRating.get(i) == null) {
                        countryRating.put(i, currentNode.data.getPoints());
                    } else {
                        int updatedPoints = countryRating.get(i) + currentNode.data.getPoints();
                        countryRating.put(i, updatedPoints);
                    }
                }

                currentNode = currentNode.next;
            }
        }
        return countryRating;
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