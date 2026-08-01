package cn.taotxi.Makemoney.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public ConfigString nineteenworldVersion       = new ConfigString("nineteenworldVersion", "", "NineteenWorld版本", this);
    public ConfigString autoRepairVersion         = new ConfigString("autorepairVersion", "", "AutoRepair版本", this);
    public ConfigString entityHighlightBoxVersion = new ConfigString("entityhighlightboxVersion", "", "EntityHighlightBox版本", this);
    public ConfigString messageCommandVersion     = new ConfigString("messagecommandVersion", "", "MessageCommand版本", this);


    private Map<String, String> getConfigVersionMap() {
        Map<String, String> map = new HashMap<>();
        map.put(makemoneyVersion.getKey(),          "");
        map.put(autoDropVersion.getKey(),           "1.2.9-beta.2+1.21.11");
        map.put(autofishVersion.getKey(),           "");
        map.put(nineteenworldVersion.getKey(),       "");
        map.put(autoRepairVersion.getKey(),         "");
        map.put(entityHighlightBoxVersion.getKey(), "");
        map.put(messageCommandVersion.getKey(),     "");

        return map;
    }

    public List<String> getConfigChangeNameList() {
        List<String> list = new ArrayList<>();
        if (!makemoneyVersion.getValue().equals(getConfigVersionMap().get(makemoneyVersion.getKey()))) {
            list.add(makemoneyVersion.getKey());
        }
        if (!autoDropVersion.getValue().equals(getConfigVersionMap().get(autoDropVersion.getKey()))) {
            list.add(autoDropVersion.getKey());
        }
        if (!autofishVersion.getValue().equals(getConfigVersionMap().get(autofishVersion.getKey()))) {
            list.add(autofishVersion.getKey());
        }
        if (!nineteenworldVersion.getValue().equals(getConfigVersionMap().get(nineteenworldVersion.getKey()))) {
            list.add(nineteenworldVersion.getKey());
        }
        if (!autoRepairVersion.getValue().equals(getConfigVersionMap().get(autoRepairVersion.getKey()))) {
            list.add(autoRepairVersion.getKey());
        }
        if (!entityHighlightBoxVersion.getValue().equals(getConfigVersionMap().get(entityHighlightBoxVersion.getKey()))) {
            list.add(entityHighlightBoxVersion.getKey());
        }
        if (!messageCommandVersion.getValue().equals(getConfigVersionMap().get(messageCommandVersion.getKey()))) {
            list.add(messageCommandVersion.getKey());
        }

        return list;
    }

    public void updateConfigVersionField() {
        Map<String, String> map = getConfigVersionMap();
        makemoneyVersion.setValue(map.get(makemoneyVersion.getKey()));
        autoDropVersion.setValue(map.get(autoDropVersion.getKey()));
        autofishVersion.setValue(map.get(autofishVersion.getKey()));
        nineteenworldVersion.setValue(map.get(nineteenworldVersion.getKey()));
        autoRepairVersion.setValue(map.get(autoRepairVersion.getKey()));
        entityHighlightBoxVersion.setValue(map.get(entityHighlightBoxVersion.getKey()));
        saveConfig();
    }

    public void resetConfig(List<String> configChangeNameList) {
        if (configChangeNameList.contains(MakemoneyConfig.getInstance().autoDropVersion.getKey())) {
            AutoDropConfig.getInstance().resetConfig();
        }
        // if (configChangeNameList.contains(MakemoneyConfig.getInstance().autoRepairVersion.getKey())) {
        //     AutoRepairConfig.getInstance().resetConfig();
        // }
        // if (configChangeNameList.contains(MakemoneyConfig.getInstance().entityHighlightBoxVersion.getKey())) {
        //     EntityHighlightBoxConfig.getInstance().resetConfig();
        // }
        // if (configChangeNameList.contains(MakemoneyConfig.getInstance().autoActionVersion.getKey())) {
        //     AutoActionConfig.getInstance().resetConfig();
        // }
        if (configChangeNameList.contains(MakemoneyConfig.getInstance().autofishVersion.getKey())) {
            AutoFishConfig.getInstance().resetConfig();
        }
        if (configChangeNameList.contains(MakemoneyConfig.getInstance().nineteenworldVersion.getKey())) {
            NineteenWorldConfig.getInstance().resetConfig();
        }
    }
}
