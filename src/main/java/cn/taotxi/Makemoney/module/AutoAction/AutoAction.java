package cn.taotxi.Makemoney.module.AutoAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.ItemStackUtil;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

public class AutoAction {
    public static final String MODULE_NAME = "autoaction";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AutoActionConfig config = new AutoActionConfig(MODULE_NAME);
    private static int runActionIndex = 0;
    private static int nextRunTick = 0;
    private static boolean isRunning = false;
    private static String currentActionName = "";
    private static boolean isShowInfo = false;
    private static boolean isLoop = false;
    private static List<Action> actions = new ArrayList<>();

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        sellWitherSkeletonSkull(client);
        if (actions.isEmpty() || !isRunning) {
            return;
        }
        if (tickCounter < nextRunTick) {
            return;
        }
        if (runActionIndex >= actions.size()) {
            if (!isLoop) {
                resetActions();
                return;
            }
            runActionIndex = 0;
        }
        if (isShowInfo) {
            showInfo();
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
        } else if (action instanceof LoopAction) {
            runActionIndex = 0;
            actions = actions.subList(0, runActionIndex);
            isLoop = true;
        } else if (action instanceof CutAction) {
            if (runActionIndex+1 == actions.size()) {
                resetActions();
                return;
            }
            actions = actions.subList(runActionIndex+1, actions.size());
        } else {
            nextRunTick = tickCounter + action.delay;
        }
        runActionIndex++;
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
            .then(ClientCommandManager.literal("list").executes(AutoAction::listActions))
            .then(ClientCommandManager.literal("test").executes(AutoAction::test))
            .then(ClientCommandManager.literal("reset").executes(AutoAction::reset)
                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .executes(AutoAction::launchActions)))
            .then(ClientCommandManager.literal("save")
                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .executes(AutoAction::saveActions)))
            .then(ClientCommandManager.literal("cmd")
                .then(ClientCommandManager.argument("cmd", StringArgumentType.string())
                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                        .executes(AutoAction::addCommand))))
            .then(ClientCommandManager.literal("click")
                .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(0, 53))
                    .then(ClientCommandManager.argument("clickType", StringArgumentType.string())
                        .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                            .executes(AutoAction::addClick)))))
            .then(ClientCommandManager.literal("cut").executes(AutoAction::addCut))
            .then(ClientCommandManager.literal("loop").executes(AutoAction::addLoop))
        );
    }

    private static int listActions(CommandContext<FabricClientCommandSource> context) {
        List<String> list = config.getActionNames();
        if (list.isEmpty()) {
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            return 0;
        }
        context.getSource().sendFeedback(T.tl("autoaction.list", String.join(", ", list)));
        return 1;
    }

    private static int launchActions(CommandContext<FabricClientCommandSource> context) {
        String name = context.getArgument("name", String.class);
        actions = config.loadActions(name);
        if (actions == null) {
            currentActionName = "null";
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            actions = new ArrayList<>();
            return 0;
        }
        currentActionName = name;
        start();
        context.getSource().sendFeedback(T.tl("autoaction.launch", name));
        return 1;
    }

    private static int saveActions(CommandContext<FabricClientCommandSource> context) {
        if (actions.isEmpty()) {
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            return 0;
        }
        String name = context.getArgument("name", String.class);
        config.toConfig(name, actions);
        actions.clear();
        context.getSource().sendFeedback(T.tl("autoaction.save", name));
        return 1;
    }

    private static int test(CommandContext<FabricClientCommandSource> context) {
        if (actions.isEmpty()) {
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            return 0;
        }
        currentActionName = "TestAction";
        start();
        context.getSource().sendFeedback(T.tl("autoaction.test"));
        return 1;
    }

    private static void start() {
        isRunning = true;
        runActionIndex = 0;
        nextRunTick = 0;
        isLoop = false;
    }

    private static void resetActions() {
        actions.clear();
        isRunning = false;
        Message.chatMsg(T.tl("autoaction.reset"));
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
        if (!(containerMenu instanceof ChestMenu)) {
            return;
        }
        client.gameMode.handleInventoryMouseClick(
            containerMenu.containerId, 20, 0, ClickType.THROW, client.player);
    }

    private static int reset(CommandContext<FabricClientCommandSource> context) {
        resetActions();
        return 1;
    }

    private static void showInfo() {
        String info = String.format("Action: %s, Index: %d, Delay: %d",
            currentActionName, runActionIndex, actions.get(runActionIndex).delay);
        Message.actionBarMsg(T.l(info));
    }
    
    private static int addCommand(CommandContext<FabricClientCommandSource> context) {
        if (isRunning) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }

        String cmd = context.getArgument("cmd", String.class);
        int delay = context.getArgument("delay", Integer.class);
        actions.add(new CommandAction(cmd, delay));
        context.getSource().sendFeedback(T.tl("autoaction.add_cmd", cmd, delay));
        return 1;
    }

    private static int addClick(CommandContext<FabricClientCommandSource> context) {
        if (isRunning) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }

        int slot = context.getArgument("slot", Integer.class);
        String clickTypeStr = context.getArgument("clickType", String.class);
        ClickType clickType = ClickType.valueOf(clickTypeStr);
        int delay = context.getArgument("delay", Integer.class);
        actions.add(new ClickAction(slot, clickType, delay));
        context.getSource().sendFeedback(T.tl("autoaction.add_click", slot, clickTypeStr, delay));
        return 1;
    }
    

    private static int addCut(CommandContext<FabricClientCommandSource> context) {
        if (isRunning) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }
        actions.add(new CutAction());
        context.getSource().sendFeedback(T.tl("autoaction.add_cut"));
        return 1;
    }

    private static int addLoop(CommandContext<FabricClientCommandSource> context) {
        if (isRunning) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }
        actions.add(new LoopAction());
        context.getSource().sendFeedback(T.tl("autoaction.add_loop"));
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
    public LoopAction() {
        this.delay = 0;
    }
}

class CutAction extends Action {
    public CutAction() {
        this.delay = 0;
    }
}