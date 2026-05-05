package cn.taotxi.Makemoney.module.StrangeFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import com.google.gson.JsonArray;
import cn.taotxi.Makemoney.util.MLogger;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class IgnoreMessage {
    private static final String MODULE_NAME = "ignoreMessage";
    private static final MLogger logger = new MLogger(MODULE_NAME);
    private static List<Pattern> ignorePatterns = new ArrayList<>();

    public static void init() {
        for (String pattern : getIgnoreList(false)) {
            try {
                ignorePatterns.add(Pattern.compile(pattern));
            } catch (Exception e) {
                logger.error("Invalid ignore pattern: {}", pattern, e);
            }
        }
    }

    public static boolean isEnabled(boolean isDefault) {
        return StrangeConfig.getInstance().getBoolean("ignore_enabled", isDefault);
    }

    public static void setEnabled(boolean enabled) {
        StrangeConfig.getInstance().putBoolean("ignore_enabled", enabled);
    }

    public static boolean addIgnoreList(String pattern) {
        if (pattern.isEmpty()) {
            return false;
        }
        JsonArray ignoreListNode = StrangeConfig.getInstance().getJsonArray("ignore_list", false);
        try {
            Pattern newPattern = Pattern.compile(pattern);
            ignorePatterns.add(newPattern);
            ignoreListNode.add(pattern);
            return true;
        } catch (Exception e) {
            logger.error("Invalid ignore pattern: {}", pattern, e);
            return false;
        }
    }

    public static void setIgnoreList(List<String> ignoreList) {
        ignorePatterns.clear();
        StrangeConfig.getInstance().reset("ignore_list");
        for (String pattern : ignoreList) {
            addIgnoreList(pattern);
        }
    }

    public static List<String> getIgnoreList(boolean isDefault) {
        JsonArray ignoreListNode = StrangeConfig.getInstance().getJsonArray("ignore_list", isDefault);
        return StrangeConfig.jsonStrToList(ignoreListNode);
    }

    public static boolean isIgnored(String message) {
        // String noColorString = StringUtil.stripColor(message);
        return ignorePatterns.stream().anyMatch(
            pattern -> pattern.matcher(message).find());
    }

    public static void handleChatMessage(ClientboundSystemChatPacket chatMessageS2CPacket_1, CallbackInfo ci) {
        if (!isEnabled(false)) {
            return;
        }
        if (isIgnored(chatMessageS2CPacket_1.content().getString())) {
            ci.cancel();
        }
    }
}
