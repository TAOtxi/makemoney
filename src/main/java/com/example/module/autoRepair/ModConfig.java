package com.example.module.AutoRepair;

import com.example.config.BaseConfig;

public class ModConfig extends BaseConfig {
    public ModConfig(String moduleName) {
        super(moduleName);
    }

    public boolean enabled = ModConfig.getDefaultEnabled();
    public boolean showMessage = ModConfig.getDefaultShowMessage();
    public boolean replaceEnabled = ModConfig.getDefaultReplaceEnabled();
    public int checkExpInterval = ModConfig.getDefaultCheckExpInterval();
    public double expCheckBound = ModConfig.getDefaultExpCheckBound();
    public boolean repairEnabled = ModConfig.getDefaultRepairEnabled();
    public int repairInterval = ModConfig.getDefaultRepairInterval();

    public static boolean getDefaultEnabled() {
        return false;
    }

    public static boolean getDefaultShowMessage() {
        return true;
    }

    public static boolean getDefaultReplaceEnabled() {
        return true;
    }
    
    public static int getDefaultCheckExpInterval() {
        return 5;
    }

    public static double getDefaultExpCheckBound() {
        return 2d;
    }
    
    public static boolean getDefaultRepairEnabled() {
        return true;
    }

    public static int getDefaultRepairInterval() {
        return 5;
    }
}