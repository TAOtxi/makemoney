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

    public final ConfigBoolean enabled     = new ConfigBoolean("enabled", true, "是否启用自动钓鱼", this);
    public final ConfigBoolean rotation    = new ConfigBoolean("rotation", false, "是否启用自动转向", this);
    public final ConfigBoolean randomDelay = new ConfigBoolean("randomDelay", false, "是否随机延迟", this);
    public final ConfigInteger throwDelay  = new ConfigInteger("throwDelay", 5, "抛竿延迟", this);
}
