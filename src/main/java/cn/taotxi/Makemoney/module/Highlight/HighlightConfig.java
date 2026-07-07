package cn.taotxi.Makemoney.module.Highlight;

import java.awt.Color;
import java.util.ArrayList;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigInteger;
import cn.taotxi.Makemoney.config.type.ConfigString;


public class HighlightConfig extends ConfigManager {
    private static HighlightConfig instance = null;
    
    public static HighlightConfig getInstance() {
        if (instance == null) {
            instance = new HighlightConfig(Highlight.MODULE_NAME);
        }
        return instance;
    }

    public HighlightConfig(String moduleName) {
        super(moduleName);
    }

    public final ConfigBoolean enabled          = new ConfigBoolean("enabled", false, "是否启用高亮模式", this);
    public final ConfigBoolean colorful         = new ConfigBoolean("colorful", true, "是否启用彩色高亮模式", this);
    public final ConfigBoolean renderInList     = new ConfigBoolean("renderInList", false, "是否仅渲染高亮实体列表", this);
    public final ConfigInteger renderRadius     = new ConfigInteger("renderRadius", -1, "高亮渲染半径", this);

    public final ConfigString  defaultColor     = new ConfigString("defaultColor", "#ffffffff", "默认高亮颜色", this);
    public final ConfigString  enemyColor      = new ConfigString("enemyColor", "#ff0000ff", "敌对生物高亮颜色", this);
    public final ConfigString  animalColor      = new ConfigString("animalColor", "#00ff00ff", "动物高亮颜色", this);
    public final ConfigString  decorationColor  = new ConfigString("decorationColor", "#0000ffff", "装饰实体高亮颜色", this);
    public final ConfigString  playerColor      = new ConfigString("playerColor", "#9d00ffff", "玩家高亮颜色", this);
    public final ConfigString  itemColor        = new ConfigString("itemColor", "#ffffffff", "掉落物高亮颜色", this);

    public final ConfigArray<String> renderEntities = new ConfigArray<>("renderEntities", new ArrayList<>(), "高亮实体列表", this, String.class);

    public static String colorToStr(Color color) {
        return String.format(
            "#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
            .toUpperCase();
    }

    public static void saveColor(ConfigString configString, Color color) {
        configString.setValue(colorToStr(color));
    }

    public static Color RGBA_StrToColor(String hex) {
        int argb = RGBA_StrToARBG(hex);
        return new Color(argb, true);
    }

    public static int RGBA_StrToARBG(String hex) {
        hex = hex.substring(1);
        if (hex.matches("[0-9A-Fa-f]{6}")) {
            hex = hex + "ff";
        }
        int rgba = (int) Long.parseLong(hex, 16);
        
        return (rgba & 0xFF) << 24 | rgba >> 8;
    }


}
