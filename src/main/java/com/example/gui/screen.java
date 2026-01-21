package com.example.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormattableController;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;

import com.example.util.Inventory;
import com.example.util.StringUtil;
import com.example.util.T;
import com.example.Makemoney;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.AutoRepair.AutoRepairConfig;
import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoCommand.AutoCommandConfig;
import com.example.module.AutoDrop.AutoDrop;
import com.example.module.AutoDrop.AutoDropConfig;
import com.example.module.AutoDrop.Droper;

public class screen {
    public static Screen getConfigScreen(Screen parent) {
        
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    AutoRepair.config.save();
                    AutoCommand.config.save();
                    AutoCommand.updateTickCounter();
                    AutoDrop.config.save();
                    Makemoney.LOGGER.info("Config saved...");
                });

        // 钓鱼相关模块
        ConfigCategory.Builder fishingCategory = createFishingCategoryBuilder(parent);
        builder.category(fishingCategory.build());

        // 自动丢弃模块
        ConfigCategory.Builder autodropCategory = createAutoDropCategoryBuilder(parent);
        builder.category(autodropCategory.build());

        // 自动命令模块
        ConfigCategory.Builder commandCategory = createCommandCategoryBuilder(parent);
        builder.category(commandCategory.build());
        
        // 实体高亮模块
        ConfigCategory.Builder entityHighlightBoxCategory = createEntityHighlightBoxCategory();
        builder.category(entityHighlightBoxCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }

    public static ConfigCategory.Builder createFishingCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("gui.config.category.fishing.name"))
                .tooltip(T.tl("gui.config.category.fishing.tooltip"));

        OptionGroup.Builder autorepairGroup = 
            OptionGroup.createBuilder()
                .name(T.tl("autorepair.name"))
                .description(OptionDescription.of(T.tl("autorepair.desc")));

        autorepairGroup.option(Option.<Boolean>createBuilder()
                .name(T.tl("module.enabled"))
                .description(OptionDescription.of(T.tl("autorepair.desc")))
                .binding(
                    AutoRepairConfig.getDefaultEnabled(),
                    () -> AutoRepair.config.enabled,
                    val -> AutoRepair.config.enabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Boolean>createBuilder()
                .name(T.tl("autorepair.replaceEnabled"))
                .description(OptionDescription.of(T.tl("autorepair.replaceEnabled.desc")))
                .binding(
                    AutoRepairConfig.getDefaultReplaceEnabled(),
                    () -> AutoRepair.config.replaceEnabled,
                    val -> AutoRepair.config.replaceEnabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Boolean>createBuilder()
                .name(T.tl("autorepair.showMessage"))
                .description(OptionDescription.of(T.tl("autorepair.showMessage.desc")))
                .binding(
                    AutoRepairConfig.getDefaultShowMessage(),
                    () -> AutoRepair.config.showMessage,
                    val -> AutoRepair.config.showMessage = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autorepair.checkExpInterval"))
                .description(OptionDescription.of(T.tl("autorepair.checkExpInterval.desc")))
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
                .name(T.tl("autorepair.expCheckBound"))
                .description(OptionDescription.of(T.tl("autorepair.expCheckBound.desc")))
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
                .name(T.tl("autorepair.repairEnabled"))
                .description(OptionDescription.of(T.tl("autorepair.repairEnabled.desc")))
                .binding(
                    AutoRepairConfig.getDefaultRepairEnabled(),
                    () -> AutoRepair.config.repairEnabled,
                    val -> AutoRepair.config.repairEnabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        autorepairGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autorepair.repairInterval"))
                .description(OptionDescription.of(T.tl("autorepair.repairInterval.desc")))
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

        category.group(autorepairGroup.build());
        return category;
    }

    public static ConfigCategory.Builder createAutoDropCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.name"))
                .tooltip(T.tl("autodrop.desc"));

        category.option(Option.<Boolean>createBuilder()
                .name(T.tl("module.enabled"))
                .description(OptionDescription.of(T.tl("module.enabled.desc")))
                .binding(
                    AutoDropConfig.getDefaultEnabled(),
                    () -> AutoDrop.config.enabled,
                    val -> AutoDrop.config.enabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.ignoreCurrentSlot"))
                .description(OptionDescription.of(T.tl("autodrop.ignoreCurrentSlot.desc")))
                .action((yaclScreen, button) -> {   
                    /**
                     * TODO: 待修Bug：重置ignoreSlots之后，需要点两次此按钮才能生效。
                     * 原因：
                     *  重置ignoreSlots时，ignoreSlots被置空的任务添加到待做任务队列中，当
                     *  点击该按钮时，首先会将一些槽位添加进配置对象中，随后reload时，会调用保存函数，
                     *  也就是将执行待做任务队列中的任务，但此时的待做任务是将ignoreSlots置空的任务，
                     *  因此ignoreSlots被覆盖掉了，也就是置空，唉。没想到有啥好解决的办法。
                     */
                    List<Integer> slots = Inventory.getInventoryNotEmptySlots();
                    AutoDrop.config.ingnoreSlots.addAll(slots);

                    // 去重
                    AutoDrop.config.ingnoreSlots = 
                        AutoDrop.config.ingnoreSlots.stream()
                           .distinct()
                           .collect(Collectors.toList());

                    // AutoDrop.config.save();
                    reload(yaclScreen, parent); // TODO: 需要确认reload是否会保存更改的配置
                })
                .build()
        );

        category.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.ignoreSlots"))
                .description(OptionDescription.of(T.tl("autodrop.ignoreSlots.desc")))
                .binding(
                    AutoDropConfig.getDefaultIngnoreSlot(),
                    () -> StringUtil.listToString(AutoDrop.config.ingnoreSlots, ", "),
                    val -> { 
                        val = val.replace(" ", "")
                                 .replace("，", ",");
                        AutoDrop.config.ingnoreSlots = StringUtil.strToIntList(val, ", ");
                    }
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        category.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.throwDirection"))
                .description(OptionDescription.of(T.tl("autodrop.throwDirection.desc")))
                .binding(
                    AutoDropConfig.getDefaultThrowDirection(),
                    () -> AutoDrop.config.throwDirection,
                    val -> AutoDrop.config.throwDirection = val
                )
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .values(AutoDropConfig.getAllThrowDirections()))
                .build()
        );

        category.option(Option.<Integer>createBuilder()
                .name(T.tl("autodrop.checkInterval"))
                .description(OptionDescription.of(T.tl("autodrop.checkInterval.desc")))
                .binding(
                    AutoDropConfig.getDefaultCheckInterval(),
                    () -> AutoDrop.config.checkInterval,
                    val -> AutoDrop.config.checkInterval = val
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(5, 10000)
                        .step(1)
                        .formatValue(val -> T.l(val + " tick")))
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addBlock"))
                .description(OptionDescription.of(T.tl("autodrop.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.addItems();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        for (int i = 0; i < AutoDrop.config.items.size(); i++) {
            AutoDropConfig.Item item = AutoDrop.config.items.get(i);
            OptionGroup.Builder whiteListGroup = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.block.name", i+1))
                    .description(OptionDescription.of(T.tl("autodrop.block.name.desc", i+1)));
            
            whiteListGroup.option(Option.<Boolean>createBuilder()
                    .name(T.tl("autodrop.block.enabled"))
                    .description(OptionDescription.of(T.tl("autodrop.block.enabled.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultItemEnabled(),
                        () -> item.enabled,
                        val -> item.enabled = val
                    )
                    .controller(BooleanControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.block.itemName"))
                    .description(OptionDescription.of(T.tl("autodrop.block.itemName.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultName(),
                        () -> item.name,
                        val -> item.name = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.block.itemID"))
                    .description(OptionDescription.of(T.tl("autodrop.block.itemID.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultID(),
                        () -> item.id,
                        val -> item.id = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.block.itemTags"))
                    .description(OptionDescription.of(T.tl("autodrop.block.itemTags.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultTag(),
                        () -> StringUtil.listToString(item.tags, ", "),
                        val -> {
                            val = val.replace(" ", "")
                                     .replace("，", ",");
                            item.tags = StringUtil.strToList(val, ",");
                        }
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<Boolean>createBuilder()
                    .name(T.tl("autodrop.block.isAllEnchantment"))
                    .description(OptionDescription.of(T.tl("autodrop.block.isAllEnchantment.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultIsAllEnchantment(),
                        () -> item.isAllEnchantment,
                        val -> item.isAllEnchantment = val
                    )
                    .controller(BooleanControllerBuilder::create)
                    .build()
            );
            category.group(whiteListGroup.build());
            category.group(ListOption.<String>createBuilder()
                    .name(T.tl("autodrop.block.enchantment.name"))
                    .description(OptionDescription.of(T.tl("autodrop.block.enchantment.name.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultEnchantments(),
                        () -> item.getEnchantList(),
                        val -> item.saveEnchantList(val)
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        return category;
    }

    public static ConfigCategory.Builder createCommandCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autocommand.name"))
                .tooltip(T.tl("autocommand.desc"));

        category.option(Option.<Boolean>createBuilder()
                .name(T.tl("module.enabled"))
                .description(OptionDescription.of(T.tl("module.enabled.desc")))
                .binding(
                    AutoCommandConfig.getDefaultEnabled(),
                    () -> AutoCommand.config.enabled,
                    val -> AutoCommand.config.enabled = val
                )
                .controller(BooleanControllerBuilder::create)
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autocommand.addBlock")
                       .withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autocommand.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoCommand.config.addCommandBlock();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        for (int i = 0; i < AutoCommand.config.commandBlocks.size(); i++) {
            AutoCommandConfig.CommandBlock block = AutoCommand.config.commandBlocks.get(i);
            OptionGroup.Builder blockGroup = OptionGroup.createBuilder()
                    .name(block.name.isEmpty() ? T.tl("autocommand.block.defaultName", i+1) : T.l(block.name))
                    .description(OptionDescription.of(T.tl("autocommand.block.defaultName.desc", i+1)));
            
            blockGroup.option(Option.<Boolean>createBuilder()
                    .name(T.tl("autocommand.block.enabled"))
                    .description(OptionDescription.of(T.tl("autocommand.block.enabled.desc")))
                    .binding(
                            AutoCommandConfig.CommandBlock.getDefaultEnabled(),
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
                    .name(T.tl("autocommand.block.name"))
                    .description(OptionDescription.of(T.tl("autocommand.block.name.desc")))
                    .binding(
                            AutoCommandConfig.CommandBlock.getDefaultName(),
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
                    .name(T.tl("autocommand.block.ip"))
                    .description(OptionDescription.of(T.tl("autocommand.block.ip.desc")))
                    .binding(
                            AutoCommandConfig.CommandBlock.getDefaultIp(),
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
                    .name(T.tl("autocommand.block.worldName"))
                    .description(OptionDescription.of(T.tl("autocommand.block.worldName.desc")))
                    .binding(
                            AutoCommandConfig.CommandBlock.getDefaultWorldName(),
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
                    .name(T.tl("autocommand.block.runCounts.reset")
                            .withStyle(ChatFormatting.RED))
                    .description(OptionDescription.of(T.tl("autocommand.block.runCounts.reset.desc")))
                    .action((yaclScreen, button) -> {
                        block.isUpdate = true;
                    })
                    .build()
                );

            blockGroup.option(Option.<Integer>createBuilder()
                    .name(T.tl("autocommand.block.runCounts"))
                    .description(OptionDescription.of(T.tl("autocommand.block.runCounts.desc")))
                    .binding(
                            AutoCommandConfig.CommandBlock.getDefaultRunCounts(),
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
                    .name(T.tl("autocommand.block.delay"))
                    .description(OptionDescription.of(T.tl("autocommand.block.delay.desc")))
                    .binding(
                            AutoCommandConfig.CommandBlock.getDefaultDelay(),
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
                .name(T.tl("autocommand.block.delete")
                        .withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autocommand.block.delete.desc")))
                .action((yaclScreen, button) -> {
                    AutoCommand.config.removeCommandBlock(block);
                    reload(yaclScreen, parent);
                })
                .build()
                );
        
            category.group(blockGroup.build());
            category.group(ListOption.<String>createBuilder()
                    .name(T.tl("autocommand.block.command"))
                    .description(OptionDescription.of(T.tl("autocommand.block.command.desc")))
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
        return category;
    }

    public static ConfigCategory.Builder createEntityHighlightBoxCategory() {
        ConfigCategory.Builder category = ConfigCategory.createBuilder()
                .name(T.tl("entityhighlightBox.name"))
                .tooltip(T.tl("entityhighlightBox.desc"));

        return category;
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
