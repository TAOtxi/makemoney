package cn.taotxi.Makemoney.module.StrangeFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import cn.taotxi.Makemoney.util.MLogger;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class IgnoreMessage {
    private static final String MODULE_NAME = "ignoreMessage";
    private static final MLogger logger = new MLogger(MODULE_NAME);
    private static final StrangeConfig CONFIG = StrangeConfig.getInstance();
    private static final List<Pattern> ignorePatterns = new ArrayList<>();

    public static void initialize() {
        CONFIG.ignoreList.onChange(
            (oldValue, newValue) -> {
                ignorePatterns.clear();
                for (JsonElement pattern : newValue) {
                    try {
                        ignorePatterns.add(Pattern.compile(pattern.getAsString()));
                    } catch (Exception e) {
                        logger.error(
                            "Invalid ignore pattern: {}", pattern.getAsString(), e);
                    }
                }
            }
        );
        CONFIG.ignoreEnabled.triggerConfigChange();
    }

    public static boolean isIgnored(String message) {
        // String noColorString = StringUtil.stripColor(message);
        return ignorePatterns.stream().anyMatch(
            pattern -> pattern.matcher(message).find());
    }

    public static void handleChatMessage(ClientboundSystemChatPacket chatMessageS2CPacket_1, CallbackInfo ci) {
        if (!CONFIG.ignoreEnabled.getValue()) {
            return;
        }
        if (isIgnored(chatMessageS2CPacket_1.content().getString())) {
            ci.cancel();
        }
    }
    
    public static void addPresetIgnoreList() {
        List<String> list = List.of(
            "^\\[拾玖福彩\\]",
            "^【猜单词游戏】$|^拾玖喵不太认识这个单词：|^提示：|^用 /word <你的猜测> 回答本题（每人仅一次）$|^当前词库：|^----------------------$",
            "^地震信息$|^ 20\\d{2}年\\d{2}月\\d{2}日 \\d{2}时\\d{2}分\\d{2}秒 发生$|^ (?:震中|震级|深度|最大震度|海啸信息|最大烈度|更新时间)|地震.*? \\| 第\\d{1,2}报|^中国地震台网",
            "^拾玖喵小道消息",
            "^拾玖喵次元口袋",
            "^别忘了去看看，奖励多多别错过喵~",
            "^$",
            "^输入/show来向大家炫耀你的物品吧喵~",
            "^\\[.*?\\] 拾玖型扫地机器人",
            "^\\w{1,16} 从 \\w+ 切换到 \\w+|^\\w{1,16} 离开了 \\w+$",
            "^\\w{1,16}(?:退出|加入)了游戏$|^\\w{1,16} joined \\w+$|^\\w{1,16} was disconnected$",
            "^<\\w{1,16}> (?:\\d+|all)$"
        );
        CONFIG.ignoreList.setStringValue(list);
    }
}
