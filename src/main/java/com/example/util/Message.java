package com.example.util;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.client.Minecraft;

public class Message {
    private static final Minecraft client = Minecraft.getInstance();

    public static void chatMsg(String msg) {
        if (client.player == null) return;
        Component component = Component.literal(msg);
        client.player.displayClientMessage(component, false);
    }

    public static void actionBarMsg(String msg) {
        Component component = Component.literal(msg);
        client.gui.setOverlayMessage(component, false);
    }

    public static void sendMessage(String msg) {
        if (client.player == null) return;
        if (msg.startsWith("/")) {
            msg = msg.substring(1);
            client.player.connection.sendCommand(msg);
        } else {
            client.player.connection.sendChat(msg);
        }
    }

    // public static void sendAllMessage(List<String> messages, int delay) {
    //     Timer timer = new Timer();
    //     for (int i = 0; i < messages.size(); i++) {
    //         final int index = i;
    //         timer.schedule(new TimerTask() {
    //             @Override
    //             public void run() {
    //                 sendMessage(messages.get(index));
    //             }
    //         }, i * delay);
    //     }
    // }
    
}
