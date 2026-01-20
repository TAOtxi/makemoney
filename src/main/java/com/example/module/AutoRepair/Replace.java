package com.example.module.AutoRepair;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import com.example.util.T;
import com.example.util.SwapSlot;
import com.example.common.Code;
import com.example.util.Message;


public class Replace {
    private static int tickCounter = 0;
    private static Minecraft client = Minecraft.getInstance();
    public static void tryToReplace() {
        if (!AutoRepair.config.replaceEnabled)
            return;

        Player player = client.player;

        if (++tickCounter < AutoRepair.config.checkExpInterval) return;
        tickCounter = 0;

        ItemStack offHandItem = player.getOffhandItem();
        // Check if there is experience in repairing and if the durability is not satisfactory
        // Check if there are any experience balls around
        if (canRepair(offHandItem) || !checkExpAround(client)) return;
        
        int canReplaceSlot = findItemToRepair(player);
        if (canReplaceSlot == Code.NOT_FOUND) return;
        
        ItemStack itemToRepair = player.inventoryMenu.getSlot(canReplaceSlot).getItem();
        if (AutoRepair.config.showMessage) {
            Message.actionBarMsg(
                T.t("makemoney.autorepair.message.repair", 
                itemToRepair.getHoverName().getString())
            );
        }

        AutoRepair.LOGGER.info(
            "swap item to offhand with slot {} name {}", 
            canReplaceSlot, 
            itemToRepair.getHoverName().getString()
        );
        playSwapSound(client);
        SwapSlot.swaper(canReplaceSlot, InventoryMenu.SHIELD_SLOT);
    }

    private static boolean canRepair(ItemStack item) {
        if (item.isEmpty() || !item.isDamaged()) return false;
        return hasEnchantment(item, Enchantments.MENDING);
    }

    public static boolean hasEnchantment(ItemStack item, ResourceKey<Enchantment> enchantment) {
        if (!item.is(Items.ENCHANTED_BOOK)) {
            for (Holder<Enchantment> enchantmentHolder : item.getEnchantments().keySet()) {
                if (enchantmentHolder.is(enchantment)) {
                    return true;
                }
            }
        } else {
            for (Holder<Enchantment> enchantmentHolder : 
                item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).keySet()) {
                if (enchantmentHolder.is(enchantment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int findItemToRepair(Player player) {
        InventoryMenu inventoryMenu = player.inventoryMenu;
        
        int mainHandSlot = player.getInventory().getSelectedSlot() + InventoryMenu.USE_ROW_SLOT_START;
        for (int i=InventoryMenu.ARMOR_SLOT_END; i<InventoryMenu.USE_ROW_SLOT_END; i++) {
            if (i == mainHandSlot) continue;
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            if (canRepair(item)) {
                return i;
            }
        }
        return Code.NOT_FOUND;
    }

    private static boolean checkExpAround(Minecraft client) {
        AABB searchBox = client.player.getBoundingBox().inflate(AutoRepair.config.expCheckBound);
        List<Entity> entities = client.level.getEntities(client.player, searchBox, (Entity entity) -> {
            return entity instanceof ExperienceOrb;
        });
        return !entities.isEmpty();
    }

    private static void playSwapSound(Minecraft client) {
        client.level.playPlayerSound(
            SoundEvents.ARMOR_EQUIP_LEATHER.value(),
            net.minecraft.sounds.SoundSource.PLAYERS,
            0.5F,
            1.0F
        );
    }
}
