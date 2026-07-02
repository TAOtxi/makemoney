package cn.taotxi.Makemoney.module.MessageCommand;

import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;

public class MessageCommandConfig extends ConfigManager {
    private static MessageCommandConfig instance = null;

    public static MessageCommandConfig getInstance() {
        if (instance == null) {
            instance = new MessageCommandConfig(MessageCommand.MODULE_NAME);
        }
        return instance;
    }

    public MessageCommandConfig(String moduleName) {
        super(moduleName);
    }

    public ConfigBoolean  enabled                 = new ConfigBoolean("enabled", false, "开关", this);
    public ConfigArray<MessageRule> messageRules  = new ConfigArray<MessageRule>("messageRules", "消息规则", this, MessageRule.class);

    public void cleanRule() {
        messageRules.clear();
    }

    public void removeRule(int index) {
        messageRules.remove(index);
    }

    public MessageRule getRule(int index) {
        return messageRules.get(index);
    }

    public List<MessageRule> getRules() {
        return messageRules.getValueAsList();
    }

    public void addRule(MessageRule rule) {
        try {
            Pattern.compile(rule.matcher);
        } catch (Exception e) {
            MessageCommand.logger.error("Invalid matcher: {}", rule.matcher);
            return;
        }

        // Add to top
        JsonArray newRules = new JsonArray();
        newRules.add(ConfigManager.getGson().toJsonTree(rule));
        newRules.addAll(messageRules.getValue());
        messageRules.setValue(newRules);
    }

    public void addRule() {
        addRule(new MessageRule());
    }

    public MessageRule getDefaultRule() {
        return new MessageRule();
    }

    public boolean getRuleEnabled(int index) {
        return messageRules.getValue().get(index)
            .getAsJsonObject().get("enabled").getAsBoolean();
    }

    public void setRuleEnabled(int index, boolean enabled) {
        JsonObject rule = messageRules.getValue().get(index).getAsJsonObject();
        rule.remove("enabled");
        rule.addProperty("enabled", enabled);
    }

    public String getRuleMatcher(int index) {
        return messageRules.getValue().get(index)
            .getAsJsonObject().get("matcher").getAsString();
    }

    public void setRuleMatcher(int index, String matcher) {
        JsonObject rule = messageRules.getValue().get(index).getAsJsonObject();
        rule.remove("matcher");
        rule.addProperty("matcher", matcher);
    }

    public String getRuleCommand(int index) {
        return messageRules.getValue().get(index)
            .getAsJsonObject().get("command").getAsString();
    }

    public void setRuleCommand(int index, String command) {
        JsonObject rule = messageRules.getValue().get(index).getAsJsonObject();
        rule.remove("command");
        rule.addProperty("command", command);
    }

    public String getRuleDescription(int index) {
        return messageRules.getValue().get(index)
            .getAsJsonObject().get("description").getAsString();
    }

    public void setRuleDescription(int index, String description) {
        JsonObject rule = messageRules.getValue().get(index).getAsJsonObject();
        rule.remove("description");
        rule.addProperty("description", description);
    }

    
}

class MessageRule {
    public String description;
    public boolean enabled;
    public String matcher;
    public String command;

    public MessageRule() {
        this("", "");
    }

    public MessageRule(String matcher, String command) {
        this(matcher, command, "Match Rule");
    }

    public MessageRule(String matcher, String command, String description) {
        enabled = true;
        this.matcher = matcher;
        this.command = command;
        this.description = description;
    }
}