package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.JsonArray;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfig.AutoDropDefaultConfig;
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
    private static AutoDropDefaultConfig DEFAULT_CONFIG = null;
    private static AutoDropConfig config = null;

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("autodrop.name"))
                .save(() -> {
                    config.saveConfig();
                    AutoDrop.LOGGER.info("Config saved...");
                });

        if (DEFAULT_CONFIG == null) {
            DEFAULT_CONFIG = config.getDefaultConfig();
        }
        if (config == null) {
            config = AutoDropConfig.getInstance();
        }
        
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
            val -> AutoDrop.enabled = val
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.showAttentionMsg"),
            T.tl("autodrop.showAttentionMsg.desc"),
            DEFAULT_CONFIG.showAttentionMsg,
            config::isShowAttentionMsg,
            config::setShowAttentionMsg
        ));

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.ignoreCurrentSlot"))
                .description(OptionDescription.of(T.tl("autodrop.ignoreCurrentSlot.desc")))
                .action((yaclScreen, button) -> {
                    if (Minecraft.getInstance().player == null) return;
                    config.setIgnoreSlots(InventoryUtil.getInventoryNotEmptySlots());
                    config.saveConfig();
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
                    StringUtil.join(DEFAULT_CONFIG.ignoreSlots),
                    () -> StringUtil.join(config.getIgnoreSlots()),
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
                        config.setIgnoreSlots(slots);
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
            DEFAULT_CONFIG.timeTrigger,
            config::isTimeTrigger,
            config::setTimeTrigger
        ));

        tiggerWayGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autodrop.timeTriggerInterval"))
                .description(OptionDescription.of(T.tl("autodrop.timeTriggerInterval.desc")))
                .binding(
                    DEFAULT_CONFIG.timeTriggerInterval,
                    config::getTimeTriggerInterval,
                    config::setTimeTriggerInterval
                )
                .controller(IntegerFieldControllerBuilder::create)
                .build()
        );

        tiggerWayGroup.option(Factory.addToggleOption(
            T.tl("autodrop.pickUpItemTrigger"),
            T.tl("autodrop.pickUpItemTrigger.desc"),
            DEFAULT_CONFIG.pickUpItemTrigger,
            config::isPickUpItemTrigger,
            config::setPickUpItemTrigger
        ));

        tiggerWayGroup.option(Option.<String>createBuilder()
                .name(T.tl("autodrop.triggerItemId"))
                .description(OptionDescription.of(T.tl("autodrop.triggerItemId.desc")))
                .binding(
                    DEFAULT_CONFIG.triggerItemId,
                    config::getTriggerItemId,
                    config::setTriggerItemId
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
                    config.setThrowYaw(Math.round(player.getYRot() * 100) / 100.0f);
                    config.setThrowPitch(Math.round(player.getXRot() * 100) / 100.0f);
                    config.setThrowWay(ThrowWay.ROTATION);
                    config.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        MutableComponent throwConfigText = config.getThrowWay().equals(ThrowWay.DIRECTION) ? 
            T.tl("autodrop.throwDirection") : 
            T.tl("autodrop.throwRotation");
        throwWayGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.throwType"))
                .text(throwConfigText)
                .description(OptionDescription.of(T.tl("autodrop.throwType.desc")))
                .action((yaclScreen, button) -> {
                    config.setThrowWay(
                        config.getThrowWay().equals(ThrowWay.DIRECTION) ? 
                        ThrowWay.ROTATION : ThrowWay.DIRECTION);
                    config.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        if (config.getThrowWay().equals(ThrowWay.DIRECTION)) {
            throwWayGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.throwDirection"))
                    .description(OptionDescription.of(T.tl("autodrop.throwDirection.desc")))
                    .binding(
                        DEFAULT_CONFIG.throwDirection,
                        () -> config.getThrowDirection().name(),
                        config::setThrowDirection
                    )
                    .controller(opt -> DropdownStringControllerBuilder.create(opt)
                            .values(AutoDropConfig.getAllThrowDirections()))
                    .build()
            );
        } else {
            throwWayGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.throwRotation"))
                    .description(OptionDescription.of(T.tl("autodrop.throwRotation.desc")))
                    .binding(
                        "<0.0, 0.0>",
                        () -> StringUtil.posToString(
                            List.of(config.getThrowYaw(), 
                            config.getThrowPitch())
                        ),
                        val -> {
                            List<Float> rotation = StringUtil.parseFloatPos(val);
                            if (rotation.size() != 2) return;
                            config.setThrowYaw(rotation.get(0));
                            config.setThrowPitch(rotation.get(1));
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
                    config.resetConfig();
                    ConfigScreen.reload(yaclScreen, parent, false, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        configManagerGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.config.reload"))
                .description(OptionDescription.of(T.tl("autodrop.config.reload.desc")))
                .action((yaclScreen, button) -> {
                    config.reloadConfig();
                    ConfigScreen.reload(yaclScreen, parent, false, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        return category;
    }

    public static ConfigCategory.Builder createControlCategory(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autodrop.controlTab"));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.turnOffWhenChangeWorld"),
            T.tl("autodrop.turnOffWhenChangeWorld.desc"),
            DEFAULT_CONFIG.turnOffWhenChangeWorld,
            config::isTurnOffWhenChangeWorld,
            config::setTurnOffWhenChangeWorld
        ));

        category.option(Option.<Integer>createBuilder()
                    .name(T.tl("autodrop.triggerMinCount"))
                    .description(OptionDescription.of(T.tl("autodrop.triggerMinCount.desc")))
                    .binding(
                        DEFAULT_CONFIG.triggerMinCount,
                        config::getTriggerMinCount,
                        config::setTriggerMinCount
                    )
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(0, 36)
                        .step(1))

                    .build()
        );

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenOpenContainer"),
            T.tl("autodrop.stopWhenOpenContainer.desc"),
            DEFAULT_CONFIG.stopWhenOpenContainer,
            config::isStopWhenOpenContainer,
            config::setStopWhenOpenContainer
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenOpenConfigGui"),
            T.tl("autodrop.stopWhenOpenConfigGui.desc"),
            DEFAULT_CONFIG.stopWhenOpenConfigGui,
            config::isStopWhenOpenConfigGui,
            config::setStopWhenOpenConfigGui
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenCrouch"),
            T.tl("autodrop.stopWhenCrouch.desc"),
            DEFAULT_CONFIG.stopWhenCrouch,
            config::isStopWhenCrouch,
            config::setStopWhenCrouch
        ));

        category.option(Factory.addToggleOption(
            T.tl("autodrop.stopWhenNotHoldingItem"),
            T.tl("autodrop.stopWhenNotHoldingItem.desc"),
            DEFAULT_CONFIG.stopWhenNotHoldingItem,
            config::isStopWhenNotHoldingItem,
            config::setStopWhenNotHoldingItem
        ));

        OptionGroup.Builder group = OptionGroup.createBuilder()
                    .name(T.tl("autodrop.triggerHoldItemGroup"));
        
        group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.stopWhenNotHoldingItemName"))
                    .description(OptionDescription.of(
                            T.tl("autodrop.stopWhenNotHoldingItemName.desc")))
                    .binding(
                        DEFAULT_CONFIG.stopWhenNotHoldingItemName,
                        config::getStopWhenNotHoldingItemName,
                        config::setStopWhenNotHoldingItemName
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
        );

        group.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.stopWhenNotHoldingItemId"))
                    .description(OptionDescription.of(
                            T.tl("autodrop.stopWhenNotHoldingItemId.desc")))
                    .binding(
                        DEFAULT_CONFIG.stopWhenNotHoldingItemId,
                        config::getStopWhenNotHoldingItemId,
                        config::setStopWhenNotHoldingItemId
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
                    config.addMatchItem();
                    config.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.addPresetMatchItem").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("autodrop.addPresetMatchItem.desc")))
                .action((yaclScreen, button) -> {
                    config.addPresetItems();
                    config.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        category.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.removeAll").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.removeAll.desc")))
                .action((yaclScreen, button) -> {
                    config.cleanMatchLists();
                    config.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
        );

        JsonArray matchListsJsonArray = config.getMatchListsJsonArray();
        AutoDropConfig.Item DEFAULT_MATCH_ITEM = AutoDropConfig.getDefaultMatchItem();
        for (int i=0; i<matchListsJsonArray.size(); i++) {
            final int index = i;
            MutableComponent matchName = config.getMatchItemDescription(index).isEmpty() ? 
                T.tl("autodrop.matchItem.description") : 
                T.l(config.getMatchItemDescription(index));
            OptionGroup.Builder whiteListGroup = OptionGroup.createBuilder()
                    .name(matchName)
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.description.desc")));
            

            whiteListGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autodrop.matchItem.remove").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("autodrop.matchItem.remove.desc")))
                .action((yaclScreen, button) -> {
                    config.removeMatchItem(index);
                    config.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, true, AutoDropConfigGui::createScreen);
                })
                .build()
            );

             whiteListGroup.option(Factory.addToggleOption(
                T.tl("autodrop.matchItem.enabled"),
                T.tl("autodrop.matchItem.enabled.desc"),
                DEFAULT_MATCH_ITEM.enabled,
                () -> config.isMatchItemEnabled(index),
                val -> config.setMatchItemEnabled(index, val)
            ));

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.matchItem.itemName"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.itemName.desc")))
                    .binding(
                        DEFAULT_MATCH_ITEM.name,
                        () -> config.getMatchItemName(index),
                        val -> config.setMatchItemName(index, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.matchItem.itemID"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.itemID.desc")))
                    .binding(
                        DEFAULT_MATCH_ITEM.id,
                        () -> config.getMatchItemId(index),
                        val -> config.setMatchItemId(index, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            whiteListGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autodrop.matchItem.itemTags"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.itemTags.desc")))
                    .binding(
                        StringUtil.join(DEFAULT_MATCH_ITEM.tags),
                        () -> StringUtil.join(config.getMatchItemTags(index)),
                        val -> config.setMatchItemTags(index, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

             whiteListGroup.option(Option.<Integer>createBuilder()
                    .name(T.tl("autodrop.matchItem.minEnchantRequir"))
                    .description(OptionDescription.of(T.tl("autodrop.matchItem.minEnchantRequir.desc")))
                    .binding(
                        DEFAULT_MATCH_ITEM.minEnchantRequir,
                        () -> config.getMatchItemMinEnchantRequir(index),
                        val -> config.setMatchItemMinEnchantRequir(index, val)
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
                        AutoDropConfig.Item.getEnchantmentsListStr(DEFAULT_MATCH_ITEM.enchantments),
                        () -> config.getEnchantList(index),
                        val -> config.setMatchItemEnchantments(index, val)
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        return category;
    }
}
