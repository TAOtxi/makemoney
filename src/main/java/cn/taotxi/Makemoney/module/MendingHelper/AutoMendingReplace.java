package cn.taotxi.Makemoney.module.MendingHelper;

import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.game.EnchantmentHelper;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class AutoMendingReplace {
    private static final Minecraft client = Minecraft.getInstance();
    private static final MendingHelperConfig CONFIG = MendingHelperConfig.getInstance();
    private static boolean throttleFlag = true;
    private static final String MENDING_HELPER_AUTO_REPLACE_THROTTLE_KEY = "mendingHelper_autoReplace_throttle";
    public static boolean enable = true;

    public static void initialize() {
        enable = CONFIG.autoReplaceEnabled.getValue();
        CONFIG.autoReplaceEnabled.onChange(
            (oldValue, newValue) -> AutoMendingReplace.enable = newValue
        );
        CONFIG.autoReplaceEnabled.triggerConfigChange();
    }

    public static void tryToReplaceOffHand() {
        if (!enable || !throttleFlag) return;

        if (client.player.hasContainerOpen()) return;
        throttleFlag = false;
        TaskUtil.createOnceTimeTask(
            MENDING_HELPER_AUTO_REPLACE_THROTTLE_KEY, () -> throttleFlag = true, 10);
        
        ItemStack offhandItem = client.player.getOffhandItem();
        if (shouldAtOffhand(offhandItem)) {
            return;
        }

        int targetSlot = -1;
        InventoryMenu inventoryMenu = client.player.inventoryMenu;
        int mainHandSlot = client.player.getInventory().getSelectedSlot() + InventoryMenu.USE_ROW_SLOT_START;
        for (int i=InventoryMenu.INV_SLOT_START; i<InventoryMenu.USE_ROW_SLOT_END; i++) {
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            if (i == mainHandSlot) continue;
            if (shouldAtOffhand(item)) {
                targetSlot = i;
                break;
            }
        }
        if (targetSlot == -1) return;

        InventoryUtil.swapItemToHand(InteractionHand.OFF_HAND, targetSlot);

        client.level.playPlayerSound(
            SoundEvents.ARMOR_EQUIP_LEATHER.value(),
            net.minecraft.sounds.SoundSource.PLAYERS,
            0.5F,
            1.0F
        );
    }

    public static boolean shouldAtOffhand(ItemStack item) {
        if (item.isEmpty()) return false;
        if (item.is(Items.ENCHANTED_BOOK)) return false;
        boolean hasMending = EnchantmentHelper.hasEnchantment(item, Enchantments.MENDING);
        return hasMending && item.isDamaged();
    }
}
