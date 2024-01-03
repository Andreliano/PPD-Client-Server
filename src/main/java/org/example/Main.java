package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int totalCountries = 5;
        int i;
        CountryClient[] countryClients = new CountryClient[5];
        for (i = 0; i < totalCountries; i++) {
            countryClients[i] = new CountryClient(i + 1);
            countryClients[i].start();
        }

        for (i = 0; i < totalCountries; i++) {
            countryClients[i].join();
        }
    }

   private static void generateCountryFiles() {
        int i, j, k;
        int points, participantsNumber, idParticipant, initialValue = 1;
        String filename;
        Random random = new Random();
        for (i = 1; i <= 5; i++) {
            participantsNumber = random.nextInt(21) + 80;
            for (j = 1; j <= 10; j++) {
                filename = "src\\main\\resources\\RezultateC" + i + "_P" + j + ".txt";
                idParticipant = initialValue;
                for (k = 1; k <= participantsNumber; k++) {
                    points = random.nextInt(12) - 1; // [-1, 10]
                    writeCompetitorToFile(filename, idParticipant, i, points);
                    idParticipant++;
                }
            }
            initialValue += participantsNumber;
        }
    }

    private static void writeCompetitorToFile(String filename, int idParticipant, int idCountry, int point) {
        try {
            File file = new File(filename);
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            writer.write(idParticipant + " " + idCountry + " " + point);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}