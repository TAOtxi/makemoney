package cn.taotxi.Makemoney.module.Task;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class TaskEntry {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("task")
                .then(ClientCommandManager.literal(DropItemInShulkerBox.TASK_NAME)
                    .then(ClientCommandManager.literal("on")
                        .executes(DropItemInShulkerBox::enable))
                    .then(ClientCommandManager.literal("off")
                        .executes(DropItemInShulkerBox::disable)))
            );
        });
    }
}
