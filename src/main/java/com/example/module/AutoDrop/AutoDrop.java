package com.example.module.AutoDrop;

import com.example.Makemoney;
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
        if (tickCounter % config.checkInterval != 0) return;

        if (config.showAttentionMsg) {
            Message.actionBarMsg(T.tl("autodrop.message.attention"));
        }
        Dropper.tryToDropItems();
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autodrop")
                .then(ClientCommandManager.literal("reload")
                    .executes(context -> {
                        context.getSource().sendFeedback(T.l("Reload config..."));
                        config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
                        return 1;
                    })));
        });
    }
}
