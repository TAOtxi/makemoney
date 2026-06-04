package cn.taotxi.Makemoney.module.MessageCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

public class MessageCommand {
    public static final String MODULE_NAME = "messagecommand";
    public static final MLogger logger = new MLogger(MODULE_NAME);
    private static final Minecraft client = Minecraft.getInstance();
    private static final MessageCommandConfig CONFIG = MessageCommandConfig.getInstance();

    private static final Pattern placeholderPattern = Pattern.compile("\\$\\{(\\d+)(?::([^}]*))?\\}");
    private static final Map<Pattern, String> rules = new HashMap<>();


    public static void initialize() {
        CONFIG.loadConfig();

        CONFIG.messageRules.onChange(
            (oldValue, newValue) -> {
                rules.clear();
                
                for (MessageRule rule : CONFIG.getRules()) {
                    if (rule.enabled) {
                        rules.put(Pattern.compile(rule.matcher), rule.command);
                    }
                }
            }
        );
        CONFIG.messageRules.triggerConfigChange();
        registCommand();
    }

    public static void onMessage(String message) {
        if (!CONFIG.enabled.getValue()) {
            return;
        }

        // System.out.println("原始消息: " + message);
        for (Pattern pattern : rules.keySet()) {
            Matcher matcher = pattern.matcher(message);
            // System.out.println("当前匹配规则: " + pattern.pattern());
            if (matcher.find()) {
                // System.out.println("匹配成功消息: " + matcher.group());
                try {
                    executeCommand(matcher, rules.get(pattern));
                } catch (Exception e) {
                    client.player.displayClientMessage(
                        T.l("Error in pattern \"" + pattern.pattern() + "\": " + e.getMessage())
                            .withStyle(ChatFormatting.RED), false);
                }
            }
        }
    }

    private static void executeCommand(Matcher messageMatcher, String command) {
        Matcher placeholderPatternMatcher = placeholderPattern.matcher(command);
        StringBuilder result = new StringBuilder();

        while (placeholderPatternMatcher.find()) {
            int index = Integer.parseInt(placeholderPatternMatcher.group(1));
            String defaultValue = 
                placeholderPatternMatcher.group(2) == null ? "" : placeholderPatternMatcher.group(2);
            String replacement = 
                messageMatcher.group(index) == null ? defaultValue : messageMatcher.group(index);
            placeholderPatternMatcher.appendReplacement(
                result, 
                Matcher.quoteReplacement(replacement)
            );
        }
        placeholderPatternMatcher.appendTail(result);
        // System.out.println("result: " + result.toString());

        Message.sendMessage(result.toString());
    }

    private static void registCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .executes(MessageCommand::showHelp)
                .then(ClientCommandManager.literal("help")
                    .executes(MessageCommand::showHelp))
                .then(ClientCommandManager.literal("config")
                    .executes(context -> {
                        GuiUtil.openYaclScreen(Makemoney.MOD_ID, 1);
                        return 1;
                    }))
                .then(ClientCommandManager.literal("on")
                    .executes(context -> {
                        CONFIG.enabled.enable();
                        return 1;
                    }))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> {
                        CONFIG.enabled.disable();
                        return 1;
                    }))
            );
            
            dispatcher.register(ClientCommandManager.literal("mr")
                    .executes(MessageCommand::showHelp)
                    .redirect(cmd));
        });
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.l("messageCommand.help"));
        return 1;
    }
}
