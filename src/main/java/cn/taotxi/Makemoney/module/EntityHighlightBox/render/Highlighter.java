package cn.taotxi.Makemoney.module.EntityHighlightBox.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4fc;
import org.joml.Vector3f;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBox;
import cn.taotxi.Makemoney.module.EntityHighlightBox.HighlightConfig;
import cn.taotxi.Makemoney.util.EntityUtil;

public class Highlighter {
    private static List<Entity> entities = new ArrayList<>();

    public static List<Entity> updateRenderEntities() {
        Minecraft client = Minecraft.getInstance();
        AABB searchBox = client.player.getBoundingBox().inflate(EntityHighlightBox.config.renderRadius);
        entities = client.level.getEntities(client.player, searchBox, Highlighter::shouldRender);

        if (
            EntityHighlightBox.config.renderMaxCounts >= 0 &&
            entities.size() > EntityHighlightBox.config.renderMaxCounts
        ) {
            entities.sort((e1, e2) -> {
                double d1 = e1.distanceToSqr(client.player);
                double d2 = e2.distanceToSqr(client.player);
                return Double.compare(d1, d2);
            });
            entities = entities.subList(0, EntityHighlightBox.config.renderMaxCounts + 1);
        }
        return entities;
    }

    // TODO: 写一个自己的渲染工具
    public static void drawHighlightBox(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (entities.isEmpty()) return;

        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        BufferBuilder buffer = CustomRenderPipeline.getBuffer(CustomRenderPipeline.FILLED_THROUGH_WALLS);

        for (Entity entity : entities) {
            if (entity.isRemoved()) continue;

            Vector3f color = getColor(entity);
            renderBox(entity, matrices.last().pose(), buffer, color.x, color.y, color.z, 1);
        }
        matrices.popPose();
        CustomRenderPipeline.drawFilledThroughWalls(client, CustomRenderPipeline.FILLED_THROUGH_WALLS, buffer);
    }

    private static void renderBox(Entity entity, Matrix4fc positionMatrix, BufferBuilder buffer, 
            float red, float green, float blue, float alpha) {
        AABB box = entity.getBoundingBox().inflate(0.1);
        buffer.addVertex(positionMatrix, (float)box.minX, (float)box.minY, (float)box.maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).setColor(red, green, blue, alpha);
    }

    private static boolean shouldRender(Entity entity) {
        if (entity.isRemoved() || entity.isInvisible()) {
            return false;
        }
        
        String type = EntityUtil.getType(entity);
        boolean isContain = EntityHighlightBox.config.entityTypes.contains(type);

        return !(isContain ^ EntityHighlightBox.config.isWhitelist);
    }

    private static Vector3f getColor(Entity entity) {
        if (!EntityHighlightBox.config.colorful) {
            String redHex = HighlightConfig.getDefaultMonsterColor();
            return colorHexToInt(redHex);
        }

        // TODO: 确认是否完全覆盖原版生物
        if (entity instanceof Monster) {
            return colorHexToInt(EntityHighlightBox.config.monsterColor);
        } else if (entity instanceof AgeableMob) {
            return colorHexToInt(EntityHighlightBox.config.friendColor);
        } else if (entity instanceof Player) {
            return colorHexToInt(EntityHighlightBox.config.playerColor);
        } else if (entity instanceof LivingEntity) {
            return colorHexToInt(EntityHighlightBox.config.neutralColor);
        }
        
        // EntityHighlightBox.LOGGER.warn("Unknown entity type: " + entity.getType().toString());
        return colorHexToInt(EntityHighlightBox.config.unknownColor);
    }

    private static Vector3f colorHexToInt(String colorHex) {
        if (!colorHex.matches("#[0-9A-Fa-f]{6}")) {
            return colorHexToInt(HighlightConfig.getDefaultUnknownColor());
        }
        colorHex = colorHex.substring(1);
        int color = Integer.parseUnsignedInt(colorHex, 16);
        float red = (color >> 16 & 255) / 255.0f;
        float green = (color >> 8 & 255) / 255.0f;
        float blue = (color & 255) / 255.0f;
        return new Vector3f(red, green, blue);
    }
    
}
