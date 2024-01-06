package org.example;

import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Constants {

    private Constants() {

    }

    public static Queue<Participant> myBlockingQueue = new ArrayBlockingQueue<>(400);

    public static final SharedLinkedList ranking = new SharedLinkedList();

    public static Set<Long> disqualifiedCompetitors = new HashSet<>();

    public static final AtomicLong lastUpdateTime = new AtomicLong(0);

    public static final Map<Long, Integer> totalParticipantsPerCountry1 = Collections.synchronizedMap(new HashMap<>());

    public static final Map<Long, Integer> totalParticipantsPerCountry2 = Collections.synchronizedMap(new HashMap<>());

    public static final AtomicInteger numberOfFinishedConsumers = new AtomicInteger(0);

    public static final AtomicBoolean isReadingFinished = new AtomicBoolean(false);

    public static final Map<Long, Integer> currentCountryScores = Collections.synchronizedMap(new HashMap<>());

    public static final List<OutputStream> clientsOutputStreams = Collections.synchronizedList(new ArrayList<>());

    public static final AtomicBoolean finalInformationAreWritingToFile = new AtomicBoolean(false);

}
