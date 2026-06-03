package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.Comparator;
import java.util.List;

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
    private static final AutoDropConfig CONFIG = AutoDropConfig.getInstance();

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("autodrop.name"))
                .save(() -> {
                    CONFIG.saveConfig();
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
            T.tl("autodrop.on"),
            T.tl("autodrop.on.desc"),
            false,
            () -> AutoDrop.enabled,
            AutoDrop::toggleSwitch
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.showAttentionMsg"),
            T.tl("autodrop.showAttentionMsg.desc"),
            CONFIG.isShowAttentionMsg.getDefaultValue(),
            CONFIG.isShowAttentionMsg::getValue,
            CONFIG.isShowAttentionMsg::setValue
        ));

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.ignoreCurrentSlot"))
                .description(OptionDescription.of(T.tl("autodrop.ignoreCurrentSlot.desc")))
                .action((yaclScreen, button) -> {
                    if (Minecraft.getInstance().player == null) return;
                    CONFIG.ignoreSlots.setValue(InventoryUtil.getInventoryNotEmptySlots());
                    CONFIG.saveConfig();
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
                    StringUtil.join(CONFIG.ignoreSlots.getDefaultValue()),
                    () -> StringUtil.join(CONFIG.ignoreSlots.getValue()),
                    val -> {
                        List<Integer> slots = StringUtil.strToIntList(val);
                        slots.sort(Comparator.naturalOrder());
                        for (int i=slots.size()-1; i>0; i--) {
                            if (slots.get(i) < InventoryMenu.INV_SLOT_START ||
                                slots.get(i) >= InventoryMenu.USE_ROW_SLOT_END ||
                                slots.get(i) == slots.get(i-1)
                            ) {
                                slots.remove(i);
                            }
                        }
                        CONFIG.ignoreSlots.setValue(slots);
                    }
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        OptionGroup.Builder tiggerWayGroup = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.triggerWay"))
                    .description(OptionDescription.of(T.tl("autodrop.triggerWay.desc")));
        
        tiggerWayGroup.option(Factory.addToggleOption(
            T.tl("autodrop.timeTrigger"),
            T.tl("autodrop.timeTrigger.desc"),
            CONFIG.isTimeTrigger.getDefaultValue(),
            CONFIG.isTimeTrigger::getValue,
            CONFIG.isTimeTrigger::setValue
        ));

        tiggerWayGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autodrop.timeTriggerInterval"))
                .description(OptionDescription.of(T.tl("autodrop.timeTriggerInterval.desc")))
                .binding(
                    CONFIG.timeTriggerInterval.getDefaultValue(),
                    CONFIG.timeTriggerInterval::getValue,
                    CONFIG.timeTriggerInterval::setValue
                )
                .controller(IntegerFieldControllerBuilder::create)
                .build()
        );

        tiggerWayGroup.option(Factory.addToggleOption(
            T.tl("autodrop.pickUpItemTrigger"),
            T.tl("autodrop.pickUpItemTrigger.desc"),
            CONFIG.isPickUpItemTrigger.getDefaultValue(),
            CONFIG.isPickUpItemTrigger::getValue,
            CONFIG.isPickUpItemTrigger::setValue
        ));

        tiggerWayGroup.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.triggerItemId"))
                .description(OptionDescription.of(T.tl("autodrop.triggerItemId.desc")))
                .binding(
                    CONFIG.triggerItemId.getDefaultValue(),
                    CONFIG.triggerItemId::getValue,
                    CONFIG.triggerItemId::setValue
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        category.group(tiggerWayGroup.build());

        OptionGroup.Builder throwWayGroup = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.throw.name"))
                    .description(OptionDescription.of(T.tl("autodrop.throw.desc")));

        throwWayGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.throw.setCurrentRotation"))
                .description(OptionDescription.of(T.tl("autodrop.throw.setCurrentRotation.desc")))
                .action((yaclScreen, button) -> {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player == null) return;
                    CONFIG.throwYaw.setValue(Math.round(player.getYRot() * 100) / 100.0f);
                    CONFIG.throwPitch.setValue(Math.round(player.getXRot() * 100) / 100.0f);
                    CONFIG.throwWay.setValue(ThrowWay.ROTATION);
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        MutableComponent throwConfigText = CONFIG.throwWay.equals(ThrowWay.DIRECTION) ? 
            T.tl("autodrop.throwDirection") : 
            T.tl("autodrop.throwRotation");
        throwWayGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.throwType"))
                .text(throwConfigText)
                .description(OptionDescription.of(T.tl("autodrop.throwType.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.throwWay.setValue(
                        CONFIG.throwWay.equals(ThrowWay.DIRECTION) ? 
                        ThrowWay.ROTATION : ThrowWay.DIRECTION);
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        if (CONFIG.throwWay.equals(ThrowWay.DIRECTION)) {
            throwWayGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.throwDirection"))
                    .description(OptionDescription.of(T.tl("autodrop.throwDirection.desc")))
                    .binding(
                        CONFIG.throwDirection.getDefaultValue(),
                        CONFIG.throwDirection::getValue,
                        CONFIG.throwDirection::setValue
                    )
                    .controller(opt -> DropdownStringControllerBuilder.create(opt)
                            .values(CONFIG.getAllThrowDirections()))
                    .build()
            );
        } else {
            throwWayGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.throwRotation"))
                    .description(OptionDescription.of(T.tl("autodrop.throwRotation.desc")))
                    .binding(
                        "<0.0, 0.0>",
                        () -> StringUtil.posToString(
                            List.of(
                                CONFIG.throwYaw.getValue(), 
                                CONFIG.throwPitch.getValue()
                            )
                        ),
                        val -> {
                            List<Float> rotation = StringUtil.parseFloatPos(val);
                            if (rotation.size() != 2) return;
                            CONFIG.throwYaw.setValue(rotation.get(0));
                            CONFIG.throwPitch.setValue(rotation.get(1));
                        }
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        category.group(throwWayGroup.build());

        OptionGroup.Builder configManagerGroup = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.config.name"));

        configManagerGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.config.reset"))
                .description(OptionDescription.of(T.tl("autodrop.config.reset.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.resetConfig();
                    AutoDrop.enabled = false;
                    AutoDrop.onConfigChange();
                    ConfigScreen.reload(yaclScreen, parent, false, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        configManagerGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.config.reload"))
                .description(OptionDescription.of(T.tl("autodrop.config.reload.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.reloadConfig();
                    AutoDrop.onConfigChange();
                    ConfigScreen.reload(yaclScreen, parent, false, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.group(configManagerGroup.build());

        return category;
    }

    public static ConfigCategory.Builder createControlCategory(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.controlTab"));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.turnOffWhenChangeWorld"),
            T.tl("autodrop.turnOffWhenChangeWorld.desc"),
            CONFIG.turnOffWhenChangeWorld.getDefaultValue(),
            CONFIG.turnOffWhenChangeWorld::getValue,
            CONFIG.turnOffWhenChangeWorld::setValue
        ));

        category.option(Option.<Integer>createBuilder()
                    .name(T.tl("autodrop.triggerMinCount"))
                    .description(OptionDescription.of(T.tl("autodrop.triggerMinCount.desc")))
                    .binding(
                        CONFIG.triggerMinCount.getDefaultValue(),
                        CONFIG.triggerMinCount::getValue,
                        CONFIG.triggerMinCount::setValue
                    )
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(0, 36)
                        .step(1))

                    .build()
        );

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenOpenContainer"),
            T.tl("autodrop.stopWhenOpenContainer.desc"),
            CONFIG.stopWhenOpenContainer.getDefaultValue(),
            CONFIG.stopWhenOpenContainer::getValue,
            CONFIG.stopWhenOpenContainer::setValue
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenOpenConfigGui"),
            T.tl("autodrop.stopWhenOpenConfigGui.desc"),
            CONFIG.stopWhenOpenConfigGui.getDefaultValue(),
            CONFIG.stopWhenOpenConfigGui::getValue,
            CONFIG.stopWhenOpenConfigGui::setValue
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenCrouch"),
            T.tl("autodrop.stopWhenCrouch.desc"),
            CONFIG.stopWhenCrouch.getDefaultValue(),
            CONFIG.stopWhenCrouch::getValue,
            CONFIG.stopWhenCrouch::setValue
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenNotHoldingItem"),
            T.tl("autodrop.stopWhenNotHoldingItem.desc"),
            CONFIG.stopWhenNotHoldingItem.getDefaultValue(),
            CONFIG.stopWhenNotHoldingItem::getValue,
            CONFIG.stopWhenNotHoldingItem::setValue
        ));

        OptionGroup.Builder group = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.triggerHoldItemGroup"));
        
        group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.stopWhenNotHoldingItemName"))
                    .description(OptionDescription.of(
                            T.tl("autodrop.stopWhenNotHoldingItemName.desc")))
                    .binding(
                        CONFIG.stopWhenNotHoldingItemName.getDefaultValue(),
                        CONFIG.stopWhenNotHoldingItemName::getValue,
                        CONFIG.stopWhenNotHoldingItemName::setValue
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
        );

        group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.stopWhenNotHoldingItemId"))
                    .description(OptionDescription.of(
                            T.tl("autodrop.stopWhenNotHoldingItemId.desc")))
                    .binding(
                        CONFIG.stopWhenNotHoldingItemId.getDefaultValue(),
                        CONFIG.stopWhenNotHoldingItemId::getValue,
                        CONFIG.stopWhenNotHoldingItemId::setValue
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
                .name(T.tl("autodrop.addMatchItem").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addMatchItem.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.addMatchItem();
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addPresetMatchItem").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addPresetMatchItem.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.addPresetItems();
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.removeAll").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.removeAll.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.matchItemLists.resetValue();
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        Item DEFAULT_MATCH_ITEM = CONFIG.getDefaultMatchItem();
        for (int i=0; i<CONFIG.matchItemLists.size(); i++) {
            final int index = i;
            MutableComponent matchName = CONFIG.getMatchItemDescription(index).isEmpty() ? 
                T.tl("autodrop.matchItem.description") : 
                T.l(CONFIG.getMatchItemDescription(index));
            OptionGroup.Builder whiteListGroup = OptionGroup.createBuilder()
                    .name(matchName)
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.description.desc")));
            

            whiteListGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.matchItem.remove").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.matchItem.remove.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.removeMatchItem(index);
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
            );

             whiteListGroup.option(Factory.addToggleOption(
                T.tl("autodrop.matchItem.enabled"),
                T.tl("autodrop.matchItem.enabled.desc"),
                DEFAULT_MATCH_ITEM.enabled,
                () -> CONFIG.isMatchItemEnabled(index),
                val -> CONFIG.setMatchItemEnabled(index, val)
            ));

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.matchItem.itemName"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.itemName.desc")))
                    .binding(
                        DEFAULT_MATCH_ITEM.name,
                        () -> CONFIG.getMatchItemName(index),
                        val -> CONFIG.setMatchItemName(index, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.matchItem.itemID"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.itemID.desc")))
                    .binding(
                        DEFAULT_MATCH_ITEM.id,
                        () -> CONFIG.getMatchItemId(index),
                        val -> CONFIG.setMatchItemId(index, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.matchItem.itemTags"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.itemTags.desc")))
                    .binding(
                        StringUtil.join(DEFAULT_MATCH_ITEM.tags),
                        () -> StringUtil.join(CONFIG.getMatchItemTags(index)),
                        val -> CONFIG.setMatchItemTags(index, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

             whiteListGroup.option(Option.<Integer>createBuilder()
                    .name(T.tl("autodrop.matchItem.minEnchantRequir"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.minEnchantRequir.desc")))
                    .binding(
                        DEFAULT_MATCH_ITEM.minEnchantRequir,
                        () -> CONFIG.getMatchItemMinEnchantRequir(index),
                        val -> CONFIG.setMatchItemMinEnchantRequir(index, val)
                    )
                    .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                        .min(-1)
                        .max(100))
                    .build()
            );

            category.group(whiteListGroup.build());
            category.group(ListOption.<String>createBuilder()
                    .name(T.tl("autodrop.matchItem.enchantment.name"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.enchantment.name.desc")))
                    .binding(
                        Item.getEnchantmentsListStr(DEFAULT_MATCH_ITEM.enchantments),
                        () -> CONFIG.getEnchantList(index),
                        val -> CONFIG.setMatchItemEnchantments(index, val)
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        return category;
    }


}
