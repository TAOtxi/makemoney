package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.EventBus;
import cn.taotxi.Makemoney.util.InventoryUtil;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.StringUtil;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.inventory.InventoryMenu;

// TODO: 添加在容器中也可以应用此功能的选项
public class AutoDrop {
    public static final String MODULE_NAME = "autodrop";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AutoDropConfig config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
    public static int tickCounter = 0;

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (!config.enabled) return;
        if (config.showAttentionMsg) {
            Message.actionBarMsg(T.tl("autodrop.message.attention"));
        }

        if (tickCounter % config.checkInterval != 0) return;
        // if (config.triggerWhenPickup) return;

        Dropper.tryToDropItems();
    }

    public static void init() {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, level) -> {
            if (config.turnOffWhenChangeWorld) {
                config.enabled = false;
            }
        });
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        var command = dispatcher.register(ClientCommandManager.literal(MODULE_NAME).executes(AutoDrop::showHelp)
                .then(ClientCommandManager.literal("help").executes(AutoDrop::showHelp))
                .then(ClientCommandManager.literal("reload").executes(AutoDrop::reloadConfig))
                .then(ClientCommandManager.literal("config").executes(AutoDrop::openConfigGui))
                .then(ClientCommandManager.literal("on")
                    .executes(context -> toggleAutoDrop(context, true)))
                .then(ClientCommandManager.literal("off")
                    .executes(context -> toggleAutoDrop(context, false)))
                .then(ClientCommandManager.literal("ignore")
                    .then(ClientCommandManager.literal("reset")
                        .executes(AutoDrop::resetIgnoreSlots))
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("slots", StringArgumentType.string())
                            .executes(context -> setIgnoreSlots(context, false))))
                    .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument("slots", StringArgumentType.string())
                            .executes(context -> setIgnoreSlots(context, true))))
                    .then(ClientCommandManager.literal("current")
                        .executes(AutoDrop::ignoreNotEmptySlots))
                )
                .then(ClientCommandManager.literal("interval")
                    .then(ClientCommandManager.argument("interval", IntegerArgumentType.integer(1))
                        .executes(AutoDrop::setCheckInterval))
                )
            );
        dispatcher.register(ClientCommandManager.literal("ad")
                .executes(AutoDrop::showHelp)
                .redirect(command));
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl(MODULE_NAME + ".message.help"));
        return 1;
    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        config = AutoDropConfig.load(AutoDropConfig.class, MODULE_NAME);
        context.getSource().sendFeedback(T.tl("autodrop.reload.message"));
        return 1;
    }

    private static int toggleAutoDrop(CommandContext<FabricClientCommandSource> context, boolean enable) {
        context.getSource().sendFeedback(
            enable ? 
                T.tl("autodrop.enabled.message") : 
                T.tl("autodrop.disabled.message")
        );
        if (config.enabled == enable) return 1;

        config.enabled = enable;
        config.save();
        return 1;
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        EventBus.post("openConfigGui", Map.of("module", MODULE_NAME));
        return 1;
    }

    private static int setCheckInterval(CommandContext<FabricClientCommandSource> context) {
        int interval = context.getArgument("interval", Integer.class);
        config.checkInterval = interval;
        config.save();
        context.getSource().sendFeedback(T.tl("autodrop.checkInterval.message", interval));
        return 1;
    }

    private static int ignoreNotEmptySlots(CommandContext<FabricClientCommandSource> context) {
        config.ingnoreSlots = InventoryUtil.getInventoryNotEmptySlots();
        config.save();
        String slots = config.ingnoreSlots.toString();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.current.message", slots));
        return 1;
    }

    private static int resetIgnoreSlots(CommandContext<FabricClientCommandSource> context) {
        config.ingnoreSlots.clear();
        config.save();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.reset.message"));
        return 1;
    }

    private static int setIgnoreSlots(CommandContext<FabricClientCommandSource> context, boolean isCover) {
        String value = context.getArgument("slots", String.class);
        List<Integer> slots = StringUtil.parseIntPos(value);
        System.out.println(slots);
        if (!isCover) {
            slots.addAll(config.ingnoreSlots);
        }

        slots.sort(Comparator.naturalOrder());
        for (int i=slots.size()-1; i>=0; i--) {
            if (slots.get(i) < InventoryMenu.INV_SLOT_START ||
                slots.get(i) >= InventoryMenu.USE_ROW_SLOT_END ||
                (i > 0 && slots.get(i) == slots.get(i-1))) {
                slots.remove(i);
            }
        }
        System.out.println(slots);

        config.ingnoreSlots = slots;

        config.save();
        String slotsStr = slots.toString();
        context.getSource().sendFeedback(T.tl("autodrop.ignore.current.message", slotsStr));
        return 1;
    }
}
