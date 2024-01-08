package org.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Constants {

    private Constants() {

    }


    public static List<Participant> participantsList = new ArrayList<>();
    public static MyBlockingQueue myBlockingQueue = new MyBlockingQueue();

    public static SharedLinkedList ranking = new SharedLinkedList();

    public static Set<Long> disqualifiedCompetitors = new HashSet<>();

    public static AtomicInteger counter = new AtomicInteger(0);
    public static ArrayList<String> serializableSubList = new ArrayList<>();

    public static final AtomicLong lastUpdateTime = new AtomicLong(0);

    public static String CountryRankingFile = "C:\\Users\\vladb\\Desktop\\Facultate\\an3\\programare paralela si distributiva\\java\\tema6\\src\\main\\resources\\out\\CountryRanking.txt";
    public static String ParticipantRankingFile = "C:\\Users\\vladb\\Desktop\\Facultate\\an3\\programare paralela si distributiva\\java\\tema6\\src\\main\\resources\\out\\ParticipantRanking.txt";

}