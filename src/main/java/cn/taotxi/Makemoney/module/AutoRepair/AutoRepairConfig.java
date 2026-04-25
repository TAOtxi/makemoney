package cn.taotxi.Makemoney.module.AutoRepair;

import cn.taotxi.Makemoney.config.BaseConfig;

public class AutoRepairConfig extends BaseConfig {
    public AutoRepairConfig(String moduleName) {
        super(moduleName);
        CONFIG_VERSION = getDefaultConfigVersion();
    }

    public boolean enabled = AutoRepairConfig.getDefaultEnabled();
    public boolean showMessage = AutoRepairConfig.getDefaultShowMessage();
    public boolean replaceEnabled = AutoRepairConfig.getDefaultReplaceEnabled();
    public int checkoffHandInterval = AutoRepairConfig.getDefaultCheckoffHandInterval();
    public boolean repairEnabled = AutoRepairConfig.getDefaultRepairEnabled();
    public int repairInterval = AutoRepairConfig.getDefaultRepairInterval();

    public static String getDefaultConfigVersion() {
        return "0.0.1";
    }

    public static boolean getDefaultEnabled() {
        return false;
    }

    public static boolean getDefaultShowMessage() {
        return true;
    }

    public static boolean getDefaultReplaceEnabled() {
        return true;
    }
    
    public static int getDefaultCheckoffHandInterval() {
        return 100;
    }
    
    public static boolean getDefaultRepairEnabled() {
        return true;
    }

    public static int getDefaultRepairInterval() {
        return 5;
    }
}