package cn.taotxi.Makemoney.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;

import com.mojang.brigadier.context.CommandContext;

import java.util.List;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
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
            for (int i = 0; i < timeTasks.size(); i++) {
                if (i >= timeTasks.size()) {
                    break;
                }
                timeTasks.get(i).tick(ticker);
            }
        });
        registerCommand();
    }

    public static void createTimeTask(String id, Runnable callback, int interval) {
        createTimeTask(id, callback, interval, false);
    }

    public static void createTimeTask(String id, Runnable callback, IntSupplier intervalSupplier) {
        createTimeTask(id, callback, intervalSupplier, false);
    }

    public static void createTimeTask(String id, Runnable callback, int interval, boolean runImmediately) {
        createTimeTask(id, callback, () -> interval, runImmediately);
       }

    public static void createTimeTask(String id, Runnable callback, IntSupplier intervalSupplier, boolean runImmediately) {
        if (hasTimeTask(id)) {
            throw new IllegalArgumentException("Time task with id " + id + " already exists");
        }
        TimeTask task = new TimeTask(id, callback, intervalSupplier, ticker + intervalSupplier.getAsInt());
        createTimeTask(task, runImmediately);
    }

    public static void createTimeTask(TimeTask task) {
        createTimeTask(task, false);
    }

    public static void createTimeTask(TimeTask task, boolean runImmediately) {
        if (hasTimeTask(task.getId())) {
            throw new IllegalArgumentException("Time task with id " + task.getId() + " already exists");
        }
        timeTasks.add(task);

        if (runImmediately) {
            task.run();
        }
    }

    public static void createOnceTimeTask(String id, Runnable callback, int delay) {
        if (delay <= 0) {
            callback.run();
            return;
        }
        Runnable callback2 = () -> {
            callback.run();
            removeTimeTask(id);
        };
        createTimeTask(id, callback2, delay);
    }

    public static boolean hasTimeTask(String id) {
        return timeTasks.stream().anyMatch(task -> task.is(id));
    }

    public static int getNextRunTick(String id) {
        for (TimeTask task : timeTasks) {
            if (task.is(id)) {
                return task.getNextRunTick();
            }
        }
        throw new IllegalArgumentException("Time task with id " + id + " not found");
    }

    public static Runnable getCallBack(String id) {
        for (TimeTask task : timeTasks) {
            if (task.is(id)) {
                return task.getCallback();
            }
        }
        throw new IllegalArgumentException("Time task with id " + id + " not found");
    }

    public static void resetNextRunTick(String id) {
        for (TimeTask task: timeTasks) {
            if (task.is(id)) {
                task.resetNextRunTick(ticker);
                return;
            }
        }
        throw new IllegalArgumentException("Time task with id " + id + " not found");
    }

    public static void updateTimeTask(String id, int interval) {
        for (TimeTask task : timeTasks) {
            if (task.is(id)) {
                task.updateTask(interval);
                return;
            }
        }
        throw new IllegalArgumentException("Time task with id " + id + " not found");
    }

    public static TimeTask removeTimeTask(String id) {
        for (TimeTask task : timeTasks) {
            if (task.is(id)) {
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

    public static int getTicker() {
        return ticker;
    }

    private static int listTimeTasks(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.l("§7========== §6Task List§7 =========="));
        for (int i = 0; i < timeTasks.size(); i++) {
            TimeTask task = timeTasks.get(i);
            context.getSource().sendFeedback(T.l("§7[§a" + task.getId() + "§7]"));
            context.getSource().sendFeedback(T.l(task.toString()));
            if (i < timeTasks.size() - 1) {
                context.getSource().sendFeedback(T.l());
            }
        }
        return 1;
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("taskUtil")
                .then(ClientCommands.literal("list")
                    .executes(TaskUtil::listTimeTasks)));
        });
    }
}

class TimeTask {
    private String id;
    private Runnable callback;
    private int nextRunTick;
    private IntSupplier intervalSupplier;

    public TimeTask(String id, Runnable callback, int interval, int nextRunTick) {
        this.id = id;
        this.callback = callback;
        this.nextRunTick = nextRunTick;
        this.intervalSupplier = () -> interval;
    }

    public TimeTask(String id, Runnable callback, IntSupplier intervalSupplier, int nextRunTick) {
        this.id = id;
        this.callback = callback;
        this.intervalSupplier = intervalSupplier;
        this.nextRunTick = nextRunTick;
    }

    public void run() {
        callback.run();
    }

    public String toString() {
        return "§7NextRunTick: §e" + nextRunTick + 
                "  §7Interval: §e" + intervalSupplier.getAsInt();
    }

    public void setIntervalSupplier(IntSupplier intervalSupplier) {
        this.intervalSupplier = intervalSupplier;
    }

    public void resetNextRunTick(int ticker) {
        nextRunTick = ticker + intervalSupplier.getAsInt();
    }

    public boolean tick(int currentTick) {
        if (currentTick >= nextRunTick) {
            nextRunTick = currentTick + intervalSupplier.getAsInt();
            callback.run();
            return true;
        }
        return false;
    }

    public void updateTask(int interval) {
        intervalSupplier = () -> interval;
    }

    public void updateTask(IntSupplier intervalSupplier) {
        this.intervalSupplier = intervalSupplier;
    }

    public int getNextRunTick() {
        return nextRunTick;
    }

    public boolean is(String id) {
        return this.id.equals(id);
    }

    public String getId() {
        return id;
    }

    public Runnable getCallback() {
        return callback;
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
