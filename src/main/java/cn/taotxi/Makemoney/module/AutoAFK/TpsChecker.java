package cn.taotxi.Makemoney.module.AutoAFK;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public class TpsChecker {
    private static final AutoAFKConfig CONFIG = AutoAFKConfig.getInstance();
    private static final String CHECK_TASK_NAME = "tpsCheck";
    private static final String GREEN_CHECK_TASK_NAME = "greenTpsCheck";
    private static final String OPEN_CHECK_DELAY = "openCheckDelay";

    public static void initialize() {
        CONFIG.tpsCheckEnabled.onChange(
            (oldValue, newValue) -> {
                TaskUtil.removeTimeTask(GREEN_CHECK_TASK_NAME);
                if (newValue && !TaskUtil.hasTimeTask(CHECK_TASK_NAME)) {
                    TaskUtil.createTimeTask(CHECK_TASK_NAME, TpsChecker::checker, 20);
                } else if (!newValue) {
                    TaskUtil.removeTimeTask(CHECK_TASK_NAME);
                }
            }
        );
        CONFIG.tpsCheckEnabled.triggerConfigChange();
    }

    private static void checker() {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        float tps = calcServerTps.getTps();
        int tpsThreshold = CONFIG.safetyTpsThreshold.getValue();
        if (tps >= tpsThreshold) {
            return;
        }

        Message.clientSideMsg(T.tl("autoAFK.tpsCheck.message", tps, tpsThreshold));
        String command = CONFIG.triggerCommand.getValue();
        if (!command.isEmpty()) {
            Message.sendMessage(command);
        }
        CONFIG.tpsCheckEnabled.disable();
        CONFIG.saveConfig();

        String greenCommand = CONFIG.greenTriggerCommand.getValue();
        if (!greenCommand.isEmpty()) {
            TaskUtil.createTimeTask(GREEN_CHECK_TASK_NAME, TpsChecker::greenChecker, 20);
        }
    }

    private static void greenChecker() {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        float tps = calcServerTps.getTps();
        int tpsThreshold = CONFIG.greenTpsThreshold.getValue();
        if (tps < tpsThreshold) {
            return;
        }

        Message.clientSideMsg(T.tl("autoAFK.tpsCheck.greenMessage", tps, tpsThreshold));
        String command = CONFIG.greenTriggerCommand.getValue();
        if (!command.isEmpty()) {
            Message.sendMessage(command);
        }
        TaskUtil.removeTimeTask(GREEN_CHECK_TASK_NAME);

        TaskUtil.createOnceTimeTask(OPEN_CHECK_DELAY, () -> {
            CONFIG.tpsCheckEnabled.enable();
            CONFIG.saveConfig();
        }, 5 * 20);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> tpsCheckCmd() {
        return ClientCommandManager.literal("tpsCheck")
            .then(ClientCommandManager.literal("on")
                .executes(context -> {
                    CONFIG.tpsCheckEnabled.enable();
                    CONFIG.saveConfig();
                    context.getSource().sendFeedback(T.tl("autoAFK.tpsCheck.on.message"));
                    return 1;
                }))
            .then(ClientCommandManager.literal("off")
                .executes(context -> {
                    CONFIG.tpsCheckEnabled.disable();
                    CONFIG.saveConfig();
                    context.getSource().sendFeedback(T.tl("autoAFK.tpsCheck.off.message"));
                    return 1;
                }));
    }
}
