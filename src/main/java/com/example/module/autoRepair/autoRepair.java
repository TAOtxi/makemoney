package com.example.module.AutoRepair;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.example.util.T;
import com.example.Makemoney;
import com.example.util.Message;
import com.example.util.MLogger;


public class AutoRepair {
    public static final String MODULE_NAME = "autorepair";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static ModConfig config;
    public static KeyMapping toggleKey;

    public static void init() {
        LOGGER.info("Initializing AutoRepair module...");
        config = ModConfig.load(ModConfig.class, MODULE_NAME);

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
            // if (toggleKey.consumeClick()) {
            //     config.enabled = !config.enabled;
            //     config.save();
            //     LOGGER.info("AutoRepair toggled to {}", config.enabled);
            //     Message.subTitleMsg(T.t("makemoney.autorepair.message.toggled", config.enabled));
            // }

            Replace.tryToReplace(client);
            EnchantExp.tryToEnchantMending();
        });
    }
}
