package cn.taotxi.Makemoney.module.MendingHelper;

import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.game.EnchantmentHelper;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class AutoEnchantMending {
    private static final Minecraft client = Minecraft.getInstance();
    private static final MendingHelperConfig CONFIG = MendingHelperConfig.getInstance();
    private static final String AUTO_ENCHANT_MENDING_TASK = "autoEnchantMendingTask";
    private static Enchantment mending;

    public static void onOpenAnvil() {
        if (!(client.player.containerMenu instanceof AnvilMenu)) {
            return;
        }
        if (!CONFIG.autoEnchantEnabled.getValue()) {
            return;
        }

        if (!TaskUtil.hasTimeTask(AUTO_ENCHANT_MENDING_TASK)) {
            TaskUtil.createTimeTask(
                AUTO_ENCHANT_MENDING_TASK, 
                AutoEnchantMending::tryToEnchantMendingForEquipment, 
                1
            );
        }
    }

    private static void tryToEnchantMendingForEquipment() {
        if (!(client.player.containerMenu instanceof AnvilMenu anvilMenu)) {
            TaskUtil.removeTimeTask(AUTO_ENCHANT_MENDING_TASK);
            AutoRepair.stopRepairing();
            return;
        }

        boolean isInputSlotValid = true;
        boolean isAdditionSlotValid = true;

        ItemStack inputItem = anvilMenu.getSlot(AnvilMenu.INPUT_SLOT).getItem();
        if (!shouldAtSlot1(inputItem)) {
            client.gameMode.handleInventoryMouseClick(
                anvilMenu.containerId, AnvilMenu.INPUT_SLOT, 1, ClickType.THROW, client.player);
            isInputSlotValid = false;
        }

        ItemStack additionItem = anvilMenu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
        if (!shouldAtSlot2(additionItem)) {
            client.gameMode.handleInventoryMouseClick(
                anvilMenu.containerId, AnvilMenu.ADDITIONAL_SLOT, 1, ClickType.THROW, client.player);
            isAdditionSlotValid = false;
        }

        int l = AnvilMenu.RESULT_SLOT + 1;
        int r = AnvilMenu.RESULT_SLOT + 4 * 9;
        if (!isInputSlotValid) {
            for (int i = l; i <= r; i++) {
                ItemStack item = anvilMenu.getSlot(i).getItem();
                if (!isInputSlotValid && shouldAtSlot1(item)) {
                    client.gameMode.handleInventoryMouseClick(
                        anvilMenu.containerId, i, 0, ClickType.QUICK_MOVE, client.player);
                    isInputSlotValid = true;
                    break;
                }
            }
            if (!isInputSlotValid) {
                client.player.closeContainer();
                AutoRepair.stopRepairing();
                return;
            }
        }

        if (!isAdditionSlotValid) {
            for (int i = l; i <= r; i++) {
                ItemStack item = anvilMenu.getSlot(i).getItem();
                if (!isAdditionSlotValid && shouldAtSlot2(item)) {
                    client.gameMode.handleInventoryMouseClick(
                        anvilMenu.containerId, i, 0, ClickType.QUICK_MOVE, client.player);
                    isAdditionSlotValid = true;
                    break;
                }
            }
            if (!isAdditionSlotValid) {
                client.player.closeContainer();
                AutoRepair.stopRepairing();
                return;
            }
        }

        if (anvilMenu.getCost() > client.player.experienceLevel) {
            client.player.closeContainer();
            AutoRepair.stopRepairing();
            return;
        }

        // 移动到背包而不是丢出
        if (AutoRepair.isRepairing()) {
            if (!InventoryUtil.inventoryHasEmptySlot()) {
                client.player.closeContainer();
                AutoRepair.stopRepairing();
                return;
            }
            client.gameMode.handleInventoryMouseClick(
                anvilMenu.containerId, AnvilMenu.RESULT_SLOT, 0, ClickType.QUICK_MOVE, client.player);
            return;
        }

        client.gameMode.handleInventoryMouseClick(
            anvilMenu.containerId, AnvilMenu.RESULT_SLOT, 1, ClickType.THROW, client.player);
    }

    private static boolean shouldAtSlot1(ItemStack item) {
        if (AutoRepair.isRepairing()) {
            return AutoRepair.isNeedRepairNetheriteEquipment(item);
        }

        if (item.isEmpty()) return false;
        if (item.is(Items.ENCHANTED_BOOK)) return false;
        if (!getMending().canEnchant(item)) return false;

        return !EnchantmentHelper.hasEnchantment(item, Enchantments.MENDING);
    }

    private static boolean shouldAtSlot2(ItemStack item) {
        if (item.isEmpty()) return false;
        if (!item.is(Items.ENCHANTED_BOOK)) return false;

        return EnchantmentHelper.hasEnchantment(item, Enchantments.MENDING);
    }

    private static Enchantment getMending() {
        Minecraft client = Minecraft.getInstance();
        if (mending == null) {
            mending = client.level
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.MENDING)
                .value();
        }
        return mending;
    }
}
