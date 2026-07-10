package cn.taotxi.Makemoney.module.Highlight;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.GuiUtil;
import cn.taotxi.Makemoney.module.Highlight.render.Drawing;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Highlight {
    private static final Minecraft client = Minecraft.getInstance();
    private static final HighlightConfig CONFIG = HighlightConfig.getInstance();
    public static final String MODULE_NAME = "highlight";
    private static final String UPDATE_RENDER_ENTITY = "updateRenderEntity";
    private static final List<Entity> renderEntities = new ArrayList<>();
    private static boolean enabled = false;


    public static void initialize() {
        CONFIG.loadConfig();
        registerCommand();
        Drawing.updateColorMap();

        CONFIG.enabled.onChange(
            (oldValue, newValue) -> {
                enabled = newValue;
                if (newValue && !TaskUtil.hasTimeTask(UPDATE_RENDER_ENTITY)) {
                    TaskUtil.createTimeTask(UPDATE_RENDER_ENTITY, Highlight::updateRenderEntity, 20);
                } else if (!newValue) {
                    TaskUtil.removeTimeTask(UPDATE_RENDER_ENTITY);
                }
            }
        );
        CONFIG.enabled.triggerConfigChange();

        CONFIG.colorful.onChange(
            (oldValue, newValue) -> {
                Drawing.setColorful(newValue);
            }
        );
        CONFIG.colorful.triggerConfigChange();

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!enabled) return;
            Drawing.drawHighlightBox(context, renderEntities);
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, level) -> {
            renderEntities.clear();
        });
    }

    private static int enable(CommandContext<FabricClientCommandSource> context) {
        Message.clientSideMsg(T.tl("highlight.on.message"));
        CONFIG.enabled.enable();
        CONFIG.saveConfig();
        return 1;
    }

    private static int disable(CommandContext<FabricClientCommandSource> context) {
        Message.clientSideMsg(T.tl("highlight.off.message"));
        CONFIG.enabled.disable();
        CONFIG.saveConfig();
        return 1;
    }

    private static void updateRenderEntity() {
        if (client.level == null || client.player == null) return;
        renderEntities.clear();
        int radius = CONFIG.renderRadius.getValue();

        if (radius == 0 || radius < -1) return;

        List<String> entityTypes = CONFIG.renderEntities.getValueAsList();
        boolean renderInList = CONFIG.renderInList.getValue();
        int radius2 = radius * radius;
        Vec3 cameraPos = client.getCameraEntity().position();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (radius != -1 && entity.distanceToSqr(cameraPos) > radius2) {
                continue;
            }
            if (renderInList && !entityTypes.contains(entity.getType().toShortString())) {
                continue;
            }

            renderEntities.add(entity);
        }   
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        Message.clientSideMsg(T.tl("highlight.help.message"));
        return 1;
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var cmd = dispatcher.register(ClientCommandManager.literal(MODULE_NAME)
                .executes(Highlight::showHelp)
                .then(ClientCommandManager.literal("on")
                    .executes(Highlight::enable))
                .then(ClientCommandManager.literal("off")
                    .executes(Highlight::disable))
                .then(ClientCommandManager.literal("help")
                    .executes(Highlight::showHelp))
                .then(ClientCommandManager.literal("config")
                    .executes(c -> {
                        GuiUtil.openYaclScreen(Makemoney.MOD_ID, MODULE_NAME);
                        return 1;
                    }))
                .then(ClientCommandManager.literal("radius")
                    .then(ClientCommandManager.argument("radius", IntegerArgumentType.integer(-1, 200))
                        .executes(c -> {
                            int radius = c.getArgument("radius", Integer.class);
                            Message.clientSideMsg(T.tl("highlight.radius.message", radius));
                            updateRenderEntity();
                            CONFIG.renderRadius.setValue(radius);
                            CONFIG.saveConfig();
                            return 1;
                        })))
            );

            dispatcher.register(ClientCommandManager.literal("hl")
                .executes(Highlight::showHelp)
                .redirect(cmd)
            );
        });
    }
}
