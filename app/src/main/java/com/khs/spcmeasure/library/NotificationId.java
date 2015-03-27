package com.khs.spcmeasure.library;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * handles unique notification id
 * Created by Mark on 26/03/2015.
 */
public class NotificationId {
    private final static AtomicInteger mId = new AtomicInteger(0);

    public static int getId() {
        return mId.incrementAndGet();
    }
}
