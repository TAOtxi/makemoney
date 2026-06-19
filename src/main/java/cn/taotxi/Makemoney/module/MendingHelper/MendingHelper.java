package cn.taotxi.Makemoney.module.MendingHelper;

import java.util.Map;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.game.InventoryUtil;

public class MendingHelper {
    public static final String MODULE_NAME = "mendinghelper";
    public static final MendingHelperConfig CONFIG = MendingHelperConfig.getInstance();

    public static void initialize() {
        CONFIG.loadConfig();

        registerCommand();
        AutoMendingReplace.initialize();
        AutoDecompose.initialize();
        AutoRepair.initialize();
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .executes(MendingHelper::showHelp)
                .then(ClientCommandManager.literal("autoreplace")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setAutoReplaceEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setAutoReplaceEnabled(false))))
                .then(ClientCommandManager.literal("autoenchant")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setAutoEnchantEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setAutoEnchantEnabled(false))))
                .then(ClientCommandManager.literal("autodecompose")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setAutoDecomposeEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setAutoDecomposeEnabled(false))))
                .then(ClientCommandManager.literal("autorepair")
                    .then(ClientCommandManager.literal("on")
                        .executes(context -> setAutoRepairEnabled(true)))
                    .then(ClientCommandManager.literal("off")
                        .executes(context -> setAutoRepairEnabled(false)))
                    .then(ClientCommandManager.literal("setMendingBookPos")
                        .executes(MendingHelper::setMendingBookPos))
                    )
                .then(ClientCommandManager.literal("config")
                    .executes(MendingHelper::openConfigScreen))
            );

            dispatcher.register(ClientCommandManager.literal("mh")
                .executes(MendingHelper::showHelp)
                .redirect(cmd)
            );
        });
    }

    private static int openConfigScreen(CommandContext<FabricClientCommandSource> context) {
        GuiUtil.openYaclScreen(Makemoney.MOD_ID, MODULE_NAME);
        return 1;
    }

    private static int setAutoRepairEnabled(boolean enabled) {
        CONFIG.autoRepairEnabled.setValue(enabled);
        CONFIG.saveConfig();
        Message.clientSideMsg(enabled ? 
            T.tl("mendingHelper.autoRepair.enabled.message") : 
            T.tl("mendingHelper.autoRepair.disabled.message"));
        return 1;
    }

    private static int setMendingBookPos(CommandContext<FabricClientCommandSource> context) {
        Minecraft client = context.getSource().getClient();
        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos posLooking = ((BlockHitResult) client.hitResult).getBlockPos();
            BlockState blockState = client.level.getBlockState(posLooking);
            Map.Entry<Integer, Integer> slotRange = InventoryUtil.getContainerSlotRange(blockState);
            if (slotRange == null) {
                Message.clientSideMsg(T.tl("mendingHelper.mendingBookPos.set.error2.message"));
                return 1;
            }

            CONFIG.setMendingBookPosition(posLooking.getX(), posLooking.getY(), posLooking.getZ());
            CONFIG.saveConfig();
            Message.clientSideMsg(T.tl("mendingHelper.mendingBookPos.set.message", posLooking.getX(), posLooking.getY(), posLooking.getZ()));
        } else {
            Message.clientSideMsg(T.tl("mendingHelper.mendingBookPos.set.error.message"));
        }
        return 1;
    }

    private static int setAutoReplaceEnabled(boolean enabled) {
        CONFIG.autoReplaceEnabled.setValue(enabled);
        CONFIG.saveConfig();
        Message.clientSideMsg(enabled ? 
            T.tl("mendingHelper.autoReplace.enabled.message") : 
            T.tl("mendingHelper.autoReplace.disabled.message"));
        return 1;
    }

    private static int setAutoEnchantEnabled(boolean enabled) {
        CONFIG.autoEnchantEnabled.setValue(enabled);
        CONFIG.saveConfig();
        Message.clientSideMsg(enabled ? 
            T.tl("mendingHelper.autoEnchant.enabled.message") : 
            T.tl("mendingHelper.autoEnchant.disabled.message"));
        return 1;
    }

    private static int setAutoDecomposeEnabled(boolean enabled) {
        CONFIG.autoDecomposeEnabled.setValue(enabled);
        CONFIG.saveConfig();
        Message.clientSideMsg(enabled ? 
            T.tl("mendingHelper.autoDecompose.enabled.message") : 
            T.tl("mendingHelper.autoDecompose.disabled.message"));
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("mendinghelper.help.message"));
        return 1;
    }
}
