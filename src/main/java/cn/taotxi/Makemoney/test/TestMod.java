package cn.taotxi.Makemoney.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfig;
import cn.taotxi.Makemoney.module.AutoDrop.Dropper;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepair;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBox;
import cn.taotxi.Makemoney.util.EntityUtil;
import cn.taotxi.Makemoney.util.ItemStackUtil;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.projectile.FishingHook;

public class TestMod {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
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
            // dispatcher.register(showhand);

            
            LiteralArgumentBuilder<FabricClientCommandSource> showinfo = ClientCommandManager.literal("showinfo").executes(context -> {
                context.getSource().sendFeedback(T.l("Called Show Command"));
                return 1;
            }).then(ClientCommandManager.literal("entity").executes(context -> {
                context.getSource().sendFeedback(T.l("Show entity information"));
                Minecraft client = context.getSource().getClient();
                
                LocalPlayer player = client.player;
                AABB box = player.getBoundingBox().inflate(10);
                List<Entity> list = client.level.getEntities(player, box);
                for (Entity entity : list) {
                    if (entity instanceof LocalPlayer) continue;
                }
                return 1;
            }));
            dispatcher.register(showinfo);
        });
    }

}