package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {

    private static int pReaders;

    private static int pWriters;

    private static int deltaT; // ms

    public static void main(String[] args) throws IOException {
        initialiseServer(args);

        int serverPort = 60000;
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            ObjectInputStream inputStream;
            Object participants;
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    // if the port is between 10000 and 19999 => client from country 1
                    // if the port is between 20000 and 29999 => client from country 2
                    // ...
                    System.out.println("Client connected with: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    participants = inputStream.readObject();
                    if (participants instanceof List) {
                        List<Participant> subParticipants = (List<Participant>) participants;
                        System.out.println(subParticipants);
                    }
                }
                catch (IOException e) {
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