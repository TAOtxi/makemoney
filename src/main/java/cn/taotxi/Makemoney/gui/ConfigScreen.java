package cn.taotxi.Makemoney.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.lwjgl.glfw.GLFW;
import com.google.common.base.Function;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepair;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepairConfigGui;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBox;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBoxConfigGui;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;


public class ConfigScreen {
    // TODO: 实现一个主设置页面，通过Mod menu打开。里面可以打开子模块的设置页面
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    AutoRepair.config.save();
                    EntityHighlightBox.config.save();
                    Makemoney.LOGGER.info("Config saved...");
                });

        // 钓鱼相关模块
        ConfigCategory.Builder fishingCategory = AutoRepairConfigGui.createFishingCategoryBuilder(parent);
        builder.category(fishingCategory.build());
        
        // 实体高亮模块
        ConfigCategory.Builder entityHighlightBoxCategory = EntityHighlightBoxConfigGui.createEntityHighlightBoxCategoryBuilder(parent);
        builder.category(entityHighlightBoxCategory.build());

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
