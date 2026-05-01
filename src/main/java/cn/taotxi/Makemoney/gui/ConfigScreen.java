package cn.taotxi.Makemoney.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.lwjgl.glfw.GLFW;
import com.google.common.base.Function;
import com.mojang.blaze3d.platform.Window;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepair;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepairConfigGui;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBox;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBoxConfigGui;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;


public class ConfigScreen {
    // TODO: 待完善
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    AutoRepair.config.save();
                    EntityHighlightBox.config.save();
                    Makemoney.LOGGER.info("Config saved...");
                });

        ConfigCategory.Builder moduleCategory = ConfigCategory.createBuilder()
                .name(T.tl("gui.config.module"));

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("gui.config.open.autodrop"))
                .text(T.tl("gui.config.open"))
                .action((screen, option) -> {
                    YACLScreen autodropScreem = (YACLScreen) AutoDropConfigGui.createScreen(screen);
                    Window window = Minecraft.getInstance().getWindow(); 
                    autodropScreem.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    Minecraft.getInstance().setScreen(autodropScreem);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("gui.config.open.autorepair"))
                .text(T.tl("gui.config.open"))
                .action((screen, option) -> {
                    YACLScreen autorepairScreem = (YACLScreen) AutoRepairConfigGui.createConfigScreen(screen);
                    Window window = Minecraft.getInstance().getWindow(); 
                    autorepairScreem.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    Minecraft.getInstance().setScreen(autorepairScreem);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("gui.config.open.entityhighlightbox"))
                .text(T.tl("gui.config.open"))
                .action((screen, option) -> {
                    YACLScreen entityhighlightboxScreen = (YACLScreen) EntityHighlightBoxConfigGui.createConfigScreen(screen);
                    Window window = Minecraft.getInstance().getWindow(); 
                    entityhighlightboxScreen.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    Minecraft.getInstance().setScreen(entityhighlightboxScreen);
                })
                .build()
        );

        builder.category(moduleCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }
    
    public static void reload(YACLScreen screen, Screen parent, Function<Screen, Screen> createConfigScreen) {
        Minecraft client = Minecraft.getInstance();
        double x = client.mouseHandler.xpos();
        double y = client.mouseHandler.ypos();
        try {
            int tab = screen.tabNavigationBar == null
                    ? 0
                    : screen.tabNavigationBar.getTabs().indexOf(screen.tabManager.getCurrentTab());
            if (tab == -1)
                tab = 0;
            screen.finishOrSave();
            screen.onClose(); // In case finishOrSave doesn't close it.
            YACLScreen newScreen = (YACLScreen) createConfigScreen.apply(parent);
            newScreen.init(screen.width, screen.height);
            try {
                newScreen.tabNavigationBar.selectTab(tab, false);
            } catch (IndexOutOfBoundsException e) {
                Makemoney.LOGGER.warn(
                        "YACL reload hack attempted to select tab {} but max index was {}",
                        tab,
                        newScreen.tabNavigationBar.getTabs().size() - 1
                );
            }
            client.setScreen(newScreen);
            GLFW.glfwSetCursorPos(client.getWindow().handle(), x, y);

        } catch (Exception e) {
            client.setScreen(parent);
            Makemoney.LOGGER.error("YACL reload hack failed with exception\n{}", e);
        }
    }

    public static boolean isOpenYaclScreen() {
        return Minecraft.getInstance().screen instanceof YACLScreen;
    }
}
