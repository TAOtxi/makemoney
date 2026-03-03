package cn.taotxi.Makemoney.module.AutoAction;

import java.util.List;
import java.util.ArrayList;

import com.mojang.brigadier.CommandDispatcher;

import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class AutoAction {
    public static final String MODULE_NAME = "autoaction";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AutoActionConfig config = new AutoActionConfig(MODULE_NAME);
    public static String currentActionName = "";
    public static List<Action> actions = new ArrayList<>();
    private static int runActionIndex = 0;
    private static int nextRunTick = 0;
    private static boolean isRunning = false;
    private static boolean isShowInfo = false;
    private static boolean isLoop = false;

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        Commander.sellWitherSkeletonSkull(client);
        if (actions.isEmpty() || !isRunning) {
            isRunning = false;
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
            LOGGER.info("Unknown action type: " + action.getClass().getSimpleName());
            nextRunTick = tickCounter + action.delay;
        }
        runActionIndex++;
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {
        Commander.registerCommand(dispatcher, registryAccess);
    }

    public static void start() {
        isRunning = true;
        runActionIndex = 0;
        nextRunTick = 0;
        isLoop = false;
    }

    public static void resetActions() {
        actions.clear();
        isRunning = false;
        Message.chatMsg(T.tl("autoaction.reset"));
    }

    public static List<Action> getCurrentActions() {
        return actions;
    }

    public static void setActions(List<Action> actions) {
        AutoAction.actions = actions;
    }

    public static void clearActions() {
        actions.clear();
    }

    public static void setCurrentActionName(String name) {
        currentActionName = name;
    }

    private static void showInfo() {
        String info = String.format("Action: %s, Index: %d, Delay: %d",
            currentActionName, runActionIndex, actions.get(runActionIndex).delay);
        Message.actionBarMsg(T.l(info));
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static void addAction(Action action) {
        actions.add(action);
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