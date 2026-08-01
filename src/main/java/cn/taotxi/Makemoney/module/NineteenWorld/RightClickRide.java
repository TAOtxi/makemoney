package cn.taotxi.Makemoney.module.NineteenWorld;

import cn.taotxi.Makemoney.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;

public class RightClickRide {
    private static final NineteenWorldConfig CONFIG = NineteenWorldConfig.getInstance();
    private static final Minecraft client = Minecraft.getInstance();

    public static void handleInteract(
        Player player, 
        Entity entity,
        InteractionHand interactionHand) 
    {
        if (player.isCrouching()) return;
        if (!CONFIG.rightClickRideEnabled.getValue()) return;

        // 右键一次，左右手都会调用interact
        if (interactionHand != InteractionHand.MAIN_HAND) return;
        
        Entity lookEntity = client.crosshairPickEntity;
        
        if (lookEntity == null) return;
        
        if (!player.getItemInHand(interactionHand).isEmpty()) return;
        if (!(lookEntity instanceof LivingEntity)) return;

        // 跳过装备鞍的生物
        if (
            lookEntity instanceof Mob mobEntity &&
            mobEntity.isSaddled()
        ) {
            return;
        }

        // 单独判断鹦鹉螺
        if (lookEntity instanceof AbstractNautilus) {
            Message.sendMessage("/ride");
            return;
        }

        // 跳过可以交互或者原版可以骑乘的生物
        if (
            lookEntity instanceof PlayerRideable ||
            lookEntity instanceof Player ||
            lookEntity instanceof AbstractVillager
        ) {
            return;
        }
        
        // 跳过装备挽具的乐魂
        if (lookEntity instanceof HappyGhast happyGhast &&
            happyGhast.isWearingBodyArmor()
        ) {
            return;
        }

        if (lookEntity instanceof Mannequin) return;

        // TODO: 配置界面可以自定义骑乘命令
        Message.sendMessage("/ride");
    }
}
