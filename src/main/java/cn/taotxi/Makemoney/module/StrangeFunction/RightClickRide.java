package cn.taotxi.Makemoney.module.StrangeFunction;


import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.taotxi.Makemoney.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;

public class RightClickRide {
    public static void handleInteract(
        Player player, 
        Entity entity,
        InteractionHand interactionHand, 
        CallbackInfoReturnable<InteractionResult> cir) 
    {
        if (!isEnabled(false)) return;
        if (player.isCrouching()) return;

        // 右键一次左右手都会调用interact
        if (interactionHand != InteractionHand.MAIN_HAND) return;
        if (!player.getItemInHand(interactionHand).isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        Entity lookEntity = client.crosshairPickEntity;

        if (lookEntity == null) return;
        if (!(lookEntity instanceof LivingEntity)) return;

        // 跳过装备鞍的生物
        if (
            lookEntity instanceof Mob mobEntity &&
            mobEntity.isSaddled()
        ) {
            return;
        }

        // 跳过可以交互或者可以骑乘的生物
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

        // TODO: 配置界面可以自定义骑乘命令
        Message.sendMessage("/ride");
    }

    public static boolean isEnabled(boolean forceDefault) {
        return StrangeConfig.getInstance().rightClickRideEnabled.getValue();
    }

    public static void setEnabled(boolean enabled) {
        StrangeConfig.getInstance().rightClickRideEnabled.setValue(enabled);
    }
}
