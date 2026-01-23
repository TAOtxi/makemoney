package com.example.module.AutoDrop;

import com.example.util.MLogger;
import com.example.util.TickCounter;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AutoDrop {
    public static final String MODULE_NAME = "autodrop";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static final AutoDropConfig config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
    public static int tickCounter = 0;

    public static void init() {
        registerTickEvents();
    }

    private static void registerTickEvents() {
        TickCounter ticker = new TickCounter();
        ticker.addTask(new TickCounter.Task(tick -> {
            LOGGER.info("ticking...");
        }, 40));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            // ticker.run();
            // if (++tickCounter < config.checkInterval) return;
            // tickCounter = 0;
            
            // // Dropper.setPlayerRotation(0, 0);
            // if (!config.enabled) return;
            // Dropper.tryToDropItems();
        });
    }
}
