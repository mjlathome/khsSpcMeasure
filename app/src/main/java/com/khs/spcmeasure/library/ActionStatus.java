package com.khs.spcmeasure.library;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mark on 29/03/2015.
 */
public enum ActionStatus {
    CREATE("C"), START("S"), OKAY("C"), FAIL("F"), DESTROY("D");

    // value to be stored
    private final String value;

    // declare value lookup map
    private static final Map<String, ActionStatus> map = new HashMap<String, ActionStatus>();

    // populate the value lookup map
    static {
        for (ActionStatus actStat : ActionStatus.values()) {
            map.put(actStat.value, actStat);
        }
    }

    // constructor
    private ActionStatus(String value) {
        this.value = value;
    }

    // convert key into enumurated type
    public static ActionStatus fromValue(String key) {
        ActionStatus actStat = null;

        if (map.containsKey(key)) {
            return map.get(key);
        }

        return actStat;
    }

    // extracts the enumerators value
    public String getValue() {
        return this.value;
    }
}
