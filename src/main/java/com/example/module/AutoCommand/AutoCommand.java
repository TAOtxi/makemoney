package com.example.module.AutoCommand;


import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import com.example.util.MLogger;
import com.example.util.Message;
import com.example.util.TickCounter;


public class AutoCommand {
    public static final String MODULE_NAME = "autoReconnect";
    private static final Minecraft client = Minecraft.getInstance();
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static final TickCounter tickCounter = new TickCounter();
    public static final AutoCommandConfig config = new AutoCommandConfig(MODULE_NAME);


    public static void init() {
        updateTickCounter();
        registerTickEvents();
    }

    public static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // TODO: 待测试
            // 判断是否是多人游戏
            if (Minecraft.getInstance().getCurrentServer() == null) {
                LOGGER.info("当前不是多人游戏");
                return;
            }

            if (!config.enabled) return;
            if (client.level == null || client.player == null) return;
        });
    }

    public static void updateTickCounter() {
        if (client.level == null) return;
        for (CommandBlock block : config.commandBlocks) {
            if (!block.enabled) {
                tickCounter.removeTask(block.id);
                continue;
            }
            TickCounter.Task task = tickCounter.getTask(block.id);
            if (task == null) {
                TickCounter.Task newTask = new TickCounter.Task(
                    block.id,
                    (tick) -> {
                        if (client.level == null) {
                            throw new IllegalStateException("Client level is null");
                        }

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
                block.isUpdate = false;
                block.cmdPtr = 0;
                task.delay = block.delay;
                task.runCounts = block.runCounts * block.commands.size();
                task.lastRunCounts = block.runCounts * block.commands.size();
            }
        }
    }


}
