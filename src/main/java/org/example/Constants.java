package org.example;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Constants {

    private Constants() {

    }

    public static MyBlockingQueue myBlockingQueue = new MyBlockingQueue();

    public static final List<Participant> ranking = new LinkedList<>();

    public static Set<Long> disqualifiedCompetitors = new HashSet<>();

    public static final AtomicLong lastUpdateTime = new AtomicLong(0);

    public static final Map<Long, Integer> totalParticipantsPerCountry1 = Collections.synchronizedMap(new HashMap<>());

    public static final Map<Long, Integer> totalParticipantsPerCountry2 = Collections.synchronizedMap(new HashMap<>());

    public static final AtomicInteger numberOfFinishedConsumers = new AtomicInteger(0);


}
