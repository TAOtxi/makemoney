package com.example.module.EntityHighlightBox;

import java.util.ArrayList;
import java.util.List;

import com.example.config.BaseConfig;

public class HighlightConfig extends BaseConfig {
    public boolean enabled = getDefaultEnabled();
    public boolean colorful = getDefaultColorful();
    public String monsterColor = getDefaultMonsterColor();      // 敌对生物（红色）
    public String friendColor = getDefaultFriendColor();        // 友好生物（绿色）
    public String neutralColor = getDefaultNeutralColor();      // 中立生物（蓝色）
    public String playerColor = getDefaultPlayerColor();        // 玩家（黄色）
    public String unknownColor = getDefaultUnknownColor();      // 未知实体（白色）
    public boolean isWhitelist = getDefaultIsWhitelist();       // 是否启用白名单模式
    public boolean isRenderName = getDefaultIsRenderName();     // 是否渲染实体名称
    public int renderRadius = getDefaultRenderRadius();         // 渲染半径
    public int renderMaxCounts = getDefaultRenderMaxCounts();   // 最大渲染数量
    public int updateInterval = getDefaultUpdateInterval();     // 渲染的实体更新间隔（tick）
    public List<String> entityTypes = getDefaultEntityTypes();  // 实体类型列表（白名单模式下有效，反之不渲染此实体列表）
    

    public static boolean getDefaultEnabled() {
        return false;
    }

    public static boolean getDefaultColorful() {
        return true;
    }
    
    public static boolean getDefaultIsRenderName() {
        return true;
    }
    
    public static int getDefaultRenderRadius() {
        return 128;
    }
    
    public static int getDefaultRenderMaxCounts() {
        return 100;
    }
    
    public static int getDefaultUpdateInterval() {
        return 5;
    }

    public static String getDefaultMonsterColor() {
        return "#FF5555";
    }

    public static String getDefaultFriendColor() {
        return "#55FF55";
    }

    public static String getDefaultNeutralColor() {
        return "#5555FF";
    }
    public static String getDefaultPlayerColor() {
        return "#FFFF55";
    }

    public static String getDefaultUnknownColor() {
        return "#FFFFFF";
    }

    public static boolean getDefaultIsWhitelist() {
        return false;
    }

    public static List<String> getDefaultEntityTypes() {
        return new ArrayList<>();
    }

    public HighlightConfig(String moduleName) {
        super(moduleName);
    }
}
