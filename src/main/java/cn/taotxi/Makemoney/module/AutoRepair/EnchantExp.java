package cn.taotxi.Makemoney.module.AutoRepair;

import cn.taotxi.Makemoney.common.Code;
import cn.taotxi.Makemoney.util.SwapSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;

public class EnchantExp {
    private static Enchantment mending;

    public static void tryToEnchantMending() {
        if (!AutoRepair.config.repairEnabled)
            return;
        
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (!(player.containerMenu instanceof AnvilMenu anvilMenu)) return;

        int equitmentSlot = Code.NOT_FOUND;
        int mendingBookSlot = Code.NOT_FOUND;

        // check INPUT_SLOT
        if (checkIsNeed(anvilMenu, AnvilMenu.INPUT_SLOT)) {
            equitmentSlot = findEquitment(player);
            if (equitmentSlot == Code.NOT_FOUND) return;
            SwapSlot.anvilSwaper(anvilMenu, equitmentSlot, AnvilMenu.INPUT_SLOT);
        }
        // check ADDITIONAL_SLOT
        if (checkIsNeed(anvilMenu, AnvilMenu.ADDITIONAL_SLOT)) {
            mendingBookSlot = findMendingBook(player);
            if (mendingBookSlot == Code.NOT_FOUND) return;
            SwapSlot.anvilSwaper(anvilMenu, mendingBookSlot, AnvilMenu.ADDITIONAL_SLOT);
        }
        
        if (player.experienceLevel < anvilMenu.getCost()) {
            // Message.sendMessage("Not enough experience to enchant Mending");
            return;
        }
        client.gameMode.handleInventoryMouseClick(anvilMenu.containerId, AnvilMenu.RESULT_SLOT, 0, ClickType.THROW, player);

        // AutoRepair.LOGGER.info("Enchanting Mending on slot {} and {}", equitmentSlot, mendingBookSlot);
    }

    private static int findEquitment(Player player) {
        if (!(player.containerMenu instanceof AnvilMenu anvilMenu)) return Code.NOT_FOUND;
        for (int i=AnvilMenu.RESULT_SLOT+1; i<AnvilMenu.RESULT_SLOT+1+4*9; i++) {
            ItemStack item = anvilMenu.getSlot(i).getItem();
            if (!item.is(Items.ENCHANTED_BOOK) &&
                getMending().canEnchant(item) &&
                !Replace.hasEnchantment(item, Enchantments.MENDING)
            ) {
                return i;
            }
        }
        return Code.NOT_FOUND;
    }

    private static int findMendingBook(Player player) {
        if (!(player.containerMenu instanceof AnvilMenu anvilMenu)) return Code.NOT_FOUND;
        for (int i=AnvilMenu.RESULT_SLOT+1; i<AnvilMenu.RESULT_SLOT+1+4*9; i++) {
            ItemStack item = anvilMenu.getSlot(i).getItem();
            if (item.is(Items.ENCHANTED_BOOK) && 
                Replace.hasEnchantment(item, Enchantments.MENDING)
            ) {
                return i;
            }
        }
        return Code.NOT_FOUND;
    }

    /** 
     * 检查anvil容器的slot是否需要从背包添加物品
     * @param anvilMenu anvil容器
     * @param slot 要检查的slot，必须是AnvilMenu.INPUT_SLOT或AnvilMenu.ADDITIONAL_SLOT
     * @return 是否需要添加
     **/
    public static boolean checkIsNeed(AnvilMenu anvilMenu, int slot) {
        if (slot != AnvilMenu.INPUT_SLOT && slot != AnvilMenu.ADDITIONAL_SLOT) {
            throw new IllegalArgumentException("slot must be AnvilMenu.INPUT_SLOT or AnvilMenu.ADDITIONAL_SLOT");
        }
        ItemStack item = anvilMenu.getSlot(slot).getItem();
        if (item.isEmpty()) return true;
        Minecraft client = Minecraft.getInstance();
        
        if (slot == AnvilMenu.INPUT_SLOT) {
            // 第一格不能是附魔书，并且要能附魔经验修补且本身也没有经验修补的附魔
            if (item.is(Items.ENCHANTED_BOOK) ||
                !getMending().canEnchant(item) ||
                Replace.hasEnchantment(item, Enchantments.MENDING)
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
