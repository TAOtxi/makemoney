package com.example.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import com.example.Makemoney;


public class Message {
    private static final Minecraft client = Minecraft.getInstance();
    
    public static void chatMsg(Component msg) {
        if (client.player == null) {
            Makemoney.LOGGER.error("Player is null");
            return;
        }
        client.player.displayClientMessage(msg, true);
    }

    public static void chatMsg(String msg) {
        chatMsg(Component.literal(msg));
    }

    public static void chatMsg(String... msgs) {
        StringBuilder sb = new StringBuilder();
        for (String str : msgs) {
            sb.append(str);
        }
        chatMsg(sb.toString());
    }

    public static void chatMsg(String msg1, Component msg2) {
        chatMsg(Component.literal(msg1).append(msg2));
    }

    public static void actionBarMsg(Component msg) {
        if (client.player == null) {
            Makemoney.LOGGER.error("Player is null");
            return;
        }
        client.gui.setOverlayMessage(msg, true);
    }

    public static void actionBarMsg(String msg) {
        actionBarMsg(Component.literal(msg));
    }

    public static void actionBarMsg(String... msgs) {
        StringBuilder sb = new StringBuilder();
        for (String str : msgs) {
            sb.append(str);
        }
        actionBarMsg(sb.toString());
    }

    public static void actionBarMsg(String msg1, Component msg2) {
        actionBarMsg(Component.literal(msg1).append(msg2));
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
