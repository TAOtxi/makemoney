package com.example.util;

import java.util.Map;
import java.util.Set;

import com.example.Makemoney;

import java.util.HashMap;
import java.util.HashSet;

public class EventBus {
    private static final Map<String, Runnable> eventMap = new HashMap<>();
    private static final Set<String> eventQueue = new HashSet<>();
    
    public static void register(String event, Runnable runnable) {
        register(event, runnable, true);
    }

    public static void register(String event, Runnable runnable, Boolean removeQueueAfterRun) {
        Makemoney.LOGGER.info("Register event: " + event);
        if (removeQueueAfterRun) {
            eventMap.put(event, () -> {
                runnable.run();
                removeFromQueue(event);
            });
        } else {
            eventMap.put(event, runnable);
        }
    }

    public static void once(String event, Runnable runnable) {
        eventMap.put(event, () -> {
            runnable.run();
            remove(event);
        });
    }

    public static void post(String event) {
        Makemoney.LOGGER.info("Post event: " + event);
        eventQueue.add(event);
    }

    public static void remove(String event) {
        eventMap.remove(event);
        removeFromQueue(event);
    }

    public static void removeFromQueue(String event) {
        eventQueue.remove(event);
    }

    public static void checkQueue() {
        if (eventQueue.isEmpty()) {
            return;
        }

        for (String event : eventQueue) {
            Makemoney.LOGGER.info("Check event: " + event);
            Runnable runnable = eventMap.get(event);
            if (runnable != null) {
                runnable.run();
            } else {
                remove(event);
            }
        }
    }
}
