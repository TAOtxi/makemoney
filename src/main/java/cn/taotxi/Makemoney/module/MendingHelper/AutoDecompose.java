package cn.taotxi.Makemoney.module.MendingHelper;

import java.util.HashMap;
import java.util.Map;

import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AutoDecompose {
    private static final Minecraft client = Minecraft.getInstance();
    private static final MendingHelperConfig CONFIG = MendingHelperConfig.getInstance();
    private static final String DECOMPOSE_TASK = "mendingHelperDecomposeTask";
    private static final Map<Item, Integer> materialCountMap = new HashMap<>();
    
    public static void initialize() {
        CONFIG.autoDecomposeEnabled.onChange(
            (oldValue, newValue) -> {
                if (newValue && !TaskUtil.hasTimeTask(DECOMPOSE_TASK)) {
                    TaskUtil.createTimeTask(
                        DECOMPOSE_TASK, AutoDecompose::tryToDecomposeEquipment,
                    1);
                }
                if (!newValue) {
                    TaskUtil.removeTimeTask(DECOMPOSE_TASK);
                }
            }
        );
        CONFIG.autoDecomposeEnabled.triggerConfigChange();
        initMap();
    }

    private static void initMap() {
        materialCountMap.put(Items.NETHERITE_HELMET, 5);
        materialCountMap.put(Items.NETHERITE_CHESTPLATE, 8);
        materialCountMap.put(Items.NETHERITE_LEGGINGS, 7);
        materialCountMap.put(Items.NETHERITE_BOOTS, 4);
        materialCountMap.put(Items.NETHERITE_HOE, 2);
        materialCountMap.put(Items.NETHERITE_SWORD, 2);
        materialCountMap.put(Items.NETHERITE_PICKAXE, 3);
        materialCountMap.put(Items.NETHERITE_AXE, 3);
        materialCountMap.put(Items.NETHERITE_SHOVEL, 1);

        // 可恶的腐竹
        materialCountMap.put(Items.DIAMOND_HELMET, 1);
        materialCountMap.put(Items.DIAMOND_CHESTPLATE, 1);
        materialCountMap.put(Items.DIAMOND_LEGGINGS, 1);
        materialCountMap.put(Items.DIAMOND_BOOTS, 1);
        materialCountMap.put(Items.DIAMOND_HOE, 1);
        materialCountMap.put(Items.DIAMOND_SWORD, 1);
        materialCountMap.put(Items.DIAMOND_PICKAXE, 1);
        materialCountMap.put(Items.DIAMOND_AXE, 1);
        materialCountMap.put(Items.DIAMOND_SHOVEL, 1);

        materialCountMap.put(Items.GOLDEN_HELMET, 5);
        materialCountMap.put(Items.GOLDEN_CHESTPLATE, 8);
        materialCountMap.put(Items.GOLDEN_LEGGINGS, 7);
        materialCountMap.put(Items.GOLDEN_BOOTS, 4);
        materialCountMap.put(Items.GOLDEN_HOE, 2);
        materialCountMap.put(Items.GOLDEN_SWORD, 2);
        materialCountMap.put(Items.GOLDEN_PICKAXE, 3);
        materialCountMap.put(Items.GOLDEN_AXE, 3);
        materialCountMap.put(Items.GOLDEN_SHOVEL, 1);

        materialCountMap.put(Items.IRON_HELMET, 5);
        materialCountMap.put(Items.IRON_CHESTPLATE, 8);
        materialCountMap.put(Items.IRON_LEGGINGS, 7);
        materialCountMap.put(Items.IRON_BOOTS, 4);
        materialCountMap.put(Items.IRON_HOE, 2);
        materialCountMap.put(Items.IRON_SWORD, 2);
        materialCountMap.put(Items.IRON_PICKAXE, 3);
        materialCountMap.put(Items.IRON_AXE, 3);
        materialCountMap.put(Items.IRON_SHOVEL, 1);

        materialCountMap.put(Items.COPPER_HELMET, 5);
        materialCountMap.put(Items.COPPER_CHESTPLATE, 8);
        materialCountMap.put(Items.COPPER_LEGGINGS, 7);
        materialCountMap.put(Items.COPPER_BOOTS, 4);
        materialCountMap.put(Items.COPPER_HOE, 2);
        materialCountMap.put(Items.COPPER_SWORD, 2);
        materialCountMap.put(Items.COPPER_PICKAXE, 3);
        materialCountMap.put(Items.COPPER_AXE, 3);
        materialCountMap.put(Items.COPPER_SHOVEL, 1);

        materialCountMap.put(Items.LEATHER_HELMET, 5);
        materialCountMap.put(Items.LEATHER_CHESTPLATE, 8);
        materialCountMap.put(Items.LEATHER_LEGGINGS, 7);
        materialCountMap.put(Items.LEATHER_BOOTS, 4);

        materialCountMap.put(Items.STONE_HOE, 2);
        materialCountMap.put(Items.STONE_SWORD, 2);
        materialCountMap.put(Items.STONE_PICKAXE, 3);
        materialCountMap.put(Items.STONE_AXE, 3);
        materialCountMap.put(Items.STONE_SHOVEL, 1);

        materialCountMap.put(Items.WOODEN_HOE, 2);
        materialCountMap.put(Items.WOODEN_SWORD, 2);
        materialCountMap.put(Items.WOODEN_PICKAXE, 3);
        materialCountMap.put(Items.WOODEN_AXE, 3);
        materialCountMap.put(Items.WOODEN_SHOVEL, 1);
    }

    private static void tryToDecomposeEquipment() {
        if (client.player == null) return;
        if (client.player.hasContainerOpen()) return;
        
        BlockPos supportPos = client.player.getOnPos();
        BlockState supportBlockState = client.level.getBlockState(supportPos);
        if (!supportBlockState.is(Blocks.GOLD_BLOCK)) return;

        InventoryMenu inventoryMenu = client.player.inventoryMenu;
        int mainHandSlot = client.player.getInventory().getSelectedSlot() + InventoryMenu.USE_ROW_SLOT_START;
        
        if (!shouldAtMainHand(client.player.getMainHandItem())) {
            int targetSlot = -1;
            for (int i=InventoryMenu.INV_SLOT_START; i<InventoryMenu.USE_ROW_SLOT_END; i++) {
                ItemStack item = inventoryMenu.getSlot(i).getItem();
                if (i == mainHandSlot) continue;
                if (shouldAtMainHand(item)) {
                    targetSlot = i;
                    break;
                }
            }
            if (targetSlot == -1) {
                return;
            }
            InventoryUtil.swapItemToHand(InteractionHand.MAIN_HAND, targetSlot);
        }

        client.gameMode.useItemOn(
            client.player, 
            InteractionHand.MAIN_HAND, 
            new BlockHitResult(
                client.player.position(),
                Direction.UP,
                supportPos,
                false
            )
        );
    }

    private static boolean shouldAtMainHand(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        int materialCount = getMaterialCount(itemStack);
        if (materialCount == 0) return false;

        if (!itemStack.isDamageableItem()) return false;
        
        if (CONFIG.onlyDecomposeNoneDamage.getValue() && itemStack.isDamaged()) {
            return false;
        }
        
        int damage = itemStack.getDamageValue();
        return damage <= itemStack.getMaxDamage() * (materialCount - 1) / materialCount;
        
    }

    private static int getMaterialCount(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0;
        if (itemStack.getMaxStackSize() != 1) return 0;
        
        return materialCountMap.getOrDefault(itemStack.getItem(), 0);
    }

}
