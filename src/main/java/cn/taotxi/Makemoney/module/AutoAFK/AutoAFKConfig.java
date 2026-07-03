package cn.taotxi.Makemoney.module.AutoAFK;


import java.util.List;


import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigInteger;
import cn.taotxi.Makemoney.config.type.ConfigString;


public class AutoAFKConfig extends ConfigManager {
    private static AutoAFKConfig instance = null;
    
    public static AutoAFKConfig getInstance() {
        if (instance == null) {
            instance = new AutoAFKConfig(AutoAFK.MODULE_NAME);
        }
        return instance;
    }

    public AutoAFKConfig(String moduleName) {
        super(moduleName);
    }

    public final ConfigBoolean  autoAttackEnabled   = new ConfigBoolean("autoAttackEnabled", false, "是否启用自动攻击", this);
    public final ConfigInteger  attackInterval      = new ConfigInteger("attackInterval", 11, "攻击间隔", this);
    public final ConfigBoolean  durabilityCheck     = new ConfigBoolean("durabilityCheck", true, "防止工具损坏", this);
    public final ConfigBoolean  showInfo            = new ConfigBoolean("showInfo", false, "是否显示攻击周期", this);
    public final ConfigBoolean  attackMode          = new ConfigBoolean("attackMode", false, "是否为白名单模式", this);
    public final ConfigArray<String> attackList     = new ConfigArray<>("attackList",  List.of("player"), "攻击列表", this, String.class);

    public final ConfigBoolean  tpsCheckEnabled     = new ConfigBoolean("tpsCheckEnabled", true, "是否启用tps检查", this);
    public final ConfigInteger  safetyTpsThreshold  = new ConfigInteger("tpsThreshold", 8, "tps安全阈值", this);
    public final ConfigInteger  greenTpsThreshold   = new ConfigInteger("greenTpsThreshold", 16, "tps绿色阈值", this);
    public final ConfigString   triggerCommand      = new ConfigString("triggerCommand", "/spawn", "低于阈值时触发的命令", this);
    public final ConfigString   greenTriggerCommand = new ConfigString("greenTriggerCommand", "/back", "绿色阈值时触发的命令", this);
}
