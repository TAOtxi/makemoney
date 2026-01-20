package com.example.module.AutoRepair;

import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.example.util.MLogger;


public class AutoRepair {
    public static final String MODULE_NAME = "autorepair";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static final AutoRepairConfig config = AutoRepairConfig.load(AutoRepairConfig.class, MODULE_NAME);
    public static KeyMapping toggleKey;

    public static void init() {
        LOGGER.info("Initializing AutoRepair module...");

        // toggleKey = new KeyMapping(
        //     "key.autorepair.toggle",
        //     InputConstants.Type.KEYSYM,
        //     GLFW.GLFW_KEY_F10,
        //     "key.categories.makemoney"
        // );

        registerTickEvents();
    }

    public static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!config.enabled) return;
            if (client.player == null || client.level == null) return;
            // if (toggleKey.consumeClick()) {
            //     config.enabled = !config.enabled;
            //     config.save();
            //     LOGGER.info("AutoRepair toggled to {}", config.enabled);
            //     Message.subTitleMsg(T.t(".message.toggled", config.enabled));
            // }

            Replace.tryToReplace();
            EnchantExp.tryToEnchantMending();
        });
    }
}
