package com.example.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityUtil {
    public static String getType(Entity entity) {
        return EntityType.getKey(entity.getType()).toString();
    }

    public static boolean equal(Entity entity, String entityType) {
        return getType(entity).equals(entityType);
    }
}
