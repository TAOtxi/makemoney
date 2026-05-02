package cn.taotxi.Makemoney;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.Window;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.module.AutoAction.AutoAction;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepair;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBox;
import cn.taotxi.Makemoney.util.EventBus;
import dev.isxander.yacl3.gui.YACLScreen;


public class Makemoney implements ModInitializer {
	public static final String MOD_ID = "makemoney";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static int tickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Starting mod...");
        
        registerTickEvents();
        registerSomeEvents();
        registerCommand();
        AutoDrop.init();
	}

    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            AutoRepair.registerCommand(dispatcher, registryAccess);
            AutoDrop.registerCommand(dispatcher, registryAccess);
            // AutoAction.registerCommand(dispatcher, registryAccess);
        });
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) return;
            EventBus.checkQueue();
            
            tickCounter++;
            // AutoRepair.registerTickEvents(client, tickCounter);
            AutoDrop.registerTickEvents(client, tickCounter);
            // AutoAction.registerTickEvents(client, tickCounter);
            EntityHighlightBox.registerTickEvents(client, tickCounter);
        });
    }

    private void registerSomeEvents() {
        // ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
        //     Message.sendMessage("Screen: " + screen.getClass().getSimpleName());
        // });
        EntityHighlightBox.registerRenderEvents();
        EventBus.register("openConfigGui", (args) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                EventBus.post("openConfigGui", args);
                return;
            }
            
            YACLScreen configScreen = null;
            String module = (String) args.get("module");

            if (module.equals(AutoDrop.MODULE_NAME)) {
                configScreen = (YACLScreen) AutoDropConfigGui.createScreen(null);
            } else {
                LOGGER.error("Can not find module: " + module);
                return;
            }

            Window window = client.getWindow(); 
            configScreen.init(window.getGuiScaledWidth(), 
                    window.getGuiScaledHeight());
            client.setScreen(configScreen);
        });
    }
}