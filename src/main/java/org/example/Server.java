package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
//                        participantMyList.sort(getComparator());
//                        participantMyList.printList();
                    }
                    else{
                        nr.incrementAndGet();
                        System.out.println(nr.get());
                    }
                    if(nr.get() == 4){
                        System.out.println("AM TERMINAT");
                        executorReaders.shutdown();
                        for (int i = 0; i < pWriters; i++) {
                            try {
                                writerThreads[i].join();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        participantMyList.printList();
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