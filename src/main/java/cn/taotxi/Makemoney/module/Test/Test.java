package cn.taotxi.Makemoney.module.Test;

import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

public class Test {
    private static final Minecraft client = Minecraft.getInstance();

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("tt")
                .then(ClientCommandManager.literal("1")
                    .executes(context -> {
                        TaskUtil.createTimeTask("tt1", () -> {
                            if (client.player == null) return;
                            var containerMenu = client.player.containerMenu;
                            if (!(containerMenu instanceof ChestMenu)) return;

                            client.gameMode.handleInventoryMouseClick(
                                containerMenu.containerId,
                                20, 0, ClickType.THROW, client.player
                            );

                        }, 1);

                        return 1;
                    }))
                .then(ClientCommandManager.literal("2")
                    .executes(context -> {
                        TaskUtil.createTimeTask("tt2", () -> {
                            if (client.player == null) return;
                            var containerMenu = client.player.containerMenu;
                            if (!(containerMenu instanceof ChestMenu)) return;

                            client.gameMode.handleInventoryMouseClick(
                                containerMenu.containerId,
                                20, 0, ClickType.THROW, client.player
                            );
                            client.gameMode.handleInventoryMouseClick(
                                containerMenu.containerId,
                                20, 0, ClickType.THROW, client.player
                            );

                        }, 1);

                        return 1;
                    }))
                .then(ClientCommandManager.literal("clear")
                    .executes(context -> {
                        TaskUtil.removeTimeTask("tt1");
                        TaskUtil.removeTimeTask("tt2");
                        return 1;
                    }))
            );
        });
    }
}
