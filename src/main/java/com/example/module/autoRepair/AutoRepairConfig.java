package com.example.module.AutoRepair;

import com.example.config.BaseConfig;

public class AutoRepairConfig extends BaseConfig {
    public AutoRepairConfig(String moduleName) {
        super(moduleName);
    }

    public boolean enabled = AutoRepairConfig.getDefaultEnabled();
    public boolean showMessage = AutoRepairConfig.getDefaultShowMessage();
    public boolean replaceEnabled = AutoRepairConfig.getDefaultReplaceEnabled();
    public int checkExpInterval = AutoRepairConfig.getDefaultCheckExpInterval();
    public double expCheckBound = AutoRepairConfig.getDefaultExpCheckBound();
    public boolean repairEnabled = AutoRepairConfig.getDefaultRepairEnabled();
    public int repairInterval = AutoRepairConfig.getDefaultRepairInterval();

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