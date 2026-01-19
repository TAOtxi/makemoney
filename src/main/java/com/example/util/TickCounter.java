package com.example.util;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.function.IntConsumer;

public class TickCounter {
    private int tickCounter = 0;
    private List<Task> tasks = new ArrayList<>();

    public void run() {
        if (tasks.isEmpty())
            return;

        ++tickCounter;
        tasks.removeIf(task -> {
            if (tickCounter % task.delay != 0)
                return false;
            task.task.accept(tickCounter);

            if (task.lastRunCounts > 0) {
                --task.lastRunCounts;
            }

            return task.lastRunCounts == 0;
        });
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void updateTask(Task task) {
        tasks.removeIf(t -> t.id.equals(task.id));
        tasks.add(task);
    }

    public void removeTask(String id) {
        tasks.removeIf(task -> task.id.equals(id));
    }

    public void clearTasks() {
        tasks.clear();
    }

    public Task getTask(String id) {
        return tasks.stream()
                .filter(task -> task.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static class Task {
        public String id;
        public int delay = -1;
        public IntConsumer task;
        public int runCounts;
        public int lastRunCounts;

        public Task(String id, IntConsumer task, int delay, int runCounts) {
            this.id = id;
            this.delay = delay;
            this.task = task;
            this.runCounts = runCounts;
            this.lastRunCounts = runCounts;
        }

        public Task(IntConsumer task, int delay, int runCounts) {
            this(UUID.randomUUID().toString(), task, delay, runCounts);
        }

        public Task(IntConsumer task, int delay) {
            this(UUID.randomUUID().toString(), task, delay, -1);
        }

    }
}
