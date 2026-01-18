package com.example.module.AutoReconnect;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;


public class AutoReconnect {
    public static final String MODULE_NAME = "autoReconnect";
    public static final AtCConfig config = new AtCConfig(MODULE_NAME);
    public static int tickCounter = 1000;


    public static void init() {
        registerTickEvents();
    }

    public static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (++tickCounter < config.checkInterval) return;
            tickCounter = 0;
            
            ClientLevel level = client.level;
            if (level == null) return;

            String levelName = level.dimension().location().toString();

            LocalPlayer player = client.player;
            if (player == null) return;

            if (!player.connection.isAcceptingMessages() || !levelName.equals(config.worldName)) return;
            if (config.tryTimes != -1 && config.tryTimes <= 0) return;
            config.tryTimes--;
            
            if (config.command.startsWith("/")) {
                config.command = config.command.substring(1);
            }
            player.connection.sendCommand(config.command);
        });
    }
}
