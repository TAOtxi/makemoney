package cn.taotxi.Makemoney.module.NineteenWorld;

import java.util.Comparator;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/**
 * Tab 列表排序。
 *
 * 原版 PlayerTabOverlay 的排序依据是
 * listOrder(倒序) -> 是否旁观者 -> 队伍名 -> 玩家名(忽略大小写)，
 * 插件服给玩家挂的称号前缀存在 PlayerInfo#getTabListDisplayName 里，并不参与原版排序。
 * 开启配置后，改为按显示名排序，把同称号的玩家聚在一起。
 */
public class TabListSort {
    private static final Minecraft client = Minecraft.getInstance();
    private static final NineteenWorldConfig CONFIG = NineteenWorldConfig.getInstance();

    /** 与原版 PlayerTabOverlay#getPlayerInfos 保持一致的条目上限 */
    private static final int MAX_ENTRIES = 80;

    private static final Comparator<PlayerInfo> DISPLAY_NAME_COMPARATOR =
        Comparator.comparing(TabListSort::getSortKey, String::compareToIgnoreCase)
                  .thenComparing(playerInfo -> playerInfo.getProfile().name(), String::compareToIgnoreCase);

    /**
     * @return 按显示名排序后的玩家列表，功能关闭或者拿不到连接时返回 null，此时交回原版处理
     */
    public static List<PlayerInfo> getSortedPlayerInfos() {
        if (!CONFIG.tabListSortByDisplayName.getValue()) return null;
        if (client.player == null) return null;

        ClientPacketListener connection = client.player.connection;
        if (connection == null) return null;

        return connection.getListedOnlinePlayers().stream()
                .sorted(DISPLAY_NAME_COMPARATOR)
                .limit(MAX_ENTRIES)
                .toList();
    }

    /**
     * 取排序用的键值。显示名为空时退回玩家名，
     * 同时剥掉 § 样式代码，避免插件塞进文本里的颜色符影响排序结果。
     */
    private static String getSortKey(PlayerInfo playerInfo) {
        Component displayName = playerInfo.getTabListDisplayName();
        String text = displayName != null ? displayName.getString() : playerInfo.getProfile().name();
        String stripped = ChatFormatting.stripFormatting(text);
        return stripped == null ? "" : stripped;
    }
}
