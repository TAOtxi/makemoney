package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.StringUtil;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import cn.taotxi.Makemoney.util.game.ItemStackUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

// TODO: 添加在容器中也可以应用此功能的选项
public class AutoDrop {
    public static final String MODULE_NAME = "autodrop";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static boolean enabled = false;
    private static AutoDropConfig CONFIG = AutoDropConfig.getInstance();
    private static final String TIME_TRIGGER_TASK_NAME = "autodrop_timeTrigger";
    private static final String SHOW_ATTENTION_MSG_TASK_NAME = "autodrop_showAttentionMsg";

    public static void initialize() {
        CONFIG.loadConfig();

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, level) -> {
            if (CONFIG.turnOffWhenChangeWorld.getValue()) {
                enabled = false;
            }
        });

        CONFIG.isShowAttentionMsg.onChange(
            (oldValue, newValue) -> {
                if (!enabled) return;
                if (newValue) {
                    createShowAttentionMsgTask();
                } else {
                    TaskUtil.removeTimeTask(SHOW_ATTENTION_MSG_TASK_NAME);
                }
            }
        );

        CONFIG.isTimeTrigger.onChange(
            (oldValue, newValue) -> {
                if (!enabled) return;
                if (newValue) {
                    createTimeTriggerTask();
                } else {
                    TaskUtil.removeTimeTask(TIME_TRIGGER_TASK_NAME);
                }
            }
        );
    }

    public static void toggleSwitch(boolean enable) {
        if (AutoDrop.enabled == enable) return;

        AutoDrop.enabled = enable;
        updateTask();
    }

    private static void updateTask() {
        if (enabled) {
            if (
                CONFIG.isShowAttentionMsg.getValue() &&
                !TaskUtil.hasTimeTask(SHOW_ATTENTION_MSG_TASK_NAME)
            ) {
                createShowAttentionMsgTask();
            } else if (!CONFIG.isShowAttentionMsg.getValue()) {
                TaskUtil.removeTimeTask(SHOW_ATTENTION_MSG_TASK_NAME);
            }

            if (
                CONFIG.isTimeTrigger.getValue() &&
                !TaskUtil.hasTimeTask(TIME_TRIGGER_TASK_NAME)
            ) {
                createTimeTriggerTask();
            } else if (!CONFIG.isTimeTrigger.getValue()) {
                TaskUtil.removeTimeTask(TIME_TRIGGER_TASK_NAME);
            }
        } else {
            TaskUtil.removeTimeTask(SHOW_ATTENTION_MSG_TASK_NAME);
            TaskUtil.removeTimeTask(TIME_TRIGGER_TASK_NAME);
        }
    }

    private static void createShowAttentionMsgTask() {
        TaskUtil.createTimeTask(
            SHOW_ATTENTION_MSG_TASK_NAME, 
            AutoDrop::showAttentionMsg, 
            20
        );
    }

    private static void createTimeTriggerTask() {
        TaskUtil.createTimeTask(
            TIME_TRIGGER_TASK_NAME, 
            Dropper::tryToDropItems, 
            () -> CONFIG.timeTriggerInterval.getValue()
        );
    }

    private static void showAttentionMsg() {
        boolean isTimeTrigger = CONFIG.isTimeTrigger.getValue();
        boolean isPickUpItemTrigger = CONFIG.isPickUpItemTrigger.getValue();
        if (!isTimeTrigger && !isPickUpItemTrigger) return;

        String triggerItemId = CONFIG.triggerItemId.getValue();
        if (!isTimeTrigger && isPickUpItemTrigger) {
            if (!triggerItemId.isEmpty()) {
                Message.actionBarMsg(T.tl("autodrop.message.attention.pickUpItemTriggerWithItem", triggerItemId));
            } else {
                Message.actionBarMsg(T.tl("autodrop.message.attention.pickUpItemTrigger"));
            }
            return;
        }

        int pendingTick = TaskUtil.getNextRunTick(TIME_TRIGGER_TASK_NAME) - TaskUtil.getTicker();
        int pendingSeconds = pendingTick / 20;

        if (!isPickUpItemTrigger) {
            Message.actionBarMsg(T.tl("autodrop.message.attention.timeTrigger", pendingSeconds));
            return;
        }

        if (!triggerItemId.isEmpty()) {
            Message.actionBarMsg(T.tl("autodrop.message.attention.bothWithItem", pendingSeconds, triggerItemId));
        } else {
            Message.actionBarMsg(T.tl("autodrop.message.attention.both", pendingSeconds));
        }
    }

    public static void onTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket) {
        if (!enabled ||
            clientboundTakeItemEntityPacket.getPlayerId() != Minecraft.getInstance().player.getId() ||
            !CONFIG.isPickUpItemTrigger.getValue()) {
            return;
        }
        if (CONFIG.triggerItemId.getValue().isEmpty()) {
            Dropper.tryToDropItems();
            return;
        }
        
        Entity entity = Minecraft.getInstance().level.getEntity(clientboundTakeItemEntityPacket.getItemId());
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack itemStack = itemEntity.getItem();
            if (ItemStackUtil.equalId(itemStack, CONFIG.triggerItemId.getValue())) {
                Dropper.tryToDropItems();

                if (TaskUtil.hasTimeTask("autodrop_timeTrigger")) {
                    TaskUtil.resetNextRunTick("autodrop_timeTrigger");
                }
            }
        }
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        var command = dispatcher.register(ClientCommandManager.literal(MODULE_NAME).executes(AutoDrop::showHelp)
                .then(ClientCommandManager.literal("help").executes(AutoDrop::showHelp))
                .then(ClientCommandManager.literal("reload").executes(AutoDrop::reloadConfig))
                .then(ClientCommandManager.literal("config").executes(AutoDrop::openConfigGui))
                .then(ClientCommandManager.literal("test").executes(AutoDrop::test))
                .then(ClientCommandManager.literal("on")
                    .executes(context -> toggleAutoDrop(context, true)))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> toggleAutoDrop(context, false)))
                .then(ClientCommandManager.literal("ignore")
                    .then(ClientCommandManager.literal("reset")
                        .executes(AutoDrop::resetIgnoreSlots))
                    .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("1,2,3,4,...", StringArgumentType.string())
                            .executes(AutoDrop::setIgnoreSlots)))
                    .then(ClientCommandManager.literal("current")
                        .executes(AutoDrop::ignoreNotEmptySlots))
                )
                .then(ClientCommandManager.literal("interval")
                    .then(ClientCommandManager.argument("interval", IntegerArgumentType.integer(1))
                        .executes(AutoDrop::setTimeTriggerInterval))
                )
            );
        dispatcher.register(ClientCommandManager.literal("ad")
                .executes(AutoDrop::showHelp)
                .redirect(command));
    }

    private static int test(CommandContext<FabricClientCommandSource> context) {
        Dropper.drop();
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl(MODULE_NAME + ".help.message"));
        return 1;
    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        CONFIG.reloadConfig();
        updateTask();
        context.getSource().sendFeedback(T.tl("autodrop.reload.message"));
        return 1;
    }

    private static int toggleAutoDrop(CommandContext<FabricClientCommandSource> context, boolean enable) {
        context.getSource().sendFeedback(
            enable ? 
                T.tl("autodrop.enabled.message") : 
                T.tl("autodrop.disabled.message")
        );
        toggleSwitch(enable);
        return 1;
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        GuiUtil.openYaclScreen(MODULE_NAME);
        return 1;
    }

    private static int setTimeTriggerInterval(CommandContext<FabricClientCommandSource> context) {
        int interval = context.getArgument("interval", Integer.class);
        CONFIG.timeTriggerInterval.setValue(interval);
        CONFIG.saveConfig();
        context.getSource().sendFeedback(T.tl("autodrop.timeTriggerInterval.message", interval));
        return 1;
    }

    private static int ignoreNotEmptySlots(CommandContext<FabricClientCommandSource> context) {
        List<Integer> slots = InventoryUtil.getInventoryNotEmptySlots();
        CONFIG.ignoreSlots.setValue(slots);
        CONFIG.saveConfig();
        String slotsStr = slots.toString();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.current.message", slotsStr));
        return 1;
    }

    private static int resetIgnoreSlots(CommandContext<FabricClientCommandSource> context) {
        CONFIG.ignoreSlots.resetValue();
        CONFIG.saveConfig();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.reset.message"));
        return 1;
    }

    private static int setIgnoreSlots(CommandContext<FabricClientCommandSource> context) {
        String value = context.getArgument("slots", String.class);
        List<Integer> slots = StringUtil.strToIntList(value);

        slots.sort(Comparator.naturalOrder());
        for (int i=slots.size()-1; i>=0; i--) {
            if (slots.get(i) < InventoryMenu.INV_SLOT_START ||
                slots.get(i) >= InventoryMenu.USE_ROW_SLOT_END ||
                (i > 0 && slots.get(i) == slots.get(i-1))) {
                slots.remove(i);
            }
        }
        CONFIG.ignoreSlots.setValue(slots);
        CONFIG.saveConfig();
        String slotsStr = slots.toString();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.current.message", slotsStr));
        return 1;
    }
}
