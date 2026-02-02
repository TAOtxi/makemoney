package com.example.util;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.example.Makemoney;

import java.util.HashMap;
import java.util.HashSet;

public class EventBus {
    private static final Map<String, Consumer<Map<String, Object>>> eventFunc = new HashMap<>();
    private static final Map<String, Map<String, Object>> eventArgs = new HashMap<>();
    private static final Set<String> eventQueue = new HashSet<>();

    public static void register(String event, Consumer<Map<String, Object>> func) {
        Makemoney.LOGGER.info("Register event: " + event);
        eventFunc.put(event, func);
    }

    public static void once(String event, Consumer<Map<String, Object>> func) {
        eventFunc.put(event, func);
    }

    public static void post(String event, Map<String, Object> args) {
        Makemoney.LOGGER.info("Post event: " + event);
        eventQueue.add(event);
        eventArgs.put(event, args);
    }

    public static void post(String event) {
        post(event, null);
    }

    public static void remove(String event) {
        eventFunc.remove(event);
        eventArgs.remove(event);
        removeFromQueue(event);
    }

    public static void finish(String event) {
        eventArgs.remove(event);
        eventQueue.remove(event);
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
            Consumer<Map<String, Object>> func = eventFunc.get(event);
            Map<String, Object> args = eventArgs.get(event);
            finish(event);
            if (func != null) {
                func.accept(args);
            } else {
                remove(event);
            }
        }
    }
}
