package cn.taotxi.Makemoney.module.Test;

import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;


public class Test {
    private static final Minecraft client = Minecraft.getInstance();

    public static void initialize() {
        // TaskUtil.createTimeTask("tttt", () -> {
        //     System.out.println(client.screen.getClass().getName());
        // }, 20);



        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = ClientCommands.literal("tt")
                .then(ClientCommands.literal("1")
                    .executes(context -> {
                        var camera = client.getCameraEntity();
                        System.out.println(camera);
                        System.out.println(camera.position());

                        System.out.println();
                        System.out.println(client.player);
                        System.out.println(client.player.position());

                        return 1;
                    }))
                .then(ClientCommands.literal("2")
                    .executes(context -> {
                        var it = client.level.entitiesForRendering();
                        for (Entity e : it) {
                            e.setGlowingTag(false);
                        }

                        return 1;
                    }))
                .then(ClientCommands.literal("clear")
                    .executes(context -> {
                        TaskUtil.removeTimeTask("tt1");
                        TaskUtil.removeTimeTask("tt2");
                        return 1;
                    }));

            // dispatcher.register(cmd);
        });
    }
}
