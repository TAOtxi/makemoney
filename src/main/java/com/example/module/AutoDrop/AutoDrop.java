package com.example.module.AutoDrop;

import java.util.Timer;
import java.util.TimerTask;

import com.example.Makemoney;
import com.example.gui.ConfigScreen;
import com.example.util.EventBus;
import com.example.util.MLogger;
import com.example.util.Message;
import com.example.util.T;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class AutoDrop {
    public static final String MODULE_NAME = "autodrop";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AutoDropConfig config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
    public static int tickCounter = 0;

    public static void init() {
        registerCommand();
    }

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (!config.enabled || Makemoney.isOpenYaclScreen) return;
        if (tickCounter < config.launchDelay) return;
        if (config.showAttentionMsg) {
            Message.actionBarMsg(T.tl("autodrop.message.attention"));
        }

        if (tickCounter % config.checkInterval != 0) return;
        Dropper.tryToDropItems();
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autodrop")
                .then(ClientCommandManager.literal("reload")
                    .executes(context -> {
                        context.getSource().sendFeedback(T.tl("message.reload", MODULE_NAME));
                        config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
                        return 1;
                    }))
                .then(ClientCommandManager.literal("enable")
                    .executes(context -> {
                        config.enabled = true;
                        config.save();
                        context.getSource().sendFeedback(T.tl("message.enable", MODULE_NAME));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("disable")
                    .executes(context -> {
                        config.enabled = false;
                        config.save();
                        context.getSource().sendFeedback(T.tl("message.disable", MODULE_NAME));
                        return 1;
                    }))
                .then(ClientCommandManager.literal("config")
                    .executes(context -> {
                        EventBus.post("openConfigGui");
                        return 1;
                    }))
                );
        });
    }
}
