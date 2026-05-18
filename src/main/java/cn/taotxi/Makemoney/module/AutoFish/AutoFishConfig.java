package cn.taotxi.Makemoney.module.AutoFish;

import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.ConfigManager;


public class AutoFishConfig extends ConfigManager {
    private static AutoFishConfig instance = null;
    
    public static AutoFishConfig getInstance() {
        if (instance == null) {
            instance = new AutoFishConfig("autofish");
        }
        return instance;
    }

    public AutoFishConfig(String moduleName) {
        super(moduleName);
    }

    @Override
    protected JsonObject createDefaultConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("enabled", true);
        config.addProperty("rotation", true);
        config.addProperty("randomDelay", false);
        config.addProperty("throwDelay", 5);
        
        return config;
    }
}
