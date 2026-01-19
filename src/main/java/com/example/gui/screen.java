package com.example.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;

import com.example.util.T;
import com.example.Makemoney;
import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.AutoRepair.AutoRepairConfig;

public class screen {
    public static Screen getConfigScreen(Screen parent) {
        
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("makemoney.gui.config.title"))
                .save(() -> {
                    AutoRepair.config.save();
                });

        ConfigCategory.Builder fishingCategory = 
            ConfigCategory.createBuilder()
                .name(T.tl("makemoney.gui.config.category.fishing.name"))
                .tooltip(T.tl("makemoney.gui.config.category.fishing.tooltip"));

        OptionGroup.Builder autorepairGroup = 
            OptionGroup.createBuilder()
                .name(T.tl("makemoney.autorepair.name"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.desc")));

        autorepairGroup.option(Option.<Boolean>createBuilder()
                .name(T.tl("makemoney.autorepair.enabled"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.desc")))
                .binding(
                    AutoRepairConfig.getDefaultEnabled(),
                    () -> AutoRepair.config.enabled,
                    val -> AutoRepair.config.enabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Boolean>createBuilder()
                .name(T.tl("makemoney.autorepair.showMessage"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.showMessage.desc")))
                .binding(
                    AutoRepairConfig.getDefaultShowMessage(),
                    () -> AutoRepair.config.showMessage,
                    val -> AutoRepair.config.showMessage = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Boolean>createBuilder()
                .name(T.tl("makemoney.autorepair.replaceEnabled"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.replaceEnabled.desc")))
                .binding(
                    AutoRepairConfig.getDefaultReplaceEnabled(),
                    () -> AutoRepair.config.replaceEnabled,
                    val -> AutoRepair.config.replaceEnabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("makemoney.autorepair.checkExpInterval"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.checkExpInterval.desc")))
                .binding(
                    AutoRepairConfig.getDefaultCheckExpInterval(),
                    () -> AutoRepair.config.checkExpInterval,
                    val -> AutoRepair.config.checkExpInterval = val
                )
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(1, 100)
                )
                .build()
        );  

        autorepairGroup.option(Option.<Double>createBuilder()
                .name(T.tl("makemoney.autorepair.expCheckBound"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.expCheckBound.desc")))
                .binding(
                    AutoRepairConfig.getDefaultExpCheckBound(),
                    () -> AutoRepair.config.expCheckBound,
                    val -> AutoRepair.config.expCheckBound = val
                )
                .controller(opt -> DoubleFieldControllerBuilder.create(opt)
                        .min(0.0d)
                        .max(128.0d)
                )
                .build()
        );

        autorepairGroup.option(Option.<Boolean>createBuilder()
                .name(T.tl("makemoney.autorepair.repairEnabled"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.repairEnabled.desc")))
                .binding(
                    AutoRepairConfig.getDefaultRepairEnabled(),
                    () -> AutoRepair.config.repairEnabled,
                    val -> AutoRepair.config.repairEnabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("makemoney.autorepair.repairInterval"))
                .description(OptionDescription.of(T.tl("makemoney.autorepair.repairInterval.desc")))
                .binding(
                    AutoRepairConfig.getDefaultRepairInterval(),
                    () -> AutoRepair.config.repairInterval,
                    val -> AutoRepair.config.repairInterval = val
                )
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(2, 100)
                )
                .build()
        );  


        ConfigCategory.Builder autocommandCategory = ConfigCategory.createBuilder()
                .name(T.tl("makemoney.autocommand.name"))
                .tooltip(T.tl("makemoney.autocommand.desc"));



        fishingCategory.group(autorepairGroup.build());

        builder.category(fishingCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }

    private static void reload(YACLScreen screen, Screen parent) {
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
