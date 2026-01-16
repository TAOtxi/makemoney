package com.example.module.AutoRepair;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;

import java.util.List;

import com.example.util.T;
import com.example.util.SwapSlot;
import com.example.util.Message;

public class Mending {
    public static void tryToRepair(Minecraft client) {
        if (client.level == null || client.player == null) return;

        ItemStack offHandItem = client.player.getOffhandItem();
        // Check if there is experience in repairing and if the durability is not satisfactory
        if (!canRepair(offHandItem)) return;
        // Check if there are any experience balls around
        if (!checkExpAround(client)) return;

        int canRepairSlot = findItemToRepair(client.player);
        if (canRepairSlot == Inventory.NOT_FOUND_INDEX) return;
        
        ItemStack itemToRepair = client.player.getInventory().getItem(canRepairSlot);
        if (AutoRepair.config.getBoolean("showMessage")) {
            Message.subTitleMsg(
                T.t("makemoney.autorepair.message.repair", 
                itemToRepair.getHoverName().getString())
            );
        }

        AutoRepair.LOGGER.info(
            "[{}] swap item to offhand with slot {} name {}", 
            AutoRepair.MODULE_NAME,
            canRepairSlot, 
            itemToRepair.getHoverName().getString()
        );
        playSwapSound(client);
        SwapSlot.swapSlot(client.player, canRepairSlot, Inventory.SLOT_OFFHAND);
    }

    private static Boolean canRepair(ItemStack item) {
        if (item.isEmpty() || !item.isDamageableItem()) return false;
        
        for (Holder<Enchantment> enchantment : item.getEnchantments().keySet()) {
            if (enchantment.is(Enchantments.MENDING) && item.getDamageValue() > 0) {
                return true;
            }
        }
        return false;
    }

    private static int findItemToRepair(Player player) {
        Inventory inventory = player.getInventory();
        
        int mainHandSlot = inventory.getSelectedSlot();
        for (int i=0; i<Inventory.INVENTORY_SIZE; i++) {
            if (i == mainHandSlot) continue;
            ItemStack item = inventory.getItem(i);
            if (canRepair(item)) {
                return i;
            }
        }
        return Inventory.NOT_FOUND_INDEX;
    }

    private static Boolean checkExpAround(Minecraft client) {
        AABB searchBox = client.player.getBoundingBox().inflate(AutoRepair.config.getDouble("expCheckBound"));
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
