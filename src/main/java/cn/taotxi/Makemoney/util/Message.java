package cn.taotxi.Makemoney.util;

import cn.taotxi.Makemoney.Makemoney;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;


public class Message {
    private static final Minecraft client = Minecraft.getInstance();
    
    public static void clientSideMsg(Component msg) {
        if (client.player == null) {
            Makemoney.LOGGER.error("Player is null");
            return;
        }
        client.player.sendSystemMessage(msg);
    }

    public static void clientSideMsg(String msg) {
        clientSideMsg(Component.literal(msg));
    }

    public static void clientSideMsg(String... msgs) {
        StringBuilder sb = new StringBuilder();
        for (String str : msgs) {
            sb.append(str);
        }
        clientSideMsg(sb.toString());
    }

    public static void clientSideMsg(String msg1, Component msg2) {
        clientSideMsg(Component.literal(msg1).append(msg2));
    }

    public static void actionBarMsg(Component msg) {
        if (client.player == null) {
            Makemoney.LOGGER.error("Player is null");
            return;
        }
        client.gui.hud.setOverlayMessage(msg, false);
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
