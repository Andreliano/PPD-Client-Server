package org.example;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Constants {

    private Constants() {

    }

    public static MyBlockingQueue myBlockingQueue = new MyBlockingQueue();

    public static MyList<Participant> ranking = new MyList<>();

    public static Set<Long> disqualifiedCompetitors = new HashSet<>();

    public static AtomicInteger counter = new AtomicInteger(0);

    public static final AtomicLong lastUpdateTime = new AtomicLong(0);

}
