package com.example.module.AutoRepair;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;

import com.example.common.Code;
import com.example.util.SwapSlot;

public class EnchantExp {
    private static int tickCounter = 0;
    private static final Minecraft client = Minecraft.getInstance();
    private static Enchantment mending;

    public static void tryToEnchantMending() {
        if (!AutoRepair.config.repairEnabled ||
            client.level == null ||
            client.player == null
        )
            return;
        if (++tickCounter < AutoRepair.config.repairInterval) return;
        tickCounter = 0;
        
        Player player = client.player;
        if (!player.hasContainerOpen() || !(player.containerMenu instanceof AnvilMenu anvilMenu)) return;
        AutoRepair.LOGGER.info("open anvil menu");

        int[] resultSlots = findExpEquitmentSlot();
        if (resultSlots[0] == Code.NOT_FOUND || resultSlots[1] == Code.NOT_FOUND) return;


        if (checkIsNeedOrDrop(anvilMenu, AnvilMenu.INPUT_SLOT))
            SwapSlot.anvilSwaper(anvilMenu, resultSlots[0], AnvilMenu.INPUT_SLOT);
        if (checkIsNeedOrDrop(anvilMenu, AnvilMenu.ADDITIONAL_SLOT))
            SwapSlot.anvilSwaper(anvilMenu, resultSlots[1], AnvilMenu.ADDITIONAL_SLOT);
        client.gameMode.handleInventoryMouseClick(anvilMenu.containerId, AnvilMenu.RESULT_SLOT, 0, ClickType.THROW, player);

        AutoRepair.LOGGER.info("Enchanting Mending on slot {} and {}", resultSlots[0], resultSlots[1]);
    }

    public static int[] findExpEquitmentSlot() {
        Player player = client.player;

        int[] resultSlots = new int[] {Code.NOT_FOUND, Code.NOT_FOUND};

        AnvilMenu anvilMenu = (AnvilMenu) player.containerMenu;
        for (int i=AnvilMenu.RESULT_SLOT+1; i<AnvilMenu.RESULT_SLOT+1+3*9; i++) {
            ItemStack item = anvilMenu.getSlot(i).getItem();
            if (resultSlots[1] == Code.NOT_FOUND && 
                item.is(Items.ENCHANTED_BOOK) && 
                Replace.hasEnchantment(item, Enchantments.MENDING)
            ) {
                // AutoRepair.LOGGER.info("Found Mending Book on slot {}", i);
                resultSlots[1] = i;
            } else if (resultSlots[0] == Code.NOT_FOUND 
                && getMending().canEnchant(item) && 
                !Replace.hasEnchantment(item, Enchantments.MENDING)

            ) {
                // AutoRepair.LOGGER.info("Found none mending equiment {} on slot {}", item.getHoverName().getString(), i);
                resultSlots[0] = i;
            }
            if (resultSlots[0] != Code.NOT_FOUND && resultSlots[1] != Code.NOT_FOUND) {
                break;
            }
        }
        return resultSlots;
    }

    /* 
     * 检查anvil容器的slot是否需要从背包添加物品
     * @param anvilMenu anvil容器
     * @param slot 要检查的slot，必须是AnvilMenu.INPUT_SLOT或AnvilMenu.ADDITIONAL_SLOT
     * @return 是否需要添加
     */
    public static boolean checkIsNeedOrDrop(AnvilMenu anvilMenu, int slot) {
        if (slot != AnvilMenu.INPUT_SLOT && slot != AnvilMenu.ADDITIONAL_SLOT) {
            throw new IllegalArgumentException("slot must be AnvilMenu.INPUT_SLOT or AnvilMenu.ADDITIONAL_SLOT");
        }
        ItemStack item = anvilMenu.getSlot(slot).getItem();
        if (item.isEmpty()) return true;

        if (slot == AnvilMenu.INPUT_SLOT) {
            // 第一格不能是附魔书，并且要能附魔经验修补且本身也没有经验修补的附魔
            if (item.is(Items.ENCHANTED_BOOK) ||
                !getMending().canEnchant(item) ||
                !Replace.hasEnchantment(item, Enchantments.MENDING)
            ) {
                client.gameMode.handleInventoryMouseClick(
                    anvilMenu.containerId, AnvilMenu.INPUT_SLOT, 0, ClickType.THROW, client.player
                );
                return true;
            }
        } else {
            // 第二格一定需要是经验修补的附魔书
            if (!item.is(Items.ENCHANTED_BOOK) ||
                !Replace.hasEnchantment(item, Enchantments.MENDING)
            ) {
                client.gameMode.handleInventoryMouseClick(
                    anvilMenu.containerId, AnvilMenu.ADDITIONAL_SLOT, 0, ClickType.THROW, client.player
                );
                return true;
            }
        }
        return false;
    }

    public static Enchantment getMending() {
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
