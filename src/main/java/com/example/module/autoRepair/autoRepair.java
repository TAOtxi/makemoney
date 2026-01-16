package com.example.module.AutoRepair;


import net.minecraft.client.KeyMapping;
import com.example.util.T;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.example.Makemoney;
import com.example.config.ModConfig;
import com.example.util.Message;

import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class AutoRepair {
    public static final String MOD_ID = Makemoney.MOD_ID;
    public static final String MODULE_NAME = "autorepair";
    public static final Logger LOGGER = Makemoney.LOGGER;
    public static ModConfig config;
    public static KeyMapping toggleKey;
    private static int tickCounter = 0;

    public static void init() {
        LOGGER.info("Initializing AutoRepair module...");
        Map<String, Object> defaultConfig = Map.of(
            "enabled", true,
            "showMessage", true,
            "checkInterval", 5,
            "expCheckBound", 1.5d
        );
        config = new ModConfig(MODULE_NAME, defaultConfig);

        toggleKey = new KeyMapping(
            T.t("key.autorepair.toggle"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            T.t("key.categories.makemoney")
        );

        registerTickEvents();
    }

    public static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.isDown()) {
                config.set("enabled", !config.getBoolean("enabled"));
                config.save();
                LOGGER.info("[{}] AutoRepair toggled to {}", MODULE_NAME, config.getBoolean("enabled"));
                Message.subTitleMsg(T.t("makemoney.autorepair.message.toggled", config.getBoolean("enabled")));
            }

            if (!config.getBoolean("enabled")) return;
            if (++tickCounter < config.getInt("checkInterval")) return;
            tickCounter = 0;

            Mending.tryToRepair(client);
        });
    }
}
