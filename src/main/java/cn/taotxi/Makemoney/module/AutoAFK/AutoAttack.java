package cn.taotxi.Makemoney.module.AutoAFK;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AutoAttack {
    private static final AutoAFKConfig CONFIG = AutoAFKConfig.getInstance();
    private static final String AUTO_ATTACK_TASK = "autoAttack";
    private static final String AUTO_ATTACK_SHOW_INFO = "autoAttackShowInfo";
    private static final Minecraft client = Minecraft.getInstance();
    private static final List<String> attackList = new ArrayList<>();

    public static void initialize() {
        CONFIG.autoAttackEnabled.onChange((oldValue, newValue) -> onConfigChange());
        CONFIG.autoAttackEnabled.triggerConfigChange();

        CONFIG.attackList.onChange(
            (oldValue, newValue) -> {
                attackList.clear();
                for (JsonElement el : newValue) {
                    attackList.add(el.getAsString());
                }
            }
        );
        CONFIG.attackList.triggerConfigChange();

        CONFIG.showInfo.onChange((oldValue, newValue) -> onConfigChange());
        CONFIG.showInfo.triggerConfigChange();
    }

    private static void onConfigChange() {
        if (CONFIG.autoAttackEnabled.getValue()) {
            if (!TaskUtil.hasTimeTask(AUTO_ATTACK_TASK)) {
                TaskUtil.createTimeTask(AUTO_ATTACK_TASK, AutoAttack::attack, AutoAttack::getAttackInterval, true);
            }
            if (CONFIG.showInfo.getValue() && !TaskUtil.hasTimeTask(AUTO_ATTACK_SHOW_INFO)) {
                TaskUtil.createTimeTask(AUTO_ATTACK_SHOW_INFO, AutoAttack::showInfo, 20, true);
            }
        } else {
            TaskUtil.removeTimeTask(AUTO_ATTACK_TASK);
            TaskUtil.removeTimeTask(AUTO_ATTACK_SHOW_INFO);
        }
    }

    private static void showInfo() {
        if (client.player == null) return;
        float tps = calcServerTps.getTps();
        int interval = getAttackInterval();
        Message.actionBarMsg(T.tl("autoAFK.autoAttack.info.message", tps, interval));
    }

    private static void attack() {
        if (client.player == null) return;

        ItemStack item = client.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (
            !client.player.isCreative() &&
            !item.isEmpty() && 
            CONFIG.durabilityCheck.getValue() && 
            item.nextDamageWillBreak()
        ) {
            return;
        }

        Entity targer = client.crosshairPickEntity;
        if (!isAttackAbled(targer)) return;
        
        client.gameMode.attack(client.player, targer);
        client.player.swing(InteractionHand.MAIN_HAND);
    }

    private static boolean isAttackAbled(Entity entity) {
        if (entity == null || entity.isRemoved()) return false;
        if (!(entity instanceof LivingEntity)) return false;

        boolean isWhiteListMode = CONFIG.attackMode.getValue();

        return (isWhiteListMode && isEntityInAttackList(entity)) || 
               (!isWhiteListMode && !isEntityInAttackList(entity));
    }

    private static boolean isEntityInAttackList(Entity entity) {
        String entityName = entity.getType().toShortString();
        return attackList.contains(entityName);
    }

    private static int getAttackInterval() {
        int rawInterval = CONFIG.attackInterval.getValue();
        float currentTps = calcServerTps.getTps();

        return Math.max(1, (int) (rawInterval / (currentTps / 20)));
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> attackCommand() {
        return ClientCommandManager.literal("attack")
            .executes(AutoAttack::showHelp)
            .then(ClientCommandManager.literal("help").executes(AutoAttack::showHelp))
            .then(ClientCommandManager.literal("on")
                .executes(context -> {
                    CONFIG.autoAttackEnabled.enable();
                    CONFIG.saveConfig();
                    context.getSource().sendFeedback(T.tl("autoAFK.autoAttack.enabled.message"));
                    return 1;
                }))
            .then(ClientCommandManager.literal("off")
                .executes(context -> {
                    CONFIG.autoAttackEnabled.disable();
                    CONFIG.saveConfig();
                    context.getSource().sendFeedback(T.tl("autoAFK.autoAttack.disabled.message"));
                    return 1;
                }))
            .then(ClientCommandManager.literal("interval")
                .then(ClientCommandManager.argument("interval", IntegerArgumentType.integer(1))
                .executes(context -> {
                    int interval = context.getArgument("interval", Integer.class);
                    CONFIG.attackInterval.setValue(interval);
                    CONFIG.saveConfig();
                    context.getSource().sendFeedback(T.tl("autoAFK.autoAttack.interval.message", interval));
                    return 1;
                })))
            .then(ClientCommandManager.literal("info")
                .executes(context -> {
                    float tps = calcServerTps.getTps();
                    int attackInterval = getAttackInterval();
                    context.getSource().sendFeedback(T.tl("autoAFK.autoAttack.info.message", tps, attackInterval));
                    return 1;
                }));
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("autoAFK.autoAttack.help.message"));
        return 1;
    }
}
