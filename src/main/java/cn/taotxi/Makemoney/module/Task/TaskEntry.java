package cn.taotxi.Makemoney.module.Task;

import cn.taotxi.Makemoney.util.help.HelpMenu;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class TaskEntry {
    public static final String MODULE_NAME = "task";
    private static final HelpMenu HELP = HelpMenu.of(MODULE_NAME, "task.help")
        .entry(DropItemInShulkerBox.TASK_NAME + " on", "task.help.dropShulkerOn")
        .entry(DropItemInShulkerBox.TASK_NAME + " off", "task.help.dropShulkerOff")
        .build();

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal(MODULE_NAME)
                .executes(HELP::executeFirstPage)
                .then(HELP.helpCommand())
                .then(ClientCommands.literal(DropItemInShulkerBox.TASK_NAME)
                    .then(ClientCommands.literal("on")
                        .executes(DropItemInShulkerBox::enable))
                    .then(ClientCommands.literal("off")
                        .executes(DropItemInShulkerBox::disable)))
            );
        });
    }
}
