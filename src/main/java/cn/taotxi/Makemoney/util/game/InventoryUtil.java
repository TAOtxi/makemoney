package cn.taotxi.Makemoney.util.game;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.InventoryMenu;

public class InventoryUtil {
    // TODO: 待测试
    // TODO: 待完善异常抛出及处理，比如不在游戏内调用等等
    public static List<Integer> getInventoryNotEmptySlots() {
        InventoryMenu inventoryMenu = Minecraft.getInstance().player.inventoryMenu;
        List<Integer> result = new ArrayList<>();
        for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.USE_ROW_SLOT_END; i++) {
            ItemStack item = inventoryMenu.getSlot(i).getItem();
            
            if (item.isEmpty()) continue;
            result.add(i);
        }
        return result;
    }
}
