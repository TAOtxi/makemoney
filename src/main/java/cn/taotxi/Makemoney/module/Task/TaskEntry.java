package cn.taotxi.Makemoney.module.Task;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class TaskEntry {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("task")
                .then(ClientCommands.literal(DropItemInShulkerBox.TASK_NAME)
                    .then(ClientCommands.literal("on")
                        .executes(DropItemInShulkerBox::enable))
                    .then(ClientCommands.literal("off")
                        .executes(DropItemInShulkerBox::disable)))
            );
        });
    }
}
