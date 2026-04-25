package cn.taotxi.Makemoney.module.AutoDrop;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.taotxi.Makemoney.util.CommonUtil;
import cn.taotxi.Makemoney.util.ItemStackUtil;
import cn.taotxi.Makemoney.util.StringUtil;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;


public class Dropper {
    // TODO: 如果命名空间是minecraft，可以省略命名空间
    public static void tryToDropItems() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        // when player is open a container or in a config GUI, do not drop items
        if (player.hasContainerOpen() || client.screen instanceof YACLScreen) return;

        final List<Integer> dropSlots = new ArrayList<>();
        InventoryMenu inventoryMenu = player.inventoryMenu;
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            if (AutoDrop.config.ingnoreSlots.contains(i)) continue;

            ItemStack item = inventoryMenu.getSlot(i).getItem();

            // AutoDrop.LOGGER.info("Check item {} in slot {}", item.getItemName(), i);
            if (!shouldDrop(item)) continue;
            // AutoDrop.LOGGER.info("Dropping item {} in slot {}", ItemStackUtil.getName(item), i);
            dropSlots.add(i);
        }
        if (dropSlots.isEmpty()) return;

        dropItemAnywhere(dropSlots, AutoDrop.config.throwDirection);

    }

    public static boolean shouldDrop(ItemStack item) {
        if (item.isEmpty()) return false;

        for (AutoDropConfig.Item check: AutoDrop.config.items){
            // TODO: 适配多颜色文本
            // check name
            String name = ItemStackUtil.getName(item);
            if (!check.name.equals("*") && !StringUtil.regMatch(name, check.name)) {
                continue; 
            }
            // check id
            String id = ItemStackUtil.getId(item);
            if (!check.id.equals("*") && !StringUtil.regMatch(id, check.id)) {
                continue;
            }

            /**
             * 有点绕，梳理下思路
             * 1. 如果check.tags包含`*`，匹配直接通过
             * 2. 如果check.tags为空，即便itemTags也为空，匹配也不通过
             * 3. 如果itemTags和check.tags有交集，则通过
             * 下面第一个if不赘述
             * 第二个if：没有包含`*`，且itemTags和check.tags没有交集，表示匹配不通过
             */
            List<String> itemTags = ItemStackUtil.getTags(item);
            // check tag
            if (check.tags.isEmpty()) continue;
            if (!check.tags.contains("*") && !CommonUtil.hasIntersection_regMatch(itemTags, check.tags)) continue;

            if (check.enchantments.size() == 0) {    // 匹配通过
                return false;
            }

            if (calEnchantCounts(item, check.enchantments) >= check.minEnchantRequir) {
                return false;   // ✔
            }
        }
        return true;
    }

    // TODO: 验证附魔书是否适用此函数
    public static int calEnchantCounts(ItemStack item, Map<String, Integer> enchantments) {
        int counter = 0;
        ItemEnchantments it = 
            item.is(Items.ENCHANTED_BOOK) ?
            item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
            item.getEnchantments();
        for (Holder<Enchantment> enchant: it.keySet()) {
            String ID = enchant.getRegisteredName();
            int level = it.getLevel(enchant);
            if (enchantments.containsKey(ID) && enchantments.getOrDefault(ID, -666) == level) {
                counter++;
            }
        }
        return counter;
    }

    public static void dropItemAnywhere(int slot, String direction) {
        dropItemAnywhere(List.of(slot), direction);
    }

    // TODO: Bug: 创造模式会丢弃两个物品，但背包实际减少的是一个
    public static void dropItemAnywhere(List<Integer> slots, String direction) {
        // if (!AutoDropConfig.getAllThrowDirections().contains(direction)) {
        //     AutoDrop.LOGGER.error("Error direction !!!");
        // }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        InventoryMenu inventoryMenu = player.inventoryMenu;

        if (direction.equals(AutoDropConfig.Direction.LOOKING)) {
          for (int slot: slots) {
            client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 1, ClickType.THROW, player);
          }
          return;
        }

        float xRot = player.getXRot();
        float yRot = player.getYRot();
        setPlayerRotation(direction);
        for (int slot: slots) {
            client.gameMode.handleInventoryMouseClick(inventoryMenu.containerId, slot, 1, ClickType.THROW, player);
        }
        setPlayerRotation(yRot, xRot);
    }

    public static void setPlayerRotation(String direction) {
        AutoDropConfig.Direction dir = AutoDropConfig.Direction.valueOf(direction);
        switch (dir) {
            case UP:
                setPlayerRotation(0, -90);
                break;
            case DOWN:
                setPlayerRotation(0, 90);
                break;
            case EAST:
                setPlayerRotation(-90, 0);
                break;
            case WEST:
                setPlayerRotation(90, 0);
                break;
            case NORTH:
                setPlayerRotation(-180, 0);
                break;
            case SOUTH:
                setPlayerRotation(0, 0);
                break;
            case LOOKING:
                break;
            default:
                AutoDrop.LOGGER.error("Error direction: {} !!!", direction);
                break;
        }
    }

    // TODO: 测试在服务端的表现
    public static void setPlayerRotation(float yaw, float pitch) {
        LocalPlayer player = Minecraft.getInstance().player;
        player.setYRot(yaw);
        player.setXRot(pitch);

        player.connection.send(
            new ServerboundMovePlayerPacket.Rot(
                yaw, pitch,
                player.onGround(),
                false
            )
        );
    }
}
