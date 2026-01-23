package com.example.module.EntityHighlightBox;

import java.util.ArrayList;
import java.util.List;

import com.example.config.BaseConfig;

public class HighlightConfig extends BaseConfig {
    public boolean enabled = true;
    public boolean colorful = getDefaultColorful();
    public boolean renderName = getDefaultRenderName();         // 是否渲染实体名称
    public boolean renderItem = getDefaultRenderItem();         // 是否渲染掉落物
    public int renderRadius = getDefaultRenderRadius();         // 渲染半径
    public int renderMaxCounts = getDefaultRenderMaxCounts();   // 最大渲染数量
    public int updateInterval = getDefaultUpdateInterval();     // 渲染的实体更新间隔（tick）
    public String monsterColor = getDefaultMonsterColor();      // 敌对生物（红色）
    public String friendColor = getDefaultFriendColor();        // 友好生物（绿色）
    public String neutralColor = getDefaultNeutralColor();      // 中立生物（蓝色）
    public String playerColor = getDefaultPlayerColor();        // 玩家（黄色）
    public boolean isWhitelist = getDefaultIsWhitelist();       // 是否启用白名单模式
    public List<String> entityTypes = new ArrayList<>();        // 实体类型列表（白名单模式下有效，反之不渲染此实体列表）
    
    public static boolean getDefaultColorful() {
        return true;
    }
    
    public static boolean getDefaultRenderName() {
        return true;
    }
    
    public static boolean getDefaultRenderItem() {
        return true;
    }
    
    public static int getDefaultRenderRadius() {
        return 60;
    }
    
    public static int getDefaultRenderMaxCounts() {
        return 100;
    }
    
    public static int getDefaultUpdateInterval() {
        return 20;
    }

    public static String getDefaultMonsterColor() {
        return "FF5555FF";
    }

    public static String getDefaultFriendColor() {
        return "55FF55FF";
    }

    public static String getDefaultNeutralColor() {
        return "5555FFFF";
    }
    public static String getDefaultPlayerColor() {
        return "FFFF55FF";
    }

    public static boolean getDefaultIsWhitelist() {
        return false;
    }

    public HighlightConfig(String moduleName) {
        super(moduleName);
    }
}
