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
    public static final Minecraft client = Minecraft.getInstance();
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static final TickCounter tickCounter = new TickCounter();
    public static final AutoCommandConfig config = AutoCommandConfig.load(AutoCommandConfig.class, MODULE_NAME);


    public static void init() {
        updateTickCounter();
        registerTickEvents();
    }

    public static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 判断是否是多人游戏
            // if (Minecraft.getInstance().getCurrentServer() == null) {
            //     LOGGER.info("当前不是多人游戏");
            //     return;
            // }
            if (client.level == null || client.player == null) {
                isGame = false;
                return;
            };
            if (!isGame) {
                isGame = true;
                tickCounter.clear();
                updateTickCounter();
            }
            if (!config.enabled) return;
            tickCounter.run();
        });
    }

    public static void updateTickCounter() {
        for (CommandBlock block : config.commandBlocks) {
            if (!block.enabled) {
                tickCounter.removeTask(block.id);
                continue;
            }
            TickCounter.Task task = tickCounter.getTask(block.id);
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
                tickCounter.addTask(newTask);
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
