package cn.taotxi.Makemoney.module.MendingHelper;

import java.util.List;
import java.util.Map;

import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import cn.taotxi.Makemoney.util.game.EnchantmentHelper;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoRepair {
    private static final Minecraft client = Minecraft.getInstance();
    private static final MendingHelperConfig CONFIG = MendingHelperConfig.getInstance();
    private static final String AUTO_REPAIR_CHECK = "autoRepairCheck";
    private static final double BLOCK_INTERACTION_RANGE = 4.5d;
    private static BlockPos mendingBookPos = null;
    private static BlockPos anvilPos = null;
    private static boolean isRepairing = false;
    private static boolean isGetingMendingBook = false;
    private static int toGetMendingBookCount = -1;
    
    public static void initialize() {
        CONFIG.mendingBookPositions.onChange(
            (oldValue, newValue) -> {
                if (newValue.size() != 3) {
                    throw new IllegalArgumentException("Mending book positions must be 3 numbers");
                }

                mendingBookPos = new BlockPos(
                    newValue.get(0).getAsInt(),
                    newValue.get(1).getAsInt(),
                    newValue.get(2).getAsInt()
                );
            }
        );

        CONFIG.autoRepairEnabled.onChange(
            (oldValue, newValue) -> {
                isRepairing = false;
                isGetingMendingBook = false;

                if (newValue && !TaskUtil.hasTimeTask(AUTO_REPAIR_CHECK)) {
                    TaskUtil.createTimeTask(AUTO_REPAIR_CHECK, AutoRepair::tick, 20);
                } else if (!newValue) {
                    TaskUtil.removeTimeTask(AUTO_REPAIR_CHECK);
                }
            }
        );
        CONFIG.autoRepairEnabled.triggerConfigChange();

        if (CONFIG.mendingBookPositions.size() == 3) {
            mendingBookPos = new BlockPos(
                CONFIG.mendingBookPositions.get(0).getAsInt(),
                CONFIG.mendingBookPositions.get(1).getAsInt(),
                CONFIG.mendingBookPositions.get(2).getAsInt()
            );
        }
    }

    private static void disableAutoRepair() {
        CONFIG.autoRepairEnabled.disable();
        CONFIG.saveConfig();
    }

    private static boolean isAnvilBlock(BlockPos pos) {
        BlockState blockState = client.level.getBlockState(pos);
        return blockState.is(Blocks.ANVIL) || 
            blockState.is(Blocks.CHIPPED_ANVIL) || 
            blockState.is(Blocks.DAMAGED_ANVIL);
    }

    private static BlockPos findAnvilPos() {
        Vec3 eyePos = client.player.getEyePosition();
        double maxDistance2 = BLOCK_INTERACTION_RANGE * BLOCK_INTERACTION_RANGE;
        int searchRadius = (int) Math.ceil(BLOCK_INTERACTION_RANGE);
        for (BlockPos pos : BlockPos.withinManhattan(
            BlockPos.containing(eyePos), searchRadius, searchRadius, searchRadius)
        ) {
            if (eyePos.distanceToSqr(pos.getCenter()) > maxDistance2) continue;

            if (isAnvilBlock(pos)) {
                return pos;
            }
        }

        return null;
    }

    public static boolean isNeedRepairNetheriteEquipment(ItemStack itemStack) {
        if (!itemStack.isDamaged()) return false;
        if (
            !itemStack.is(Items.NETHERITE_SWORD) &&
            !itemStack.is(Items.NETHERITE_AXE) &&
            !itemStack.is(Items.NETHERITE_PICKAXE) &&
            !itemStack.is(Items.NETHERITE_SHOVEL) &&
            !itemStack.is(Items.NETHERITE_HOE) &&
            !itemStack.is(Items.NETHERITE_HELMET) &&
            !itemStack.is(Items.NETHERITE_CHESTPLATE) &&
            !itemStack.is(Items.NETHERITE_LEGGINGS) &&
            !itemStack.is(Items.NETHERITE_BOOTS)
        ) {
            return false;
        }

        return !EnchantmentHelper.hasEnchantment(itemStack, Enchantments.MENDING);
    }

    private static boolean isMendingBook(ItemStack itemStack) {
        if (!itemStack.is(Items.ENCHANTED_BOOK)) return false;
        return EnchantmentHelper.hasEnchantment(itemStack, Enchantments.MENDING);
    }

    public static boolean isRepairing() {
        return isRepairing;
    }

    public static void stopRepairing() {
        isRepairing = false;
    }

    public static boolean isGetingMendingBook() {
        return isGetingMendingBook;
    }

    public static void onOpenContainer() {
        if (!isGetingMendingBook || !CONFIG.autoRepairEnabled.getValue()) {
            isGetingMendingBook = false;
            return;
        }
        tryToGetMendingBookFromContainer();
    }

    private static void tryToGetMendingBookFromContainer() {
        if (!client.player.hasContainerOpen()) {
            throw new IllegalArgumentException("No container open");
        }

        AbstractContainerMenu containerMenu = client.player.containerMenu;
        Map.Entry<Integer, Integer> slotRange = InventoryUtil.getContainerSlotRange(containerMenu);
        if (slotRange == null) {
            throw new IllegalArgumentException("No supported container " + containerMenu.getClass().getSimpleName());
        }

        if (toGetMendingBookCount <= 0) {
            throw new IllegalArgumentException("toGetMendingBookCount must be greater than 0");
        }

        int startSlot = slotRange.getKey();
        int endSlot = slotRange.getValue();

        boolean hasMendingBook = false;
        for (int i = startSlot; i <= endSlot; i++) {
            ItemStack item = containerMenu.getSlot(i).getItem();
            if (isMendingBook(item)) {
                hasMendingBook = true;
                toGetMendingBookCount--;
                client.gameMode.handleInventoryMouseClick(
                    containerMenu.containerId, i, 0, ClickType.QUICK_MOVE, client.player
                );
            }
            if (toGetMendingBookCount <= 0) break;
        }

        client.player.closeContainer();
        isGetingMendingBook = false;
        if (!hasMendingBook) {
            Message.clientSideMsg(T.tl("mendingHelper.mendingBookPos.noMendingBook.message"));
            disableAutoRepair();
        }
    }

    private static void tryToOpenMendingBookContainer() {
        if (toGetMendingBookCount <= 0) return;

        if (mendingBookPos == null) {
            Message.clientSideMsg(T.tl("mendingHelper.mendingBookPos.notSet.message"));
            disableAutoRepair();
            return;
        }

        if (!canInteract(mendingBookPos)) {
            return;
        }

        BlockState blockState = client.level.getBlockState(mendingBookPos);
        Map.Entry<Integer, Integer> slotRange = InventoryUtil.getContainerSlotRange(blockState);
        if (slotRange == null) {
            Message.clientSideMsg(
                T.tl("mendingHelper.mendingBookPos.notContainer.message", 
                mendingBookPos.getX(), mendingBookPos.getY(), mendingBookPos.getZ())
            );
            disableAutoRepair();
            return;
        }

        isGetingMendingBook = true;
        BlockHitResult hitResult = new BlockHitResult(mendingBookPos.getCenter(), Direction.UP, mendingBookPos, false);
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hitResult);
    }

    private static boolean canInteract(BlockPos blockPos) {
        return blockPos.getCenter().distanceToSqr(client.player.getEyePosition()) <= BLOCK_INTERACTION_RANGE * BLOCK_INTERACTION_RANGE;
    }

    private static void tick() {
        if (client.player == null) return;
        if (
            !client.player.hasContainerOpen() &&
            (isRepairing || isGetingMendingBook)
        ) {
            isRepairing = false;
            isGetingMendingBook = false;
            return;
        }

        int level = client.player.experienceLevel;
        if (level < 5) return;

        if (
            !CONFIG.autoEnchantEnabled.getValue() ||
            !CONFIG.autoReplaceEnabled.getValue()
        ) {
            Message.clientSideMsg(T.tl("mendingHelper.mendingBookPos.noOpenOtherFunction.message"));
            disableAutoRepair();
            return;
        }

        if (isGetingMendingBook) return;
        if (isRepairing) return;

        // if (AutoMendingReplace.isOffhandSuitable()) return;

        List<Integer> repairableItemSlots = InventoryUtil.findSuitableSlots(
            AutoRepair::isNeedRepairNetheriteEquipment);

        int needMendingBookCount = repairableItemSlots.size();
        if (needMendingBookCount == 0) return;
 
        List<Integer> mendingBookSlots = InventoryUtil.findSuitableSlots(AutoRepair::isMendingBook);
        int mendingBookCount = mendingBookSlots.size();

        if (mendingBookCount == 0) {
            if (client.player.hasContainerOpen()) {
                client.player.closeContainer();
            }

            int emptySlotCount = InventoryUtil.getInventoryEmptySlotCount();

            toGetMendingBookCount = Math.min(needMendingBookCount, level / 2);
            toGetMendingBookCount = Math.min(toGetMendingBookCount, emptySlotCount);

            // 从容器拿点经验修补
            tryToOpenMendingBookContainer();
            return;
        }

        if (anvilPos == null || !isAnvilBlock(anvilPos)) {
            anvilPos = findAnvilPos();
            if (anvilPos == null) return;
        }

        if (!canInteract(anvilPos)) {
            return;
        }

        isRepairing = true;
        BlockHitResult hitResult = new BlockHitResult(anvilPos.getCenter(), Direction.UP, anvilPos, false);
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hitResult);
    }
}
