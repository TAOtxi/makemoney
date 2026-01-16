package com.example.util;

import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public class Message {
    private static final Minecraft client = Minecraft.getInstance();

    public static void chatMsg(String msg) {
        Component component = Component.literal(msg);
        client.player.displayClientMessage(component, false);
    }

    public static void subTitleMsg(String msg) {
        Component component = Component.literal(msg);
        client.gui.setOverlayMessage(component, false);
    }
    
}
