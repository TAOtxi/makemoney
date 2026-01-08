package com.example.module;

import com.example.Makemoney;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;


import org.slf4j.Logger;

public class autoRepair {
    public static final String module = "autorepair";
    public static KeyMapping toggleKey;
    private static final Logger LOGGER = Makemoney.LOGGER;

    public static void registerTickEvents(Minecraft client) {
        LOGGER.info("[{}] registerTickEvents", module);
    }


}
