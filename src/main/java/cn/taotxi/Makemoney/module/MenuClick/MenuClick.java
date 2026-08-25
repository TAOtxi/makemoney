package cn.taotxi.Makemoney.module.MenuClick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ChestMenu;

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

    private static void runTask(String taskName) {
        if (taskMap.containsKey(taskName)) {
            Message.clientSideMsg(T.tl("menuClick.isRunning.message", taskName));
            return;
        }

        MenuClickTask task = CONFIG.getTask(taskName);
        if (task == null) {
            Message.clientSideMsg(T.tl("menuClick.notFound.message", taskName));
            return;
        }

        if (task.actions.isEmpty()) {
            Message.clientSideMsg(T.tl("menuClick.noAction.message", taskName));
            return;
        }

        if (!TaskUtil.hasTimeTask(CONTROL_LISTENER)) {
            TaskUtil.createTimeTask(CONTROL_LISTENER, () -> {
                if (!client.hasControlDown()) return;

                for (String _taskName : taskMap.keySet()) {
                    cancelTask(_taskName);
                    Message.clientSideMsg(T.tl("menuClick.cancel.message", _taskName));
                }
            }, 1);
        }

        taskMap.put(taskName, 0);
        String taskNameWithPrefix = createTaskName(taskName);

        List<TaskAction> taskActions = new ArrayList<>();
        for (int i = 0; i < task.actions.size(); i++) {
            taskActions.add(task.getAction(i));
        }
        
        Message.clientSideMsg(T.tl("menuClick.start.message", taskName));
        TaskUtil.createTimeTask(taskNameWithPrefix, () -> {
            if (client.player == null) {
                cancelAllTask();
                return;
            }

            int i = taskMap.get(taskName);
            if (i >= taskActions.size()) {
                taskMap.put(taskName, 0);
                i = 0;
            }   
            
            TaskAction currentAction = taskActions.get(i);
            boolean success = execute(currentAction, taskName);
            if (!success) {
                cancelTask(taskName);
                return;
            }

            int nextIndex = i + 1;

            while (currentAction.delay == 0) {
                if (nextIndex >= taskActions.size()) {
                    if (task.isLoop) nextIndex = 0;
                    break;
                }
                currentAction = taskActions.get(nextIndex);
                success = execute(currentAction, taskName);
                if (!success) {
                    cancelTask(taskName);
                    return;
                }

                nextIndex++;
            }
            
            if (nextIndex >= taskActions.size() && !task.isLoop) {
                cancelTask(taskName);
                Message.clientSideMsg(T.tl("menuClick.finish.message", taskName));
                return;
            }
            taskMap.put(taskName, nextIndex);
            
            int nextDelay = currentAction.delay == -1 ? task.delay : currentAction.delay;
            TaskUtil.updateTimeTask(taskNameWithPrefix, Math.max(1, nextDelay));
            TaskUtil.resetNextRunTick(taskNameWithPrefix);
        }, Math.max(1, task.startDelay));
    }

    private static boolean execute(TaskAction action, String taskName) {
        if (action.isClick()) {
            if (!(client.player.containerMenu instanceof ChestMenu chestMenu)) {
                Message.clientSideMsg(T.tl("menuClick.break.message", taskName));
                return false;
            }

            client.gameMode.handleContainerInput(
                chestMenu.containerId, 
                action.slot, 
                action.button, 
                action.clickType, 
                client.player
            );
        } else if (action.isCommand()) {
            Message.sendMessage(action.command);
        } else {
            throw new IllegalArgumentException("Unknown action: " + taskName);
        }
        return true;
    }

    private static String createTaskName(String name) {
        return MODULE_NAME + "_" + name;
    }

    private static void cancelTask(String name) {
        taskMap.remove(name);
        TaskUtil.removeTimeTask(createTaskName(name));

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
            var cmd = dispatcher.register(ClientCommands.literal(MODULE_NAME)
                .executes(MenuClick::showHelp)
                .then(ClientCommands.literal("help")
                    .executes(MenuClick::showHelp))
                .then(ClientCommands.literal("config")
                    .executes(context -> {
                        GuiUtil.openYaclScreen(Makemoney.MOD_ID, MODULE_NAME);
                        return 1;
                    }))
                .then(ClientCommands.literal("run")
                    .then(ClientCommands.argument("task", StringArgumentType.string())
                        .suggests(MenuClick::suggestTaskNames)
                        .executes(context -> {
                            String task = context.getArgument("task", String.class);
                            runTask(task);
                            return 1;
                        }))
                    )
            );
            
            dispatcher.register(ClientCommands.literal("click")
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
        context.getSource().sendFeedback(T.tl("menuClick.help.message"));
        return 1;
    }
}
