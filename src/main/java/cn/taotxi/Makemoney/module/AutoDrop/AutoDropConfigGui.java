package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.util.StringUtil;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.game.InventoryUtil;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;

public class AutoDropConfigGui {

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("autodrop.name"))
                .save(() -> {
                    AutoDrop.config.save();
                    AutoDrop.LOGGER.info("Config saved...");
                });

        ConfigCategory.Builder baseSettingCategory = createbaseSetting(parent);
        ConfigCategory.Builder controlCategory = createControlCategory(parent);
        ConfigCategory.Builder conditionCategory = createConditionCategory(parent);

        builder.category(baseSettingCategory.build());
        builder.category(controlCategory.build());
        builder.category(conditionCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }

    public static ConfigCategory.Builder createbaseSetting(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.baseTab"));


        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            AutoDropConfig.getDefaultEnabled(),
            () -> AutoDrop.config.enabled,
            val -> AutoDrop.config.enabled = val
        ));

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
                    if (Minecraft.getInstance().player == null) return;
                    AutoDrop.config.ingnoreSlots = InventoryUtil.getInventoryNotEmptySlots();

                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent, false, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.ignoreSlots"))
                .description(OptionDescription.createBuilder()
                    .text(T.tl("autodrop.ignoreSlots.desc"))
                    // TODO: 图片大小待优化或找其它替代方式
                    .webpImage(Identifier.fromNamespaceAndPath(Makemoney.MOD_ID, "images/slot_example.webp"))
                    .build())
                .binding(
                    AutoDropConfig.getDefaultIngnoreSlot(),
                    () -> StringUtil.listToStr(AutoDrop.config.ingnoreSlots),
                    val -> {
                        List<Integer> slots = StringUtil.strToIntList(val);
                        slots.sort(Comparator.naturalOrder());
                        for (int i=slots.size()-1; i>=0; i--) {
                            if (slots.get(i) < InventoryMenu.INV_SLOT_START ||
                                slots.get(i) >= InventoryMenu.USE_ROW_SLOT_END ||
                                (i > 0 && slots.get(i) == slots.get(i-1))) {
                                slots.remove(i);
                            }
                        }
                        AutoDrop.config.ingnoreSlots = slots;
                    }
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        category.option(Option.<Integer>createBuilder()
                .name(T.tl("autodrop.checkInterval"))
                .description(OptionDescription.of(T.tl("autodrop.checkInterval.desc")))
                .binding(
                    AutoDropConfig.getDefaultCheckInterval(),
                    () -> AutoDrop.config.checkInterval,
                    val -> AutoDrop.config.checkInterval = Math.clamp(val, 1, 10000)
                )
                .controller(IntegerFieldControllerBuilder::create)
                .build()
        );

        OptionGroup.Builder group = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.throw.name"))
                    .description(OptionDescription.of(T.tl("autodrop.throw.desc")));

        group.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.throw.setCurrentRotation"))
                .description(OptionDescription.of(T.tl("autodrop.throw.setCurrentRotation.desc")))
                .action((yaclScreen, button) -> {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player == null) return;
                    AutoDrop.config.throwYaw = player.getYRot();
                    AutoDrop.config.throwPitch = player.getXRot();
                    AutoDrop.config.isDirectionThrow = false;
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        var throwConfigText = AutoDrop.config.isDirectionThrow ? 
            T.tl("autodrop.throwDirection") : 
            T.tl("autodrop.throwRotation");
        group.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.throwType"))
                .text(throwConfigText)
                .description(OptionDescription.of(T.tl("autodrop.throwType.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.isDirectionThrow = !AutoDrop.config.isDirectionThrow;
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        if (AutoDrop.config.isDirectionThrow) {
            group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.throwDirection"))
                    .description(OptionDescription.of(T.tl("autodrop.throwDirection.desc")))
                    .binding(
                        AutoDropConfig.getDefaultThrowDirection(),
                        () -> AutoDrop.config.throwDirection,
                        val -> AutoDrop.config.throwDirection = val
                    )
                    .controller(opt -> DropdownStringControllerBuilder.create(opt)
                            .values(AutoDropConfig.getAllThrowDirections().stream().map(Enum::name).collect(Collectors.toList())))
                    .build()
            );
        } else {
            group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.throwRotation"))
                    .description(OptionDescription.of(T.tl("autodrop.throwRotation.desc")))
                    .binding(
                        "<0.0, 0.0>",
                        () -> StringUtil.posToString(
                            List.of(AutoDrop.config.throwYaw, AutoDrop.config.throwPitch)),
                        val -> {
                            List<Float> rotation = StringUtil.parseFloatPos(val);
                            if (rotation.size() != 2) return;
                            AutoDrop.config.throwYaw = rotation.get(0);
                            AutoDrop.config.throwPitch = rotation.get(1);
                        }
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        category.group(group.build());

        return category;
    }

    public static ConfigCategory.Builder createControlCategory(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.controlTab"));

        // TODO: 待实现
        // category.option(Factory.addToggleOption(
        //     T.tl("autodrop.triggerWhenPickUpItem"),
        //     T.tl("autodrop.triggerWhenPickUpItem.desc"),
        //     AutoDropConfig.getDefaultTriggerWhenPickup(),
        //     () -> AutoDrop.config.triggerWhenPickup,
        //     val -> AutoDrop.config.triggerWhenPickup = val
        // ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.turnOffWhenChangeWorld"),
            T.tl("autodrop.turnOffWhenChangeWorld.desc"),
            AutoDropConfig.getDefaultTurnOffWhenChangeWorld(),
            () -> AutoDrop.config.turnOffWhenChangeWorld,
            val -> AutoDrop.config.turnOffWhenChangeWorld = val
        ));

        category.option(Option.<Integer>createBuilder()
                    .name(T.tl("autodrop.triggerMinCount"))
                    .description(OptionDescription.of(T.tl("autodrop.triggerMinCount.desc")))
                    .binding(
                        AutoDropConfig.getDefaultTriggerMinCount(),
                        () -> AutoDrop.config.triggerMinCount,
                        val -> AutoDrop.config.triggerMinCount = val
                    )
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(0, 36)
                        .step(1))

                    .build()
        );

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenOpenContainer"),
            T.tl("autodrop.stopWhenOpenContainer.desc"),
            AutoDropConfig.getDefaultStopWhenOpenContainer(),
            () -> AutoDrop.config.stopWhenOpenContainer,
            val -> AutoDrop.config.stopWhenOpenContainer = val
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenOpenConfigGui"),
            T.tl("autodrop.stopWhenOpenConfigGui.desc"),
            AutoDropConfig.getDefaultStopWhenOpenConfig(),
            () -> AutoDrop.config.stopWhenOpenConfig,
            val -> AutoDrop.config.stopWhenOpenConfig = val
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenCrouch"),
            T.tl("autodrop.stopWhenCrouch.desc"),
            AutoDropConfig.getDefaultStopWhenCrouch(),
            () -> AutoDrop.config.stopWhenCrouch,
            val -> AutoDrop.config.stopWhenCrouch = val
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.triggerWithItem"),
            T.tl("autodrop.triggerWithItem.desc"),
            AutoDropConfig.getDefaultTriggerWithItem(),
            () -> AutoDrop.config.triggerWithItem,
            val -> AutoDrop.config.triggerWithItem = val
        ));

        OptionGroup.Builder group = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.triggerItemGroup"));
        
        group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.triggerItemName"))
                    .description(OptionDescription.of(
                            T.tl("autodrop.triggerItemName.desc")))
                    .binding(
                        "",
                        () -> AutoDrop.config.triggerItemName,
                        val -> AutoDrop.config.triggerItemName = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
        );

        group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.triggerItemId"))
                    .description(OptionDescription.of(
                            T.tl("autodrop.triggerItemId.desc")))
                    .binding(
                        "",
                        () -> AutoDrop.config.triggerItemId,
                        val -> AutoDrop.config.triggerItemId = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
        );
        category.group(group.build());

        return category;
    }

    public static ConfigCategory.Builder createConditionCategory(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.conditionTab"));

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addBlock").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.addItems();
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addPresetBlock").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addPresetBlock.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.addPresetItems();
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.removeAll").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.removeAll.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.items.clear();
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        for (AutoDropConfig.Item item : AutoDrop.config.items) {
            MutableComponent matchName = item.comment.isEmpty() ? T.tl("autodrop.block.name") : T.l(item.comment);
            OptionGroup.Builder whiteListGroup = OptionGroup.createBuilder()
                    .name(matchName)
                    .description(OptionDescription.of(T.tl("autodrop.block.name.desc")));
            

            whiteListGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.block.remove").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.block.remove.desc")))
                .action((yaclScreen, button) -> {
                    AutoDrop.config.removeItem(item);
                    AutoDrop.config.save();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
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

             whiteListGroup.option(Option.<Integer>createBuilder()
                    .name(T.tl("autodrop.block.minEnchantRequir"))
                    .description(OptionDescription.of(T.tl("autodrop.block.minEnchantRequir.desc")))
                    .binding(
                        AutoDropConfig.Item.getDefaultMinEnchantRequir(),
                        () -> item.minEnchantRequir,
                        val -> item.minEnchantRequir = val
                    )
                    .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .min(-1)
                        .max(100))
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
}
