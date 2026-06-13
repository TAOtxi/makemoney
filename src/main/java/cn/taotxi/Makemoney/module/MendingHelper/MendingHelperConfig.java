package cn.taotxi.Makemoney.module.MendingHelper;


import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;


public class MendingHelperConfig extends ConfigManager {
    private static MendingHelperConfig instance = null;
    
    public static MendingHelperConfig getInstance() {
        if (instance == null) {
            instance = new MendingHelperConfig(MendingHelper.MODULE_NAME);
        }
        return instance;
    }

    public MendingHelperConfig(String moduleName) {
        super(moduleName);
    }

    public ConfigBoolean autoReplaceEnabled        = new ConfigBoolean("autoReplaceEnabled", false, "吸取到经验时将副手替换为带有经验修补且耐久未满的装备", this);
    public ConfigBoolean autoEnchantEnabled        = new ConfigBoolean("autoEnchantEnabled", false, "打开铁砧时自动为装备附魔经验修补", this);
    public ConfigBoolean autoDecomposeEnabled      = new ConfigBoolean("autoDecomposeEnabled", false, "站金块上时自动分解装备", this);
    public ConfigBoolean onlyDecomposeNoneDamage   = new ConfigBoolean("onlyDecomposeNoneDamage", false, "仅分解满耐久装备", this);
}
