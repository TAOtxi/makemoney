package com.example.module.EntityHighlightBox;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import com.example.module.EntityHighlightBox.render.Highlighter;
import com.example.util.MLogger;


public class EntityHighlightBox {
    public static final String MODULE_NAME = "entityhighlightbox";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static final HighlightConfig config = HighlightConfig.load(HighlightConfig.class, MODULE_NAME);
    private static int tickCounter = 0;

    public static void init() {
        registerTickEvents();
        registerRenderEvents();
    }

    private static void registerRenderEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!config.enabled) return;
            Highlighter.drawHighlightBox(context);
        });
    }

    // TODO: 待整合各个模块的tick事件
    private static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            if (!config.enabled) return;
            if (tickCounter++ % config.updateInterval != 0) return;
            Highlighter.updateRenderEntities();
        });
    }

    // public static void registerTickEvents() {
    //     TickCounter ticker = new TickCounter();
    //     ticker.addTask(new TickCounter.Task(tick -> {
    //         LOGGER.info("ticking...");
    //     }, 40));
        
    //     ClientTickEvents.END_CLIENT_TICK.register(client -> {
    //         if (client.player == null || client.level == null) return;

    //         // ticker.run();
    //     });
    // }
}
