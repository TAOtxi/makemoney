package cn.taotxi.Makemoney.module.AutoAFK;

import java.util.List;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.module.AutoAFK.AutoAFKConfig.PositionCheckItem;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AutoAFKGui {
    private static final AutoAFKConfig CONFIG = AutoAFKConfig.getInstance();
    private static Screen parent;

    public static ConfigCategory.Builder createAutoAFKCategory(Screen parent) {
        AutoAFKGui.parent = parent;
        ConfigCategory.Builder autoAFKCategory = ConfigCategory.createBuilder()
                .name(T.tl("autoAFK.category"));

        createAutoAttackGroup(autoAFKCategory);
        createTpsCheckGroup(autoAFKCategory);
        createPositionCheckGroup(autoAFKCategory);

        return autoAFKCategory;
    }


    public static ConfigCategory.Builder createAutoAttackGroup(ConfigCategory.Builder category) {
        OptionGroup.Builder group = OptionGroup.createBuilder()
                .name(T.tl("autoAFK.autoAttack"))
                .description(OptionDescription.of(T.tl("autoAFK.autoAttack.desc")));

        group.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.enabled"), 
            T.tl("autoAFK.autoAttack.enabled.desc"), 
            CONFIG.autoAttackEnabled.getDefaultValue(),
            CONFIG.autoAttackEnabled::getValue,
            CONFIG.autoAttackEnabled::setValue
        ));

        group.option(Option.<Integer>createBuilder()
                .name(T.tl("autoAFK.autoAttack.interval"))
                .description(OptionDescription.of(T.tl("autoAFK.autoAttack.interval.desc")))
                .binding(
                    CONFIG.attackInterval.getDefaultValue(),
                    CONFIG.attackInterval::getValue,
                    CONFIG.attackInterval::setValue
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(1, 25)
                    .step(1)
                    .formatValue(val -> T.l(val + " tick"))
                )
                .build()
        );

        group.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.durabilityCheck"), 
            T.tl("autoAFK.autoAttack.durabilityCheck.desc"), 
            CONFIG.durabilityCheck.getDefaultValue(),
            CONFIG.durabilityCheck::getValue,
            CONFIG.durabilityCheck::setValue
        ));

        group.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.showInfo"), 
            T.tl("autoAFK.autoAttack.showInfo.desc"), 
            CONFIG.showInfo.getDefaultValue(),
            CONFIG.showInfo::getValue,
            CONFIG.showInfo::setValue
        ));

        group.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.mode"), 
            T.tl("autoAFK.autoAttack.mode.desc"), 
            CONFIG.attackMode.getDefaultValue(),
            CONFIG.attackMode::getValue,
            CONFIG.attackMode::setValue
        ));

        category.group(group.build());
        category.group(ListOption.<String>createBuilder()
                    .name(T.tl("autoAFK.autoAttack.list"))
                    .description(OptionDescription.of(T.tl("autoAFK.autoAttack.list.desc")))
                    .binding(
                        List.of("player"),
                        CONFIG.attackList::getValueAsList,
                        CONFIG.attackList::setValue
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );

        return category;
    }

    public static ConfigCategory.Builder createTpsCheckGroup(ConfigCategory.Builder category) {
        OptionGroup.Builder group = OptionGroup.createBuilder()
                .name(T.tl("autoAFK.tpsCheck"))
                .description(OptionDescription.of(T.tl("autoAFK.tpsCheck.desc")));

        group.option(Factory.addToggleOption(
            T.tl("autoAFK.tpsCheck.enabled"), 
            T.tl("autoAFK.tpsCheck.enabled.desc"), 
            CONFIG.tpsCheckEnabled.getDefaultValue(),
            CONFIG.tpsCheckEnabled::getValue,
            CONFIG.tpsCheckEnabled::setValue
        ));

        group.option(Option.<Integer>createBuilder()
                .name(T.tl("autoAFK.tpsCheck.threshold"))
                .description(OptionDescription.of(T.tl("autoAFK.tpsCheck.threshold.desc")))
                .binding(
                    CONFIG.safetyTpsThreshold.getDefaultValue(),
                    CONFIG.safetyTpsThreshold::getValue,
                    CONFIG.safetyTpsThreshold::setValue
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(5, 15)
                    .step(1)
                    .formatValue(val -> T.l(val + " tick"))
                )
                .build()
        );

        group.option(Option.<Integer>createBuilder()
                .name(T.tl("autoAFK.tpsCheck.greenThreshold"))
                .description(OptionDescription.of(T.tl("autoAFK.tpsCheck.greenThreshold.desc")))
                .binding(
                    CONFIG.greenTpsThreshold.getDefaultValue(),
                    CONFIG.greenTpsThreshold::getValue,
                    CONFIG.greenTpsThreshold::setValue
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(10, 20)
                    .step(1)
                    .formatValue(val -> T.l(val + " tick"))
                )
                .build()
        );

        group.option(Option.<String>createBuilder()
                .name(T.tl("autoAFK.tpsCheck.command"))
                .description(OptionDescription.of(T.tl("autoAFK.tpsCheck.command.desc")))
                .binding(
                    CONFIG.triggerCommand.getDefaultValue(),
                    CONFIG.triggerCommand::getValue,
                    CONFIG.triggerCommand::setValue
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        group.option(Option.<String>createBuilder()
                .name(T.tl("autoAFK.tpsCheck.greenCommand"))
                .description(OptionDescription.of(T.tl("autoAFK.tpsCheck.greenCommand.desc")))
                .binding(
                    CONFIG.greenTriggerCommand.getDefaultValue(),
                    CONFIG.greenTriggerCommand::getValue,
                    CONFIG.greenTriggerCommand::setValue
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        category.group(group.build());
        return category;
    }

    public static ConfigCategory.Builder createPositionCheckGroup(ConfigCategory.Builder category) {
        OptionGroup.Builder group = OptionGroup.createBuilder()
                .name(T.tl("autoAFK.positionCheck"))
                .description(OptionDescription.of(T.tl("autoAFK.positionCheck.desc")));

        group.option(Factory.addToggleOption(
            T.tl("autoAFK.positionCheck.enabled"), 
            T.tl("autoAFK.positionCheck.enabled.desc"), 
            CONFIG.positionCheckEnabled.getDefaultValue(),
            CONFIG.positionCheckEnabled::getValue,
            CONFIG.positionCheckEnabled::setValue
        ));

        group.option(Option.<Integer>createBuilder()
                    .name(T.tl("autoAFK.positionCheck.interval"))
                    .description(OptionDescription.of(T.tl("autoAFK.positionCheck.interval.desc")))
                    .binding(
                        CONFIG.positionCheckInterval.getDefaultValue(),
                        CONFIG.positionCheckInterval::getValue,
                        CONFIG.positionCheckInterval::setValue
                    )
                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(20, 200)
                        .step(1)
                        .formatValue(val -> T.l(val + " tick"))
                    )

                    .build()
        );

        PositionCheckItem defaultItem = CONFIG.getDefaultPositionCheckItem();
        group.option(ButtonOption.createBuilder()
                .name(T.tl("autoAFK.positionCheck.addItem"))
                .description(OptionDescription.of(T.tl("autoAFK.positionCheck.addItem.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.addPositionCheckItem(defaultItem);
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        category.group(group.build());

        for (int index = 0; index < CONFIG.positionCheckItems.size(); index++) {
            final int i = index;

            OptionGroup.Builder itemGroup = OptionGroup.createBuilder()
                .name(T.tl("autoAFK.positionCheck.item"))
                .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.desc")));

            itemGroup.option(Factory.addToggleOption(
                T.tl("autoAFK.positionCheck.item.enabled"), 
                T.tl("autoAFK.positionCheck.item.enabled.desc"), 
                defaultItem.isEnabled(),
                () -> CONFIG.getPositionCheckItemEnabled(i),
                (val) -> CONFIG.setPositionCheckItemEnabled(i, val)
            ));

            itemGroup.option(Option.<String>createBuilder()
                .name(T.tl("autoAFK.positionCheck.item.world"))
                .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.world.desc")))
                .binding(
                    defaultItem.getWorld(),
                    () -> CONFIG.getPositionCheckItemWorld(i),
                    (val) -> CONFIG.setPositionCheckItemWorld(i, val)
                )
                .controller(StringControllerBuilder::create)
                .build()
            );

            itemGroup.option(Factory.addToggleOption(
                T.tl("autoAFK.positionCheck.item.isInner"), 
                T.tl("autoAFK.positionCheck.item.isInner.desc"), 
                defaultItem.isInner(),
                () -> CONFIG.getPositionCheckItemIsInner(i),
                (val) -> CONFIG.setPositionCheckItemIsInner(i, val)
            ));

            boolean isBallType = CONFIG.getPositionCheckItemType(i).equals("ball");
            Component text = isBallType ? 
                T.tl("autoAFK.positionCheck.item.ballType") : T.tl("autoAFK.positionCheck.item.cuboidType");

            itemGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autoAFK.positionCheck.item.type"))
                .text(text)
                .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.type.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.setPositionCheckItemType(i, isBallType ? "cuboid" : "ball");
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
            );

            if (isBallType) {
                itemGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autoAFK.positionCheck.item.ballCenter"))
                    .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.ballCenter.desc")))
                    .binding(
                        "<0, 0, 0>",
                        () -> CONFIG.getPositionCheckItemPosition(i, 1),
                        (val) -> CONFIG.setPositionCheckItemPosition(i, 1, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                );

                itemGroup.option(Option.<Float>createBuilder()
                        .name(T.tl("autoAFK.positionCheck.item.radius"))
                        .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.radius.desc")))
                        .binding(
                            defaultItem.getRadius(),
                            () -> CONFIG.getPositionCheckItemRadius(i),
                            (val) -> CONFIG.setPositionCheckItemRadius(i, val)
                        )
                        .controller(opt -> FloatFieldControllerBuilder.create(opt)
                            .min(1.0f))
                        .build()
                );
            } else {
                itemGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autoAFK.positionCheck.item.cuboidStart"))
                    .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.cuboidStart.desc")))
                    .binding(
                        "<0, 0, 0>",
                        () -> CONFIG.getPositionCheckItemPosition(i, 1),
                        (val) -> CONFIG.setPositionCheckItemPosition(i, 1, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                );

                itemGroup.option(Option.<String>createBuilder()
                    .name(T.tl("autoAFK.positionCheck.item.cuboidEnd"))
                    .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.cuboidEnd.desc")))
                    .binding(
                        "<0, 0, 0>",
                        () -> CONFIG.getPositionCheckItemPosition(i, 2),
                        (val) -> CONFIG.setPositionCheckItemPosition(i, 2, val)
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                );
            }

            itemGroup.option(Option.<String>createBuilder()
                .name(T.tl("autoAFK.positionCheck.item.triggerCmd"))
                .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.triggerCmd.desc")))
                .binding(
                    defaultItem.getCommand(),
                    () -> CONFIG.getPositionCheckItemTriggerCmd(i),
                    (val) -> CONFIG.setPositionCheckItemTriggerCmd(i, val)
                )
                .controller(StringControllerBuilder::create)
                .build()
            );

            itemGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autoAFK.positionCheck.item.delete"))
                .description(OptionDescription.of(T.tl("autoAFK.positionCheck.item.delete.desc")))
                .action((yaclScreen, button) -> {
                    CONFIG.removePositionCheckItem(i);
                    CONFIG.saveConfig();
                    ConfigScreen.reload(yaclScreen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
            );
            category.group(itemGroup.build());
        }
        return category;
    }
}
