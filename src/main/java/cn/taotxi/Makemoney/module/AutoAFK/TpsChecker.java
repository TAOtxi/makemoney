package cn.taotxi.Makemoney.module.AutoAFK;

import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.minecraft.client.Minecraft;

public class TpsChecker {
    private static final AutoAFKConfig CONFIG = AutoAFKConfig.getInstance();
    private static final String CHECK_TASK_NAME = "tpsCheck";

    public static void initialize() {
        CONFIG.tpsCheckEnabled.onChange(
            (oldValue, newValue) -> {
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
    }
}
