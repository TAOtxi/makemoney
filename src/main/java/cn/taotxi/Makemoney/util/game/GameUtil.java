package cn.taotxi.Makemoney.util.game;

import java.util.ArrayList;
import java.util.List;

import cn.taotxi.Makemoney.util.StringUtil;
import net.minecraft.client.Minecraft;

public class GameUtil {
    private final static Minecraft client = Minecraft.getInstance();
    
    // Bug: 
    // 在原版客户端，所有的玩家实体，包括 NPC 玩家实体，都会被添加到在线玩家列表中。
    // （其实原版客户端是无法判断 NPC 玩家实体是否是真实玩家）
    // 随后插件服会发送删除 NPC 玩家实体的数据包，以达到排除 NPC 玩家实体的效果。
    // 这就导致在线玩家列表中可能会包含 NPC 玩家实体。
    // 目前没有较好的解决方法，只能判断玩家名字是否合法来排除 NPC 玩家实体。
    public static List<String> getOnlinePlayerNames() {
        var connection = client.getConnection();
        if (connection == null) {
            throw new IllegalStateException("Connection is null");
        }
        List<String> playerNames = new ArrayList<>();
        for (var player : connection.getOnlinePlayers()) {
            if (player.getProfile() != null && StringUtil.isValidName(player.getProfile().name())) {
                playerNames.add(player.getProfile().name());
            }
        }
        return playerNames;
    }
}
