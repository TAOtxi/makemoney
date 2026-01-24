package com.example.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import com.example.Makemoney;
import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoDrop.AutoDrop;
import com.example.module.AutoDrop.AutoDropConfig;
import com.example.module.AutoDrop.Dropper;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.EntityHighlightBox.EntityHighlightBox;
import com.example.util.ItemStackUtil;
import com.example.util.Message;
import com.example.util.T;

public class TestMod {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("trydrop").executes(context -> {
                context.getSource().sendFeedback(T.l("Called /trydrop with no arguments."));
                Dropper.tryToDropItems();
                return 1;
            }));

            LiteralArgumentBuilder<FabricClientCommandSource> look = ClientCommandManager.literal("look");

            List<String> direction = AutoDropConfig.getAllThrowDirections();
            for (String d : direction) {
                look.then(ClientCommandManager.literal(d))
                        .executes(context -> {
                            context.getSource().sendFeedback(T.l("This is an feedback."));
                            Dropper.setPlayerRotation(d);
                            return 1;
                        });
            }

            LiteralArgumentBuilder<FabricClientCommandSource> drop = ClientCommandManager.literal("drop");

            for (int i = 0; i < 36; i++) {
                final int slot = i;
                drop.then(ClientCommandManager.literal(String.valueOf(i)))
                        .executes(context -> {
                            Dropper.dropItemAnywhere(slot, AutoDropConfig.Direction.EAST);
                            return 1;
                        });
            }

            LiteralArgumentBuilder<FabricClientCommandSource> showhand = ClientCommandManager.literal("showhand")
                    .executes(context -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) {
                            context.getSource().sendFeedback(T.l("Player is null."));
                            return 0;
                        }
                        ItemStack item = player.getMainHandItem();
                        // Message.chatMsg("\n");
                        // Message.chatMsg("\nItem: ", item.toString());
                        // Message.chatMsg("\nItemId: ", id);
                        // Message.chatMsg("\nItemName: ", item.getItemName());
                        // Message.chatMsg("\nHoverName: ", item.getHoverName());
                        // Message.chatMsg("\nCustomName: ", item.getCustomName());
                        // Message.chatMsg("\nDisplayName: ", item.getDisplayName());
                        // Message.chatMsg("\nStyledHoverName: ", item.getStyledHoverName());
                        // Message.chatMsg("\nItemTags: ", item.getTags().toString());

                        Makemoney.LOGGER.info("Item: {}", item.toString());
                        Makemoney.LOGGER.info("ItemId: {}", item.getItem().toString());
                        Makemoney.LOGGER.info("ItemName: {}", item.getItemName());
                        Makemoney.LOGGER.info("HoverName: {}", item.getHoverName());
                        Makemoney.LOGGER.info("CustomName: {}", item.getCustomName());
                        Makemoney.LOGGER.info("DisplayName: {}", item.getDisplayName());
                        Makemoney.LOGGER.info("StyledHoverName: {}", item.getStyledHoverName());
                        Makemoney.LOGGER.info("ItemTags: {}", item.getTags().map(tagKey -> "#" + tagKey.location().toString()).toList());

                        // Makemoney.LOGGER.info("ItemName: {}", ItemStackUtil.getName(item));
                        // Makemoney.LOGGER.info("ItemID: {}", ItemStackUtil.getId(item));
                        // Makemoney.LOGGER.info("ItemTags: {}", item.getTags().map(tagKey -> "#" + tagKey.location().toString()).toList());

                        return 1;
                    });

            dispatcher.register(look);
            dispatcher.register(drop);
            dispatcher.register(showhand);

            dispatcher.register(ClientCommandManager.literal("removeConfig").executes(context -> {
                context.getSource().sendFeedback(T.l("Called /removeConfig with no arguments."));
                
                EntityHighlightBox.config.remove();
                AutoDrop.config.remove();
                AutoRepair.config.remove();
                AutoCommand.config.remove();
                return 1;
            }));
        });
    }

}