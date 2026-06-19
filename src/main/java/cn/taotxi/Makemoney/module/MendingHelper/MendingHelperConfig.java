package cn.taotxi.Makemoney.module.MendingHelper;


import java.util.List;

import com.google.gson.JsonArray;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.util.StringUtil;


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
    public ConfigBoolean autoRepairEnabled         = new ConfigBoolean("autoRepairEnabled", false, "自动为合金装备附魔经验修补", this);
    public ConfigArray   mendingBookPositions      = new ConfigArray("mendingBookPositions", createDefaultMendingBookPositions(), "经验修补附魔书容器位置", this);
    
    private static JsonArray createDefaultMendingBookPositions() {
        JsonArray defaultPositions = new JsonArray(3);
        defaultPositions.add(0);
        defaultPositions.add(0);
        defaultPositions.add(0);
        return defaultPositions;
    }

    public List<Integer> getMendingBookPositions() {
        return mendingBookPositions.getValueAsIntList();
    }

    public String getMendingBookPositionsString() {
        List<Integer> positions = getMendingBookPositions();
        return StringUtil.posToString(positions);
    }

    public boolean setMendingBookPosition(String positions) {
        List<Integer> positionsList = StringUtil.parseIntPos(positions);
        if (positionsList.size() != 3) {
            return false;
        }
        JsonArray newPos = new JsonArray();
        for (int position : positionsList) {
            newPos.add(position);
        }
        mendingBookPositions.setValue(newPos);
        return true;
    }

    public void setMendingBookPosition(int x, int y, int z) {
        mendingBookPositions.setValue(List.of(x, y, z));
    }
}
