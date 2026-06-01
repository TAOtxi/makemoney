package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.EventBus;
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

    public static void init() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, level) -> {
            if (AutoDropConfig.getInstance().isTurnOffWhenChangeWorld()) {
                enabled = false;
            }
        });
        TaskUtil.createTimeTask(
            "autodrop_timeTrigger", 
            AutoDrop::timeTriggerTask, 
            () -> AutoDropConfig.getInstance().getTimeTriggerInterval()
        );

        TaskUtil.createTimeTask(
            "autodrop_showAttentionMsg", 
            AutoDrop::showAttentionMsg, 
            20
        );
    }

    private static void timeTriggerTask() {
        if (!enabled || !AutoDropConfig.getInstance().isTimeTrigger()) return;
        Dropper.tryToDropItems();
    }

    private static void showAttentionMsg() {
        if (!enabled) return;
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.isShowAttentionMsg()) return;
        if (!config.isTimeTrigger() && !config.isPickUpItemTrigger()) return;

        Message.actionBarMsg(T.tl("autodrop.message.attention"));
    }

    public static void onTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket) {
        if (!enabled ||
            clientboundTakeItemEntityPacket.getPlayerId() != Minecraft.getInstance().player.getId() ||
            !AutoDropConfig.getInstance().isPickUpItemTrigger()) {
            return;
        }
        if (AutoDropConfig.getInstance().getTriggerItemId().isEmpty()) {
            Dropper.tryToDropItems();
            return;
        }
        
        Entity entity = Minecraft.getInstance().level.getEntity(clientboundTakeItemEntityPacket.getItemId());
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack itemStack = itemEntity.getItem();
            if (ItemStackUtil.equalId(itemStack, AutoDropConfig.getInstance().getTriggerItemId())) {
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
        AutoDropConfig.getInstance().reloadConfig();
        context.getSource().sendFeedback(T.tl("autodrop.reload.message"));
        return 1;
    }

    private static int toggleAutoDrop(CommandContext<FabricClientCommandSource> context, boolean enable) {
        context.getSource().sendFeedback(
            enable ? 
                T.tl("autodrop.enabled.message") : 
                T.tl("autodrop.disabled.message")
        );
        AutoDrop.enabled = enable;
        return 1;
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        EventBus.post("openConfigGui", Map.of("module", MODULE_NAME));
        return 1;
    }

    private static int setTimeTriggerInterval(CommandContext<FabricClientCommandSource> context) {
        int interval = context.getArgument("interval", Integer.class);
        AutoDropConfig.getInstance().setTimeTriggerInterval(interval);
        AutoDropConfig.getInstance().saveConfig();
        context.getSource().sendFeedback(T.tl("autodrop.timeTriggerInterval.message", interval));
        return 1;
    }

    private static int ignoreNotEmptySlots(CommandContext<FabricClientCommandSource> context) {
        List<Integer> slots = InventoryUtil.getInventoryNotEmptySlots();
        AutoDropConfig.getInstance().setIgnoreSlots(slots);
        AutoDropConfig.getInstance().saveConfig();
        String slotsStr = slots.toString();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.current.message", slotsStr));
        return 1;
    }

    private static int resetIgnoreSlots(CommandContext<FabricClientCommandSource> context) {
        AutoDropConfig.getInstance().setIgnoreSlots(List.of());
        AutoDropConfig.getInstance().saveConfig();
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
        AutoDropConfig.getInstance().setIgnoreSlots(slots);
        AutoDropConfig.getInstance().saveConfig();
        String slotsStr = slots.toString();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.current.message", slotsStr));
        return 1;
    }
}
