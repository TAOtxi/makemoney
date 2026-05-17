package cn.taotxi.Makemoney.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TaskUtil {
    private static int ticker = 0;
    private static List<TimeTask> timeTasks = new ArrayList<>();
    private static Map<String, TickTask> tickTasks = new HashMap<>();

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (timeTasks.isEmpty()) {
                return;
            }
            ticker++;
            for (int i = timeTasks.size() - 1; i >= 0; i--) {
                timeTasks.get(i).tick(ticker);
            }
        });
    }

    public static void createTimeTask(String id, Runnable callback, int interval) {
        timeTasks.add(new TimeTask(id, callback, interval, ticker + interval, false));
    }

    public static void createTimeTask(String id, Runnable callback, int interval, boolean runImmediately) {
        if (hasTimeTask(id)) {
            throw new IllegalArgumentException("Time task with id " + id + " already exists");
        }
        TimeTask task = new TimeTask(id, callback, interval, ticker + interval, runImmediately);
        timeTasks.add(task);
    }

    public static void createOnceTimeTask(String id, Runnable callback, int interval) {
        Runnable callback2 = () -> {
            callback.run();
            removeTimeTask(id);
        };
        createTimeTask(id, callback2, interval);
    }

    public static boolean hasTimeTask(String id) {
        return timeTasks.stream().anyMatch(task -> task.getId().equals(id));
    }

    public static void updateTimeTask(String id, int interval) {
        for (TimeTask task : timeTasks) {
            if (task.getId().equals(id)) {
                task.updateTask(interval);
                return;
            }
        }
        throw new IllegalArgumentException("Time task with id " + id + " not found");
    }

    public static TimeTask removeTimeTask(String id) {
        for (TimeTask task : timeTasks) {
            if (task.getId().equals(id)) {
                timeTasks.remove(task);
                return task;
            }
        }
        return null;
    }

    public static void createTickTask(String id, Runnable callback, int interval) {
        if (hasTickTask(id)) {
            throw new IllegalArgumentException("Tick task with id " + id + " already exists");
        }
        tickTasks.put(id, new TickTask(interval, callback));
    }

    public static boolean hasTickTask(String id) {
        return tickTasks.containsKey(id);
    }

    public static void updateTickTask(String id, int interval) {
        if (!hasTickTask(id)) {
            throw new IllegalArgumentException("Tick task with id " + id + " not found");
        }
        tickTasks.get(id).updateTask(interval);
    }

    public static TickTask removeTickTask(String id) {
        return tickTasks.remove(id);
    }

    public static void tickTask(String id) {
        if (!hasTickTask(id)) {
            throw new IllegalArgumentException("Tick task with id " + id + " not found");
        }
        tickTasks.get(id).tick();
    }
}

class TimeTask {
    private String id;
    private Runnable callback;
    private int interval;
    private int nextRunTick;

    public TimeTask(String id, Runnable callback, int interval, int nextRunTick) {
        this(id, callback, interval, nextRunTick, false);
    }

    public TimeTask(String id, Runnable callback, int interval, int nextRunTick, boolean runImmediately) {
        this.id = id;
        this.callback = callback;
        this.interval = interval;
        this.nextRunTick = nextRunTick;

        if (runImmediately) {
            callback.run();
        }
    }

    public boolean tick(int currentTick) {
        if (currentTick >= nextRunTick) {
            nextRunTick = currentTick + interval;
            callback.run();
            return true;
        }
        return false;
    }

    public void updateTask(int interval) {
        this.interval = interval;
    }

    public int getNextRunTick() {
        return nextRunTick;
    }

    public String getId() {
        return id;
    }
}

class TickTask {
    private int ticker;
    private Runnable callback;
    private int interval;

    public TickTask(int interval, Runnable callback) {
        this.ticker = 0;
        this.callback = callback;
        this.interval = interval;
    }

    public void tick() {
        ticker++;
        if (ticker >= interval) {
            callback.run();
            ticker = 0;
        }
    }

    public void updateTask(int interval) {
        this.interval = interval;
    }
}
