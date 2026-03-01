package cn.taotxi.Makemoney.module.AutoAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.EventBus;
import cn.taotxi.Makemoney.util.ItemStackUtil;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

public class AutoAction {
    public static final String MODULE_NAME = "action";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    private static int runActionIndex = 0;
    private static int defaultDelay = 5;
    private static int nextRunTick = 0;
    private static boolean isRunning = false;
    private static List<Action> actions = new ArrayList<>();

    public static void init() {
        registerCommand();
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            Message.chatMsg("Screen: " + screen.getClass().getSimpleName());
        });
    }

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        sellWitherSkeletonSkull(client);
        if (actions.isEmpty() || !isRunning) {
            return;
        }
        if (tickCounter < nextRunTick) {
            return;
        }
        if (runActionIndex >= actions.size()) {
            resetActions();
            return;
        }
        Action action = actions.get(runActionIndex);
        LocalPlayer player = client.player;
        if (action instanceof CommandAction commandAction) {
            Message.sendMessage(commandAction.command);
        } else if (action instanceof ClickAction clickAction) {
            if (!player.hasContainerOpen()) {
                resetActions();
                return;
            }
            AbstractContainerMenu containerMenu = player.containerMenu;
            client.gameMode.handleInventoryMouseClick(
                containerMenu.containerId, clickAction.slot, 0, clickAction.clickType, player);
        } else if (action instanceof LoopAction loopAction) {
            runActionIndex = 0;
            nextRunTick = tickCounter + loopAction.delay;
        } else if (action instanceof DelayLaunchAction delayLaunchAction) {
            nextRunTick = tickCounter + delayLaunchAction.delay;
            actions = actions.subList(runActionIndex, actions.size());
        } else {
            nextRunTick = tickCounter + action.delay;
        }
        runActionIndex++;
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .then(ClientCommandManager.literal("start").executes(AutoAction::start))
                .then(ClientCommandManager.literal("reset").executes(AutoAction::stop))
                .then(ClientCommandManager.literal("delay")
                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                        .executes(AutoAction::setDelay)))
                .then(ClientCommandManager.literal("cmdAction")
                    .then(ClientCommandManager.argument("cmd", StringArgumentType.string())
                        .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                            .executes(AutoAction::addCommand))))
                .then(ClientCommandManager.literal("clickAction")
                    .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(0))
                        .then(ClientCommandManager.argument("clickType", StringArgumentType.string())
                            .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                                .executes(AutoAction::addClick)))))
                .then(ClientCommandManager.literal("delayAction")
                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                        .executes(AutoAction::addDelayLaunch)))
                .then(ClientCommandManager.literal("loopAction")
                    .executes(AutoAction::addLoop))
            );
        });
    }

    private static int start(CommandContext<FabricClientCommandSource> context) {
        if (actions.isEmpty()) {
            Message.chatMsg(T.t("auto_action_no_action"));
            return 1;
        }
        isRunning = true;
        Message.chatMsg(T.t("auto_action_start"));
        return 1;
    }

    private static void resetActions() {
        actions.clear();
        runActionIndex = 0;
        isRunning = false;
        Message.chatMsg(T.t("auto_action_reset"));
    }

    private static void sellWitherSkeletonSkull(Minecraft client) {
        if (!client.player.hasContainerOpen()) {
            return;
        }
        Inventory inventory = client.player.getInventory();
        if (!inventory.hasAnyMatching(
            (itemStack) -> ItemStackUtil.getName(itemStack).equals("凋灵骷髅头颅"))) {
            return;
        }
        AbstractContainerMenu containerMenu = client.player.containerMenu;
        client.gameMode.handleInventoryMouseClick(
            containerMenu.containerId, 20, 0, ClickType.THROW, client.player);
    }

    private static int stop(CommandContext<FabricClientCommandSource> context) {
        resetActions();
        return 1;
    }

    private static int setDelay(CommandContext<FabricClientCommandSource> context) {
        int delay = context.getArgument("delay", Integer.class);
        defaultDelay = delay;
        return 1;
    }

    private static int addCommand(CommandContext<FabricClientCommandSource> context) {
        String cmd = context.getArgument("cmd", String.class);
        int delay = context.getArgument("delay", Integer.class);
        actions.add(new CommandAction(cmd, delay));
        return 1;
    }

    private static int addClick(CommandContext<FabricClientCommandSource> context) {
        int slot = context.getArgument("slot", Integer.class);
        String clickTypeStr = context.getArgument("clickType", String.class);
        ClickType clickType = ClickType.valueOf(clickTypeStr);
        int delay = context.getArgument("delay", Integer.class);
        actions.add(new ClickAction(slot, clickType, delay));
        return 1;
    }
    

    private static int addDelayLaunch(CommandContext<FabricClientCommandSource> context) {
        int delay = context.getArgument("delay", Integer.class);
        actions.add(new DelayLaunchAction(delay));
        return 1;
    }

    private static int addLoop(CommandContext<FabricClientCommandSource> context) {
        actions.add(new LoopAction(0));
        return 1;
    }
}

class Action {
    public int delay;
}

class CommandAction extends Action {
    public String command;

    public CommandAction(String command, int delay) {
        this.command = command;
        this.delay = delay;
    }
}

class ClickAction extends Action {
    public int slot;
    public ClickType clickType;

    public ClickAction(int slot, ClickType clickType, int delay) {
        this.slot = slot;
        this.clickType = clickType;
        this.delay = delay;
    }
}

class LoopAction extends Action {
    public LoopAction(int delay) {
        this.delay = delay;
    }
}

class DelayLaunchAction extends Action {
    public DelayLaunchAction(int delay) {
        this.delay = delay;
    }
}