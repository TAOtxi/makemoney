package cn.taotxi.Makemoney.module.IgnoreMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.azure.json.models.JsonString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.util.ConfigMaker;
import cn.taotxi.Makemoney.util.JsonObjectUtil;
import cn.taotxi.Makemoney.util.MLogger;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.util.StringUtil;

public class IgnoreMessage {
    private static final String MODULE_NAME = "ignore_message";
    private static final MLogger logger = new MLogger(MODULE_NAME);
    private static JsonObject config;
    private static JsonObject defaultConfig;
    private static List<Pattern> ignorePatterns = new ArrayList<>();
    public static boolean configChanged = false;

    public static void init() {
        defaultConfig = createDefaultConfig();
        JsonElement configElement = ConfigMaker.loadConfig(MODULE_NAME, defaultConfig);
        config = configElement.getAsJsonObject();
        
        for (String pattern : getIgnoreList(false)) {
            try {
                ignorePatterns.add(Pattern.compile(pattern));
            } catch (Exception e) {
                logger.error("Invalid ignore pattern: {}", pattern, e);
            }
        }
    }

    private static JsonObject createDefaultConfig() {
        JsonObject config = new JsonObject();
        JsonObjectUtil.putBoolean(config, "enabled", false);
        JsonObjectUtil.put(config, "ignoreList", new JsonArray());
        return config;
    }

    public static boolean isEnabled(boolean forceDefault) {
        if (config.has("enabled") && !forceDefault) {
            return config.get("enabled").getAsBoolean();
        }
        return defaultConfig.get("enabled").getAsBoolean();
    }

    public static void setEnabled(boolean enabled) {
        JsonObjectUtil.putBoolean(config, "enabled", enabled);
        configChanged = true;
    }

    public static boolean addIgnoreList(String pattern) {
        if (pattern.isEmpty()) {
            return false;
        }
        try {
            Pattern newPattern = Pattern.compile(pattern);
            ignorePatterns.add(newPattern);
            JsonArray ignoreListNode = config.get("ignoreList").getAsJsonArray();
            ignoreListNode.add(pattern);
            return true;
        } catch (Exception e) {
            logger.error("Invalid ignore pattern: {}", pattern, e);
            return false;
        }
    }

    public static void setIgnoreList(List<String> ignoreList) {
        ignorePatterns.clear();
        JsonArray ignoreListNode = new JsonArray();
        for (String pattern : ignoreList) {
            if (pattern.isEmpty()) {
                continue;
            }
            try {
                ignorePatterns.add(Pattern.compile(pattern));
                ignoreListNode.add(pattern);
            } catch (Exception e) {
                logger.error("Invalid ignore pattern: {}", pattern, e);
            }
        }
        JsonObjectUtil.put(config, "ignoreList", ignoreListNode);
        configChanged = true;
    }

    public static List<String> getIgnoreList(boolean forceDefault) {
        if (config.has("ignoreList") && !forceDefault) {
            return JsonObjectUtil.jsonStrToList(config.get("ignoreList").getAsJsonArray());
        }
        return JsonObjectUtil.jsonStrToList(defaultConfig.get("ignoreList").getAsJsonArray());
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
    
    public static void saveConfig() {
        if (!configChanged) {
            return;
        }
        configChanged = false;
        ConfigMaker.saveConfig(MODULE_NAME, config);
    }
}
