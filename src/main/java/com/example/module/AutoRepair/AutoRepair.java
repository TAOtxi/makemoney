package com.example.module.AutoRepair;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import java.util.Map;

import com.example.util.EventBus;
import com.example.util.MLogger;
import com.example.util.T;


public class AutoRepair {
    public static final String MODULE_NAME = "autorepair";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AutoRepairConfig config = AutoRepairConfig.load(AutoRepairConfig.class, MODULE_NAME);
    public static KeyMapping toggleKey;

    public static void init() {
        LOGGER.info("Initializing AutoRepair module...");
        registerCommand();
        // toggleKey = new KeyMapping(
        //     "key.autorepair.toggle",
        //     InputConstants.Type.KEYSYM,
        //     GLFW.GLFW_KEY_F10,
        //     "key.categories.makemoney"
        // );
    }

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (!config.enabled) return;
        // if (client.player == null || client.level == null) return;
        // if (toggleKey.consumeClick()) {
        //     config.enabled = !config.enabled;
        //     config.save();
        //     LOGGER.info("AutoRepair toggled to {}", config.enabled);
        //     Message.subTitleMsg(T.t(".message.toggled", config.enabled));
        // }
        // ticker.run();
        if (tickCounter % config.checkoffHandInterval == 0) {
            Replace.tryToReplace();
        }
        if (tickCounter % config.repairInterval == 0) {
            EnchantExp.tryToEnchantMending();
        }
    }

        private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .then(ClientCommandManager.literal("reload")
                    .executes(context -> {
                        context.getSource().sendFeedback(T.tl("message.reload", MODULE_NAME));
                        config = AutoRepairConfig.load(AutoRepairConfig.class, MODULE_NAME);
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
                        EventBus.post("openConfigGui", Map.of("title", T.t("autorepair.name")));
                        return 1;
                    }))
                );
        });
    }
}
