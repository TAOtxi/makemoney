package cn.taotxi.Makemoney.module.Highlight;

import java.util.ArrayList;
import java.util.List;

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

        CONFIG.renderInList.onChange(
            (oldValue, newValue) -> {
                if (!TaskUtil.hasTimeTask(UPDATE_RENDER_ENTITY) && newValue) {
                    TaskUtil.createTimeTask(UPDATE_RENDER_ENTITY, Highlight::updateRenderEntity, 40);
                } else if (!newValue) {
                    TaskUtil.removeTimeTask(UPDATE_RENDER_ENTITY);
                }
            }
        );

        CONFIG.enabled.onChange(
            (oldValue, newValue) -> {
                if (!TaskUtil.hasTimeTask(UPDATE_RENDER_ENTITY) && newValue) {
                    TaskUtil.createTimeTask(UPDATE_RENDER_ENTITY, Highlight::updateRenderEntity, 40);
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

        if (CONFIG.enabled.getValue()) {
            enabled = true;
        }

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
            if (!enabled) return;
            Drawing.drawHighlightBox(context, getRenderEntities());
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((mc, level) -> {
            renderEntities.clear();
        });
    }

    private static int enable(CommandContext<FabricClientCommandSource> context) {
        enabled = true;
        Message.clientSideMsg(T.tl("highlight.on.message"));
        CONFIG.enabled.enable();
        CONFIG.saveConfig();
        return 1;
    }

    private static int disable(CommandContext<FabricClientCommandSource> context) {
        enabled = false;
        Message.clientSideMsg(T.tl("highlight.off.message"));
        CONFIG.enabled.disable();
        CONFIG.saveConfig();
        return 1;
    }

    private static Iterable<Entity> getRenderEntities() {
        if (!CONFIG.renderInList.getValue()) {
            return client.level.entitiesForRendering();
        }
        return renderEntities;
    }

    private static void updateRenderEntity() {
        renderEntities.clear();
        if (client.level == null) return;

        List<String> entityTypes = CONFIG.renderEntities.getValueAsList();
        if (entityTypes.isEmpty()) return;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entityTypes.contains(entity.getType().toShortString())) {
                renderEntities.add(entity);
            }
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
            );

            dispatcher.register(ClientCommandManager.literal("hl")
                .executes(Highlight::showHelp)
                .redirect(cmd)
            );
        });
    }
}
