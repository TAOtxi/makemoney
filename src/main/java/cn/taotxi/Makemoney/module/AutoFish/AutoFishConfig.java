package cn.taotxi.Makemoney.module.AutoFish;


import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigInteger;


public class AutoFishConfig extends ConfigManager {
    private static AutoFishConfig instance = null;
    
    public static AutoFishConfig getInstance() {
        if (instance == null) {
            instance = new AutoFishConfig(AutoFish.MODULE_NAME);
        }
        return instance;
    }

    public AutoFishConfig(String moduleName) {
        super(moduleName);
    }

    public ConfigBoolean enabled     = new ConfigBoolean("enabled", true, "是否启用自动钓鱼", this);
    public ConfigBoolean rotation    = new ConfigBoolean("rotation", true, "是否启用自动转向", this);
    public ConfigBoolean randomDelay = new ConfigBoolean("randomDelay", false, "是否随机延迟", this);
    public ConfigInteger throwDelay  = new ConfigInteger("throwDelay", 5, "抛竿延迟", this);
}
