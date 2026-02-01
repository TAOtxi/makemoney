package com.example.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.awt.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormattableController;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;

import com.example.util.InventoryUtil;
import com.example.util.StringUtil;
import com.example.util.T;
import com.example.Makemoney;
import com.example.module.AutoRepair.AutoRepair;
import com.example.module.AutoRepair.AutoRepairConfig;
import com.example.module.AutoDrop.AutoDrop;
import com.example.module.AutoDrop.AutoDropConfig;
import com.example.module.AutoCommand.AutoCommand;
import com.example.module.AutoCommand.AutoCommandConfig;
import com.example.module.EntityHighlightBox.EntityHighlightBox;
import com.example.module.EntityHighlightBox.HighlightConfig;

// TODO: 将类别写成模块，代码分离到模组各自模块中
public class ConfigScreen {
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    AutoRepair.config.save();
                    AutoCommand.config.save();
                    // AutoCommand.updateTickCounter();
                    AutoDrop.config.save();
                    EntityHighlightBox.config.save();
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
        ConfigCategory.Builder entityHighlightBoxCategory = createEntityHighlightBoxCategory(parent);
        builder.category(entityHighlightBoxCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }

    public static ConfigCategory.Builder createFishingCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("gui.config.category.fishing.name"))
                .tooltip(T.tl("gui.config.category.fishing.tooltip"));

        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            AutoRepairConfig.getDefaultEnabled(),
            () -> AutoRepair.config.enabled,
            val -> AutoRepair.config.enabled = val
        ));

        OptionGroup.Builder replaceGroup = 
            OptionGroup.createBuilder()
                .name(T.tl("autorepair.replace.name"))
                .description(OptionDescription.of(T.tl("autorepair.replace.desc")));

        replaceGroup.option(Factory.addToggleOption(
            T.tl("autorepair.replaceEnabled"),
            T.tl("autorepair.replaceEnabled.desc"),
            AutoRepairConfig.getDefaultReplaceEnabled(),
            () -> AutoRepair.config.replaceEnabled,
            val -> AutoRepair.config.replaceEnabled = val
        ));

        replaceGroup.option(Factory.addToggleOption(
            T.tl("autorepair.showMessage"),
            T.tl("autorepair.showMessage.desc"),
            AutoRepairConfig.getDefaultShowMessage(),
            () -> AutoRepair.config.showMessage,
            val -> AutoRepair.config.showMessage = val
        ));

        replaceGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autorepair.checkoffHandInterval"))
                .description(OptionDescription.of(T.tl("autorepair.checkoffHandInterval.desc")))
                .binding(
                    AutoRepairConfig.getDefaultCheckoffHandInterval(),
                    () -> AutoRepair.config.checkoffHandInterval,
                    val -> AutoRepair.config.checkoffHandInterval = val
                )
                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .range(1, 100)
                )
                .build()
        );
        category.group(replaceGroup.build());

        OptionGroup.Builder enchantGroup = 
            OptionGroup.createBuilder()
                .name(T.tl("autorepair.enchant.name"))
                .description(OptionDescription.of(T.tl("autorepair.enchant.desc")));

        enchantGroup.option(Factory.addToggleOption(
            T.tl("autorepair.repairEnabled"),
            T.tl("autorepair.repairEnabled.desc"),
            AutoRepairConfig.getDefaultRepairEnabled(),
            () -> AutoRepair.config.repairEnabled,
            val -> AutoRepair.config.repairEnabled = val
        ));

        enchantGroup.option(Option.<Integer>createBuilder()
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

        category.group(enchantGroup.build());
        return category;
    }

    public static ConfigCategory.Builder createAutoDropCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.name"))
                .tooltip(T.tl("autodrop.desc"));


        // TODO: 重新打开后，延迟计时器也归零
        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            AutoDropConfig.getDefaultEnabled(),
            () -> AutoDrop.config.enabled,
            val -> AutoDrop.config.enabled = val
        ));

        category.option(Option.<Integer>createBuilder()
                .name(T.tl("autodrop.launchDelay"))
                .description(OptionDescription.of(T.tl("autodrop.launchDelay.desc")))
                .binding(
                    AutoDropConfig.getDefaultLaunchDelay(),
                    () -> AutoDrop.config.launchDelay,
                    val -> AutoDrop.config.launchDelay = val
                )
                .controller(IntegerFieldControllerBuilder::create)
                .build()
        );

        category.option(Factory.addToggleOption(
            T.tl("autodrop.showAttentionMsg"),
            T.tl("autodrop.showAttentionMsg.desc"),
            AutoDropConfig.getDefaultShowAttentionMsg(),
            () -> AutoDrop.config.showAttentionMsg,
            val -> AutoDrop.config.showAttentionMsg = val
        ));

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
                    if (Minecraft.getInstance().player == null) return;
                    
                    List<Integer> slots = InventoryUtil.getInventoryNotEmptySlots();
                    AutoDrop.config.ingnoreSlots.addAll(slots);

                    // 去重
                    AutoDrop.config.ingnoreSlots = 
                        AutoDrop.config.ingnoreSlots.stream()
                           .distinct()
                           .collect(Collectors.toList());

                    AutoDrop.config.save();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        category.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.ignoreSlots"))
                .description(OptionDescription.createBuilder()
                    .text(T.tl("autodrop.ignoreSlots.desc"))
                    // TODO: 图片大小待优化或找其它替代方式
                    .webpImage(ResourceLocation.fromNamespaceAndPath(Makemoney.MOD_ID, "images/slot_example.webp"))
                    .build())
                .binding(
                    AutoDropConfig.getDefaultIngnoreSlot(),
                    () -> StringUtil.listToStr(AutoDrop.config.ingnoreSlots),
                    val -> AutoDrop.config.ingnoreSlots = StringUtil.strToIntList(val)
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        // TODO: 玩家自定义更精确的丢弃朝向
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
                        .range(1, 500)
                        .step(1)
                        .formatValue(val -> T.l(val + " tick")))
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addBlock").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.addItems();
                    AutoDrop.config.save();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addPresetBlock").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addPresetBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.addPresetItems();
                    AutoDrop.config.save();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        for (int i = 0; i < AutoDrop.config.items.size(); i++) {
            AutoDropConfig.Item item = AutoDrop.config.items.get(i);
            OptionGroup.Builder whiteListGroup = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.block.name", i+1))
                    .description(OptionDescription.of(T.tl("autodrop.block.name.desc", i+1)));
            

            whiteListGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.block.remove").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.block.remove.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.removeItem(item);
                    AutoDrop.config.save();
                    reload(yaclScreen, parent);
                })
                .build()
            );

             whiteListGroup.option(Factory.addToggleOption(
                T.tl("autodrop.block.enabled"),
                T.tl("autodrop.block.enabled.desc"),
                AutoDropConfig.Item.getDefaultItemEnabled(),
                () -> item.enabled,
                val -> item.enabled = val
            ));

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
                        () -> StringUtil.listToStr(item.tags),
                        val -> item.saveTags(val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            // TODO: 增加只匹配任意一种魔咒的选项
             whiteListGroup.option(Factory.addToggleOption(
                T.tl("autodrop.block.isAllEnchantment"),
                T.tl("autodrop.block.isAllEnchantment.desc"),
                AutoDropConfig.Item.getDefaultIsAllEnchantment(),
                () -> item.isAllEnchantment,
                val -> item.isAllEnchantment = val
            ));

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

        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            AutoCommandConfig.getDefaultEnabled(),
            () -> AutoCommand.config.enabled,
            val -> AutoCommand.config.enabled = val
        ));

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autocommand.addBlock")
                       .withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autocommand.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoCommand.config.addCommandBlock();
                    AutoCommand.config.save();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        for (int i = 0; i < AutoCommand.config.commandBlocks.size(); i++) {
            AutoCommandConfig.CommandBlock block = AutoCommand.config.commandBlocks.get(i);
            OptionGroup.Builder blockGroup = OptionGroup.createBuilder()
                    .name(block.name.isEmpty() ? T.tl("autocommand.block.defaultName", i+1) : T.l(block.name))
                    .description(OptionDescription.of(T.tl("autocommand.block.defaultName.desc", i+1)));
            
            blockGroup.option(Factory.addToggleOption(
                T.tl("autocommand.block.enabled"),
                T.tl("autocommand.block.enabled.desc"),
                AutoCommandConfig.CommandBlock.getDefaultEnabled(),
                () -> block.enabled,
                val -> {
                    block.enabled = val;
                    block.isUpdate = true;
                }
            ));

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
                    AutoCommand.config.save();
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

    public static ConfigCategory.Builder createEntityHighlightBoxCategory(Screen parent) {
        ConfigCategory.Builder category = ConfigCategory.createBuilder()
                .name(T.tl("entityhighlightBox.name"))
                .tooltip(T.tl("entityhighlightBox.desc"));

        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            HighlightConfig.getDefaultEnabled(),
            () -> EntityHighlightBox.config.enabled,
            val -> EntityHighlightBox.config.enabled = val
        ));

        // category.option(Factory.addToggleOption(
        //     T.tl("entityhighlightBox.colorful"),
        //     T.tl("entityhighlightBox.colorful.desc", HighlightConfig.getDefaultUnknownColor()),
        //     HighlightConfig.getDefaultColorful(),
        //     () -> EntityHighlightBox.config.colorful,
        //     val -> EntityHighlightBox.config.colorful = val
        // ));

        category.option(ButtonOption.createBuilder()
                .name(T.tl("entityhighlightBox.colorful"))
                .text(EntityHighlightBox.config.colorful ? 
                    T.tl("message.value.on").withStyle(ChatFormatting.GREEN) : 
                    T.tl("message.value.off").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("entityhighlightBox.colorful.desc", EntityHighlightBox.config.unknownColor)))
                .action((yaclScreen, button) -> {
                    EntityHighlightBox.config.colorful = !EntityHighlightBox.config.colorful;
                    EntityHighlightBox.config.save();
                    reload(yaclScreen, parent);
                })
                .build()
        );

        if (EntityHighlightBox.config.colorful) {
            OptionGroup.Builder colorGroup = 
                OptionGroup.createBuilder()
                    .name(T.tl("entityhighlightBox.color.group"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.color.group.desc")));

            colorGroup.option(Option.<Color>createBuilder()
                    .name(T.tl("entityhighlightBox.color.monster"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.color.monster.desc")))
                    .binding(
                            StringUtil.strToColor(HighlightConfig.getDefaultMonsterColor()),
                            () -> StringUtil.strToColor(EntityHighlightBox.config.monsterColor),
                            val -> EntityHighlightBox.config.monsterColor = StringUtil.colorToStr(val)
                    )
                    .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(true))
                    .build()
            );

            colorGroup.option(Option.<Color>createBuilder()
                    .name(T.tl("entityhighlightBox.color.friend"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.color.friend.desc")))
                    .binding(
                            StringUtil.strToColor(HighlightConfig.getDefaultFriendColor()),
                            () -> StringUtil.strToColor(EntityHighlightBox.config.friendColor),
                            val -> EntityHighlightBox.config.friendColor = StringUtil.colorToStr(val)
                    )
                    .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(true))
                    .build()
            );

            colorGroup.option(Option.<Color>createBuilder()
                    .name(T.tl("entityhighlightBox.color.neutral"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.color.neutral.desc")))
                    .binding(
                            StringUtil.strToColor(HighlightConfig.getDefaultNeutralColor()),
                            () -> StringUtil.strToColor(EntityHighlightBox.config.neutralColor),
                            val -> EntityHighlightBox.config.neutralColor = StringUtil.colorToStr(val)
                    )
                    .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(true))
                    .build()
            );

            colorGroup.option(Option.<Color>createBuilder()
                    .name(T.tl("entityhighlightBox.color.player"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.color.player.desc")))
                    .binding(
                            StringUtil.strToColor(HighlightConfig.getDefaultPlayerColor()),
                            () -> StringUtil.strToColor(EntityHighlightBox.config.playerColor),
                            val -> EntityHighlightBox.config.playerColor = StringUtil.colorToStr(val)
                    )
                    .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(true))
                    .build()
            );

            colorGroup.option(Option.<Color>createBuilder()
                    .name(T.tl("entityhighlightBox.color.unknown"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.color.unknown.desc")))
                    .binding(
                            StringUtil.strToColor(HighlightConfig.getDefaultUnknownColor()),
                            () -> StringUtil.strToColor(EntityHighlightBox.config.unknownColor),
                            val -> EntityHighlightBox.config.unknownColor = StringUtil.colorToStr(val)
                    )
                    .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(true))
                    .build()
            );

            category.group(colorGroup.build());
        }

        // TODO: 更改为三种可选值：白名单、黑名单、全部
        category.option(Option.<Boolean>createBuilder()
                    .name(T.tl("entityhighlightBox.isWhitelist"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.isWhitelist.desc")))
                    .binding(
                            HighlightConfig.getDefaultIsWhitelist(),
                            () -> EntityHighlightBox.config.isWhitelist,
                            val -> EntityHighlightBox.config.isWhitelist = val
                    )
                    .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? 
                            T.tl("entityhighlightBox.isWhitelist.true").withStyle(ChatFormatting.GREEN) : 
                            T.tl("entityhighlightBox.isWhitelist.false").withStyle(ChatFormatting.RED))
                        )
                    .build()
        );

        category.option(Factory.addToggleOption(
            T.tl("entityhighlightBox.isRenderName"),
            T.tl("entityhighlightBox.isRenderName.desc"),
            HighlightConfig.getDefaultIsRenderName(),
            () -> EntityHighlightBox.config.isRenderName,
            val -> EntityHighlightBox.config.isRenderName = val
        ));

        category.option(Option.<Integer>createBuilder()
                    .name(T.tl("entityhighlightBox.renderRadius"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.renderRadius.desc")))
                    .binding(
                            HighlightConfig.getDefaultRenderRadius(),
                            () -> EntityHighlightBox.config.renderRadius,
                            val -> EntityHighlightBox.config.renderRadius = val
                    )
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(1, 512)
                                .step(1))
                    .build()
        );

        category.option(Option.<Integer>createBuilder()
                    .name(T.tl("entityhighlightBox.renderMaxCounts"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.renderMaxCounts.desc")))
                    .binding(
                            HighlightConfig.getDefaultRenderMaxCounts(),
                            () -> EntityHighlightBox.config.renderMaxCounts,
                            val -> EntityHighlightBox.config.renderMaxCounts = val
                    )
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(1, 500)
                                .step(1))
                    .build()
        );

        category.option(Option.<Integer>createBuilder()
                    .name(T.tl("entityhighlightBox.updateInterval"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.updateInterval.desc")))
                    .binding(
                            HighlightConfig.getDefaultUpdateInterval(),
                            () -> EntityHighlightBox.config.updateInterval,
                            val -> EntityHighlightBox.config.updateInterval = val
                    )
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(1, 500)
                                .step(1)
                                .formatValue(val -> T.l(val + " tick")))
                    .build()
        );

        category.group(ListOption.<String>createBuilder()
                    .name(T.tl("entityhighlightBox.entityTypes"))
                    .description(OptionDescription.of(T.tl("entityhighlightBox.entityTypes.desc")))
                    .binding(
                            HighlightConfig.getDefaultEntityTypes(),
                            () -> EntityHighlightBox.config.entityTypes,
                            val -> EntityHighlightBox.config.entityTypes = val
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        
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
