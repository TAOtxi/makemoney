package cn.taotxi.Makemoney.module.AutoAFK;

import java.util.ArrayList;
import java.util.List;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.module.AutoAFK.AutoAFKConfig.PositionCheckItem;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.minecraft.client.Minecraft;


public class PositionChecker {
    private static final AutoAFKConfig CONFIG = AutoAFKConfig.getInstance();
    private static final Minecraft client = Minecraft.getInstance();
    private static List<PositionCheckItem> positionCheckItems = new ArrayList<>();
    private static final String CHECK_TASK_NAME = "position_check";


    public static void initialize() {
        CONFIG.positionCheckItems.onChange(
            (oldValue, newValue) -> {
                positionCheckItems = CONFIG.positionCheckItems.getValueAsList();
                positionCheckItems.removeIf(
                    item -> !item.isEnabled() || item.getCommand().isEmpty()
                );
            }
        );
        CONFIG.positionCheckItems.triggerConfigChange();

        CONFIG.positionCheckEnabled.onChange(
            (oldValue, newValue) -> {
                if (newValue && !TaskUtil.hasTimeTask(CHECK_TASK_NAME)) {
                    TaskUtil.createTimeTask(
                        CHECK_TASK_NAME, PositionChecker::tick, () -> CONFIG.positionCheckInterval.getValue()
                    );
                } else if (!newValue) {
                    TaskUtil.removeTimeTask(CHECK_TASK_NAME);
                }
            }
        );
        CONFIG.positionCheckEnabled.triggerConfigChange();
    }

    private static void tick() {
        if (client.player == null) {
            return;
        }

        if (ConfigScreen.isOpenYaclScreen()) {
            return;
        }

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        String world = client.level.dimension().identifier().toString();
        for (PositionCheckItem item : positionCheckItems) {
            if (!item.isInSameWorld(world)) {
                continue;
            }
            if (item.isInArea(x, y, z)) {
                Message.sendMessage(item.getCommand());
            }
        }
    }
}

