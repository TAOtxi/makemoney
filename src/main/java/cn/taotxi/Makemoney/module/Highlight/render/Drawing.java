package cn.taotxi.Makemoney.module.Highlight.render;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import cn.taotxi.Makemoney.module.Highlight.HighlightConfig;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class Drawing {
    private static final Map<String, Integer> colorMap = new HashMap<>();
    private static boolean colorful = false;

    public static void drawHighlightBox(WorldRenderContext context, Iterable<Entity> entityList) {
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        BufferBuilder buffer = CustomRenderPipeline.getInstance().getBuffer();

        boolean hasDrawn = false;
        for (Entity entity : entityList) {
            if (entity.isRemoved()) continue;
            if (entity == Minecraft.getInstance().player) continue;

            hasDrawn = true;
            ShapeRenderer.renderShape(
                matrices,
                buffer, 
                Shapes.create(entity.getBoundingBox()),
                0, 0, 0,  // offset
                getColor(entity),
                1 // width
            );
        }
        if (!hasDrawn) return;

        matrices.popPose();
        CustomRenderPipeline.getInstance().startDrawing();
    }

    public static void setColorful(boolean colorful) {
        Drawing.colorful = colorful;
    }

    private static int getColor(Entity entity) {
        if (!colorful) return colorMap.get("default");

        if (entity instanceof Monster) {
            return colorMap.get("monster");
        }
        if (entity instanceof AgeableMob) {
            return colorMap.get("friendly");
        }
        if (entity instanceof Player) {
            return colorMap.get("player");
        }
        if (entity instanceof ItemEntity) {
            return colorMap.get("item");
        }

        return colorMap.get("default");
    }

    public static void updateColorMap() {
        HighlightConfig CONFIG = HighlightConfig.getInstance();
        colorMap.put("default", HighlightConfig.RGBA_StrToARBG(CONFIG.defaultColor.getValue()));
        colorMap.put("monster", HighlightConfig.RGBA_StrToARBG(CONFIG.monsterColor.getValue()));
        colorMap.put("player", HighlightConfig.RGBA_StrToARBG(CONFIG.playerColor.getValue()));
        colorMap.put("friendly", HighlightConfig.RGBA_StrToARBG(CONFIG.friendlyColor.getValue()));
        colorMap.put("item", HighlightConfig.RGBA_StrToARBG(CONFIG.itemColor.getValue()));
        colorMap.put("neutral", HighlightConfig.RGBA_StrToARBG(CONFIG.neutralColor.getValue()));
    }
}
