package cn.taotxi.Makemoney.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.config.type.ConfigString;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfig;
import cn.taotxi.Makemoney.module.AutoFish.AutoFishConfig;
import cn.taotxi.Makemoney.module.NineteenWorld.NineteenWorldConfig;


public class MakemoneyConfig extends ConfigManager {
    private static MakemoneyConfig instance = null;
    
    public static MakemoneyConfig getInstance() {
        if (instance == null) {
            instance = new MakemoneyConfig(Makemoney.MOD_ID);
        }
        return instance;
    }

    public MakemoneyConfig(String moduleName) {
        super(moduleName);
    }

    public ConfigString makemoneyVersion          = new ConfigString("makemoneyVersion", "", "Makemoney版本", this);
    public ConfigString autoDropVersion           = new ConfigString("autodropVersion", "", "AutoDrop版本", this);
    public ConfigString autofishVersion           = new ConfigString("autofishVersion", "", "AutoFish版本", this);
    public ConfigString nineteenworldVersion      = new ConfigString("nineteenworldVersion", "", "NineteenWorld版本", this);
    public ConfigString autoRepairVersion         = new ConfigString("autorepairVersion", "", "AutoRepair版本", this);
    public ConfigString messageCommandVersion     = new ConfigString("messagecommandVersion", "", "MessageCommand版本", this);

    public List<ConfigString> fields = List.of(
        makemoneyVersion,
        autoDropVersion,
        autofishVersion,
        nineteenworldVersion,
        autoRepairVersion,
        messageCommandVersion
    );

    private Map<String, String> getConfigVersionMap() {
        Map<String, String> map = new HashMap<>();
        map.put(makemoneyVersion.getKey(),          "");
        map.put(autoDropVersion.getKey(),           "1.2.9-beta.2+1.21.11");
        map.put(autofishVersion.getKey(),           "");
        map.put(nineteenworldVersion.getKey(),       "");
        map.put(autoRepairVersion.getKey(),         "");
        map.put(messageCommandVersion.getKey(),     "");

        return map;
    }

    public Set<String> getConfigChangeNameSet() {
        Set<String> set = new HashSet<>();
        Map<String, String> versionMap = getConfigVersionMap();

        fields.forEach((field) -> {
            if (!field.getValue().equals(versionMap.get(field.getKey()))) {
                set.add(field.getKey());
            }
        });

        return set;
    }

    public void updateConfigVersionField() {
        Map<String, String> versionMap = getConfigVersionMap();
        fields.forEach((field) -> {
            field.setValue(versionMap.get(field.getKey()));
        });
        saveConfig();
    }

    public void resetConfig(Set<String> configChangeNameList) {
        if (configChangeNameList.contains(MakemoneyConfig.getInstance().autoDropVersion.getKey())) {
            AutoDropConfig.getInstance().resetConfig();
        }
        if (configChangeNameList.contains(MakemoneyConfig.getInstance().autofishVersion.getKey())) {
            AutoFishConfig.getInstance().resetConfig();
        }
        if (configChangeNameList.contains(MakemoneyConfig.getInstance().nineteenworldVersion.getKey())) {
            NineteenWorldConfig.getInstance().resetConfig();
        }
    }
}
