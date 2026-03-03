package cn.taotxi.Makemoney.module.AutoAction;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.ItemStackUtil;
import cn.taotxi.Makemoney.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

public class Commander {
    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal(AutoAction.MODULE_NAME)
            .then(ClientCommandManager.literal("list").executes(Commander::listActions))
            .then(ClientCommandManager.literal("test").executes(Commander::test))
            .then(ClientCommandManager.literal("reset").executes(Commander::reset)
                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .executes(Commander::launchActions)))
            .then(ClientCommandManager.literal("save")
                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .executes(Commander::saveActions)))
            .then(ClientCommandManager.literal("cmd")
                .then(ClientCommandManager.argument("cmd", StringArgumentType.string())
                    .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                        .executes(Commander::addCommand))))
            .then(ClientCommandManager.literal("click")
                .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(0, 53))
                    .then(ClientCommandManager.argument("clickType", StringArgumentType.string())
                        .then(ClientCommandManager.argument("delay", IntegerArgumentType.integer(1))
                            .executes(Commander::addClick)))))
            .then(ClientCommandManager.literal("cut").executes(Commander::addCut))
            .then(ClientCommandManager.literal("loop").executes(Commander::addLoop))
        );
    }

    private static int listActions(CommandContext<FabricClientCommandSource> context) {
        List<String> list = AutoAction.config.getActionNames();
        if (list.isEmpty()) {
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            return 0;
        }
        context.getSource().sendFeedback(T.tl("autoaction.list", String.join(", ", list)));
        return 1;
    }

    private static int launchActions(CommandContext<FabricClientCommandSource> context) {
        String name = context.getArgument("name", String.class);
        List<Action> actions = AutoAction.config.loadActions(name);
        if (actions == null) {
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            return 0;
        }
        AutoAction.currentActionName = name;
        AutoAction.setActions(actions);
        AutoAction.start();
        context.getSource().sendFeedback(T.tl("autoaction.launch", name));
        return 1;
    }

    private static int saveActions(CommandContext<FabricClientCommandSource> context) {
        if (AutoAction.getCurrentActions().isEmpty()) {
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            return 0;
        }
        String name = context.getArgument("name", String.class);
        AutoAction.config.toConfig(name, AutoAction.getCurrentActions());
        AutoAction.clearActions();
        context.getSource().sendFeedback(T.tl("autoaction.save", name));
        return 1;
    }

    private static int test(CommandContext<FabricClientCommandSource> context) {
        if (AutoAction.getCurrentActions().isEmpty()) {
            context.getSource().sendFeedback(T.tl("autoaction.no_action"));
            return 0;
        }
        AutoAction.setCurrentActionName("TestAction");
        AutoAction.start();
        context.getSource().sendFeedback(T.tl("autoaction.test"));
        return 1;
    }

    private static int reset(CommandContext<FabricClientCommandSource> context) {
        AutoAction.clearActions();
        return 1;
    }
    
    private static int addCommand(CommandContext<FabricClientCommandSource> context) {
        if (AutoAction.isRunning()) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }

        String cmd = context.getArgument("cmd", String.class);
        int delay = context.getArgument("delay", Integer.class);
        AutoAction.addAction(new CommandAction(cmd, delay));
        context.getSource().sendFeedback(T.tl("autoaction.add_cmd", cmd, delay));
        return 1;
    }

    private static int addClick(CommandContext<FabricClientCommandSource> context) {
        if (AutoAction.isRunning()) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }

        int slot = context.getArgument("slot", Integer.class);
        String clickTypeStr = context.getArgument("clickType", String.class);
        ClickType clickType = ClickType.valueOf(clickTypeStr);
        int delay = context.getArgument("delay", Integer.class);
        AutoAction.addAction(new ClickAction(slot, clickType, delay));
        context.getSource().sendFeedback(T.tl("autoaction.add_click", slot, clickTypeStr, delay));
        return 1;
    }
    

    private static int addCut(CommandContext<FabricClientCommandSource> context) {
        if (AutoAction.isRunning()) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }
        AutoAction.addAction(new CutAction());
        context.getSource().sendFeedback(T.tl("autoaction.add_cut"));
        return 1;
    }

    private static int addLoop(CommandContext<FabricClientCommandSource> context) {
        if (AutoAction.isRunning()) {
            context.getSource().sendFeedback(T.tl("autoaction.running_warning"));
            return 0;
        }
        AutoAction.addAction(new LoopAction());
        context.getSource().sendFeedback(T.tl("autoaction.add_loop"));
        return 1;
    }

    public static void sellWitherSkeletonSkull(Minecraft client) {
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
}
