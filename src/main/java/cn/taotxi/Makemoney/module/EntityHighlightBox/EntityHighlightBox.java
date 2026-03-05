package cn.taotxi.Makemoney.module.EntityHighlightBox;

import cn.taotxi.Makemoney.module.EntityHighlightBox.render.Highlighter;
import cn.taotxi.Makemoney.util.MLogger;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;


public class EntityHighlightBox {
    public static final String MODULE_NAME = "entityhighlightbox";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static HighlightConfig config = HighlightConfig.load(HighlightConfig.class, MODULE_NAME);

    public static void init() {
        // registerTickEvents();
        registerRenderEvents();
    }

    private static void registerRenderEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!config.enabled) return;
            Highlighter.drawHighlightBox(context);
        });
    }

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        // if (client.player == null || client.level == null) return;
        if (!config.enabled) return;
        if (tickCounter % config.updateInterval != 0) return;
        Highlighter.updateRenderEntities();
    }
}
