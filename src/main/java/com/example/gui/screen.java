package com.example.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;

import com.example.util.T;

import java.util.ArrayList;

import com.example.Makemoney;
import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoCommand.AutoCommandConfig;
import com.example.module.AutoCommand.AutoCommandConfig.CommandBlock;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.AutoRepair.AutoRepairConfig;

public class screen {
    public static Screen getConfigScreen(Screen parent) {
        
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("makemoney.gui.config.title"))
                .save(() -> {
                    AutoRepair.config.save();
                    AutoCommand.config.save();
                    AutoCommand.updateTickCounter();
                    Makemoney.LOGGER.info("Config saved.");
                });

        // 钓鱼相关模块
        ConfigCategory.Builder fishingCategory = createFishingCategoryBuilder(parent);
        builder.category(fishingCategory.build());

        // 自动命令模块
        ConfigCategory.Builder commandCategory = createCommandCategoryBuilder(parent);
        builder.category(commandCategory.build());


        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }

    public static ConfigCategory.Builder createFishingCategoryBuilder(Screen parent) {
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

        fishingCategory.group(autorepairGroup.build());
        return fishingCategory;
    }

    public static ConfigCategory.Builder createCommandCategoryBuilder(Screen parent) {
        ConfigCategory.Builder autocommandCategory = ConfigCategory.createBuilder()
                .name(T.tl("makemoney.autocommand.name"))
                .tooltip(T.tl("makemoney.autocommand.desc"));

        autocommandCategory.option(Option.<Boolean>createBuilder()
                .name(T.tl("makemoney.autocommand.enabled"))
                .description(OptionDescription.of(T.tl("makemoney.autocommand.enabled.desc")))
                .binding(
                    AutoCommandConfig.getDefaultEnabled(),
                    () -> AutoCommand.config.enabled,
                    val -> AutoCommand.config.enabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autocommandCategory.option(ButtonOption.createBuilder()
                .name(T.tl("makemoney.autocommand.addBlock")
                       .withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("makemoney.autocommand.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoCommand.config.addCommandBlock();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        for (int i = 0; i < AutoCommand.config.commandBlocks.size(); i++) {
            CommandBlock block = AutoCommand.config.commandBlocks.get(i);
            OptionGroup.Builder blockGroup = OptionGroup.createBuilder()
                    .name(block.name.isEmpty() ? T.tl("makemoney.autocommand.block.defaultName", i+1) : T.l(block.name))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.defaultName.desc", i+1)));
            
            blockGroup.option(Option.<Boolean>createBuilder()
                    .name(T.tl("makemoney.autocommand.block.enabled"))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.enabled.desc")))
                    .binding(
                            AutoCommandConfig.getDefaultEnabled(),
                            () -> block.enabled,
                            val -> {
                                block.enabled = val;
                                block.isUpdate = true;
                            }
                    )
                    .controller(BooleanControllerBuilder::create)
                    .build()
            );

            blockGroup.option(Option.<String>createBuilder()
                    .name(T.tl("makemoney.autocommand.block.name"))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.name.desc")))
                    .binding(
                            AutoCommandConfig.getDefaultName(),
                            () -> block.name,
                            val -> {
                                block.name = val;
                                block.isUpdate = true;
                            }
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            blockGroup.option(Option.<String>createBuilder()
                    .name(T.tl("makemoney.autocommand.block.ip"))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.ip.desc")))
                    .binding(
                            AutoCommandConfig.getDefaultIp(),
                            () -> block.ip,
                            val -> {
                                block.ip = val;
                                block.isUpdate = true;
                            }
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            blockGroup.option(Option.<String>createBuilder()
                    .name(T.tl("makemoney.autocommand.block.worldName"))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.worldName.desc")))
                    .binding(
                            AutoCommandConfig.getDefaultWorldName(),
                            () -> block.worldName,
                            val -> {
                                block.worldName = val;
                                block.isUpdate = true;
                            }
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                );

            blockGroup.option(ButtonOption.createBuilder()
                    .name(T.tl("makemoney.autocommand.block.runCounts.reset")
                            .withStyle(ChatFormatting.RED))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.runCounts.reset.desc")))
                    .action((yaclScreen, button) -> {
                        block.isUpdate = true;
                    })
                    .build()
                );

            blockGroup.option(Option.<Integer>createBuilder()
                    .name(T.tl("makemoney.autocommand.block.runCounts"))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.runCounts.desc")))
                    .binding(
                            AutoCommandConfig.getDefaultRunCounts(),
                            () -> block.runCounts,
                            val -> {
                                block.runCounts = val;
                                block.isUpdate = true;
                            }
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
            );

            blockGroup.option(Option.<Integer>createBuilder()
                    .name(T.tl("makemoney.autocommand.block.delay"))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.delay.desc")))
                    .binding(
                            AutoCommandConfig.getDefaultDelay(),
                            () -> block.delay,
                            val -> {
                                block.delay = val;
                                block.isUpdate = true;
                            }
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
            );


            
            blockGroup.option(ButtonOption.createBuilder()
            .name(T.tl("makemoney.autocommand.block.delete")
                    .withStyle(ChatFormatting.RED))
            .description(OptionDescription.of(T.tl("makemoney.autocommand.block.delete.desc")))
            .action((yaclScreen, button) -> {
                AutoCommand.config.removeCommandBlock(block);
                reload(yaclScreen, parent);
            })
            .build()
            );
        
            autocommandCategory.group(blockGroup.build());
            autocommandCategory.group(ListOption.<String>createBuilder()
                    .name(T.tl("makemoney.autocommand.block.command"))
                    .description(OptionDescription.of(T.tl("makemoney.autocommand.block.command.desc")))
                    .binding(
                            new ArrayList<String>(),
                            () -> block.commands,
                            val -> {
                                block.commands = val;
                                block.isUpdate = true;
                            }
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        return autocommandCategory;
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
