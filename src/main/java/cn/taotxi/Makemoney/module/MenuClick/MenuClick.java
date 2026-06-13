package cn.taotxi.Makemoney.module.MenuClick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.util.MLogger;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MenuClick {
    public static final String MODULE_NAME = "menuclick";
    public static final MLogger logger = new MLogger(MODULE_NAME);
    private static final Minecraft client = Minecraft.getInstance();
    private static final MenuClickConfig CONFIG = MenuClickConfig.getInstance();
    private static final Map<String, Integer> taskMap = new HashMap<>();
    private static final String CONTROL_LISTENER = "control_listener";


    public static void initialize() {
        CONFIG.loadConfig();
        registCommand();
    }

    private static void runTask(String name) {
        if (taskMap.containsKey(name)) {
            Message.clientSideMsg(T.tl("menuClick.isRunning.message", name));
            return;
        }

        MenuClickTask task = CONFIG.getTask(name);
        if (task == null) {
            Message.clientSideMsg(T.tl("menuClick.notFound.message", name));
            return;
        }

        if (task.actions.isEmpty()) {
            Message.clientSideMsg(T.tl("menuClick.noAction.message", name));
            return;
        }

        if (!TaskUtil.hasTimeTask(CONTROL_LISTENER)) {
            TaskUtil.createTimeTask(CONTROL_LISTENER, () -> {
                if (!client.hasControlDown()) return;

                for (String taskName : taskMap.keySet()) {
                    cancelTask(taskName);
                }
            }, 1);
        }

        taskMap.put(name, 0);
        String taskName = createTaskName(name);

        List<TaskAction> taskActions = new ArrayList<>();
        for (int i = 0; i < task.actions.size(); i++) {
            taskActions.add(task.getAction(i));
        }
        
        TaskUtil.createOnceTimeTask(createStartTaskName(name), () -> {
            TaskUtil.createTimeTask(taskName, () -> {
                if (client.player == null) {
                    cancelAllTask();
                    return;
                }

                int i = taskMap.get(name);
                if (i >= taskActions.size()) {
                    if (task.isLoop) {
                        taskMap.put(name, 0);
                        i = 0;
                    } else {
                        cancelTask(name);
                        return;
                    }
                }
                
                TaskAction currentAction = taskActions.get(i);
                if (currentAction.isClick()) {
                    if (!(client.player.containerMenu instanceof ChestMenu chestMenu)) {
                        cancelTask(name);
                        return;
                    }

                    client.gameMode.handleInventoryMouseClick(
                        chestMenu.containerId, 
                        currentAction.slot, 
                        currentAction.button, 
                        currentAction.clickType, 
                        client.player
                    );
                } else if (currentAction.isCommand()) {
                    Message.sendMessage(currentAction.command);
                } else {
                    throw new IllegalArgumentException("Unknown action type: " + task.name);
                }
                taskMap.put(name, i + 1);

            }, task.delay, true);
        }, task.startDelay);
    }

    private static String createTaskName(String name) {
        return MODULE_NAME + "_" + name;
    }

    private static String createStartTaskName(String name) {
        return MODULE_NAME + "_start_" + name;
    }

    private static void cancelTask(String name) {
        taskMap.remove(name);
        TaskUtil.removeTimeTask(createTaskName(name));
        TaskUtil.removeTimeTask(createStartTaskName(name));

        if (taskMap.isEmpty()) {
            TaskUtil.removeTimeTask(CONTROL_LISTENER);
        }
    }

    private static void cancelAllTask() {
        for (String taskName : taskMap.keySet()) {
            cancelTask(taskName);
            Message.clientSideMsg(T.tl("menuClick.cancel.message", taskName));
        }
    }

    private static void registCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .executes(MenuClick::showHelp)
                .then(ClientCommandManager.literal("help")
                    .executes(MenuClick::showHelp))
                .then(ClientCommandManager.literal("config")
                    .executes(context -> {
                        GuiUtil.openYaclScreen(Makemoney.MOD_ID, 4);
                        return 1;
                    }))
                .then(ClientCommandManager.literal("run")
                    .then(ClientCommandManager.argument("task", StringArgumentType.string())
                        .suggests(MenuClick::suggestTaskNames)
                        .executes(context -> {
                            String task = context.getArgument("task", String.class);
                            runTask(task);
                            return 1;
                        }))
                    )
            );
            
            dispatcher.register(ClientCommandManager.literal("click")
                    .executes(MenuClick::showHelp)
                    .redirect(cmd));
        });
    }

    private static CompletableFuture<Suggestions> suggestTaskNames(
        CommandContext<FabricClientCommandSource> context,
        SuggestionsBuilder builder
    ) {
        List<String> taskNameList = CONFIG.getTaskNameList();
        for (String name : taskNameList) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("menuclick.help.message"));
        return 1;
    }
}
