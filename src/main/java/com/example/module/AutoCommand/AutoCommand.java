package com.example.module.AutoCommand;


import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import com.example.util.MLogger;
import com.example.util.Message;
import com.example.util.TickCounter;
import com.example.module.AutoCommand.AutoCommandConfig.CommandBlock;


public class AutoCommand {
    public static boolean isGame = false;
    public static final String MODULE_NAME = "autocommand";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static final TickCounter ticker = new TickCounter();
    public static final AutoCommandConfig config = AutoCommandConfig.load(AutoCommandConfig.class, MODULE_NAME);


    public static void init() {
        // updateTickCounter();
        // registerTickEvents();
    }

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        // ticker.addTask(new TickCounter.Task(tick -> {
        //     LOGGER.info("ticking...");
        // }, 40));
        // 判断是否是多人游戏
        // if (Minecraft.getInstance().getCurrentServer() == null) {
        //     LOGGER.info("当前不是多人游戏");
        //     return;
        // }
        // if (client.level == null || client.player == null) {
        //     isGame = false;
        //     return;
        // };
        // ticker.run();
        // if (!isGame) {
        //     isGame = true;
        //     ticker.clear();
        //     updateTickCounter();
        // }
        if (!config.enabled) return;
        // ticker.run();
    }

    public static void updateTickCounter() {
        Minecraft client = Minecraft.getInstance();
        for (CommandBlock block : config.commandBlocks) {
            if (!block.enabled) {
                ticker.removeTask(block.id);
                continue;
            }
            TickCounter.Task task = ticker.getTask(block.id);
            if (task == null) {
                block.cmdPtr = 0;
                block.isUpdate = false;
                TickCounter.Task newTask = new TickCounter.Task(
                    block.id,
                    (tick) -> {
                        String ip = client.getCurrentServer().ip;
                        if (!ip.equals(block.ip) && !block.ip.equals("*")) {
                            return;
                        }
                        String worldName = client.level.dimension().location().toString();
                        if (!worldName.equals(block.worldName) && !block.worldName.equals("*")) {
                            return;
                        }

                        String command = block.commands.get(block.cmdPtr % block.commands.size());
                        Message.sendMessage(command);
                        block.cmdPtr++;
                    },
                    block.delay,
                    block.runCounts * block.commands.size()
                );
                ticker.addTask(newTask);
            } else if (block.isUpdate) {    // 配置界面修改config时， 会令当前的isUpdate为true
                block.cmdPtr = 0;
                block.isUpdate = false;
                task.delay = block.delay;
                task.runCounts = block.runCounts * block.commands.size();
                task.lastRunCounts = task.runCounts;
            }
        }
    }


}
