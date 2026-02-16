package com.example.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;

import com.example.util.T;
import com.example.Makemoney;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.AutoRepair.AutoRepairConfigGui;
import com.example.module.AutoDrop.AutoDrop;
import com.example.module.AutoDrop.AutoDropConfigGui;
import com.example.module.EntityHighlightBox.EntityHighlightBox;
import com.example.module.EntityHighlightBox.EntityHighlightBoxConfigGui;


public class ConfigScreen {
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    // AutoRepair.config.save();
                    AutoDrop.config.save();
                    // EntityHighlightBox.config.save();
                    Makemoney.LOGGER.info("Config saved...");
                });

        // 钓鱼相关模块
        // ConfigCategory.Builder fishingCategory = AutoRepairConfigGui.createFishingCategoryBuilder(parent);
        // builder.category(fishingCategory.build());

        // 自动丢弃模块
        ConfigCategory.Builder autodropCategory = AutoDropConfigGui.createAutoDropCategoryBuilder(parent);
        builder.category(autodropCategory.build());
        
        // 实体高亮模块
        // ConfigCategory.Builder entityHighlightBoxCategory = EntityHighlightBoxConfigGui.createEntityHighlightBoxCategoryBuilder(parent);
        // builder.category(entityHighlightBoxCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }
    
    public static void reload(YACLScreen screen, Screen parent) {
        Minecraft client = Minecraft.getInstance();
        try {
            int tab = screen.tabNavigationBar == null
                    ? 0
                    : screen.tabNavigationBar.getTabs().indexOf(screen.tabManager.getCurrentTab());
            if (tab == -1)
                tab = 0;
            screen.finishOrSave();
            screen.onClose(); // In case finishOrSave doesn't close it.
            YACLScreen newScreen = (YACLScreen) getConfigScreen(parent);
            newScreen.init(client, screen.width, screen.height);
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
        } catch (Exception e) {
            client.setScreen(parent);
            Makemoney.LOGGER.error("YACL reload hack failed with exception\n{}", e);
        }
    }


}
