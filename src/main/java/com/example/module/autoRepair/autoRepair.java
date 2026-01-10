package com.example.module.autoRepair;

import com.example.Makemoney;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;


import org.slf4j.Logger;

public class autoRepair {
    public static final String moduleName = "autorepair";
    public static KeyMapping toggleKey;
    private static final Logger LOGGER = Makemoney.LOGGER;
    public static final ModConfig

    public void init(Minecraft client) {
        toggleKey = 

        registerTickEvents(client);
    }

    public static void registerTickEvents(Minecraft client) {
        LOGGER.info("[{}] registerTickEvents", moduleName);


    }   


}
