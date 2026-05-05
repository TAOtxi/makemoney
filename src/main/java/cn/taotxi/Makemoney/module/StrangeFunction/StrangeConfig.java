package cn.taotxi.Makemoney.module.StrangeFunction;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.ConfigManager;

public class StrangeConfig extends ConfigManager {
    private static StrangeConfig instance = null;

    public static StrangeConfig getInstance() {
        if (instance == null) {
            instance = new StrangeConfig("strangefunction");
        }
        return instance;
    }

    public StrangeConfig(String moduleName) {
        super(moduleName);
    }

    @Override
    protected JsonObject createDefaultConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("autoride_targetPlayer", "");
        config.addProperty("autoride_runInterval", 5);
        config.addProperty("autoride_minDistance", 6);

        config.addProperty("ignore_enabled", false);
        config.add("ignore_list", new JsonArray());
        
        config.addProperty("rightClickRide_enabled", true);
        
        return config;
    }
}
