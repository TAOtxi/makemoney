package com.example.module.AutoReconnect;

import com.example.config.BaseConfig;


public class AtCConfig extends BaseConfig {
    public AtCConfig(String moduleName) {
        super(moduleName);
    }
    
    public boolean enabled = AtCConfig.getDefaultEnabled();
    public String worldName = AtCConfig.getDefaultWorldName();
    public String command = AtCConfig.getDefaultCommand();
    public int tryTimes = AtCConfig.getDefaultTryTimes();
    public int checkInterval = AtCConfig.getDefaultCheckInterval();


    public static boolean getDefaultEnabled() {
        return false;
    }

    public static String getDefaultWorldName() {
        return "minecraft:overworld";
    }

    public static int getDefaultTryTimes() {
        return 1;
    }

    public static String getDefaultCommand() {
        return "/stp survival2";
    }

    public static int getDefaultCheckInterval() {
        return 1200;
    }

}
