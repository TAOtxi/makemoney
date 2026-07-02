package cn.taotxi.Makemoney.module.AutoAFK;

import java.util.List;

import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;

public class AutoAFKGui {
    private static final AutoAFKConfig CONFIG = AutoAFKConfig.getInstance();

    public static ConfigCategory.Builder createAutoAFKCategory(Screen parent) {
        ConfigCategory.Builder autoAFKCategory = ConfigCategory.createBuilder()
                .name(T.tl("autoAFK.category"));

        OptionGroup.Builder AutoAttackGroup = OptionGroup.createBuilder()
                .name(T.tl("autoAFK.autoAttack"))
                .description(OptionDescription.of(T.tl("autoAFK.autoAttack.desc")));

        AutoAttackGroup.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.enabled"), 
            T.tl("autoAFK.autoAttack.enabled.desc"), 
            CONFIG.autoAttackEnabled.getDefaultValue(),
            CONFIG.autoAttackEnabled::getValue,
            CONFIG.autoAttackEnabled::setValue
        ));

        AutoAttackGroup.option(Option.<Integer>createBuilder()
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

        AutoAttackGroup.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.durabilityCheck"), 
            T.tl("autoAFK.autoAttack.durabilityCheck.desc"), 
            CONFIG.durabilityCheck.getDefaultValue(),
            CONFIG.durabilityCheck::getValue,
            CONFIG.durabilityCheck::setValue
        ));

        AutoAttackGroup.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.showInfo"), 
            T.tl("autoAFK.autoAttack.showInfo.desc"), 
            CONFIG.showInfo.getDefaultValue(),
            CONFIG.showInfo::getValue,
            CONFIG.showInfo::setValue
        ));

        AutoAttackGroup.option(Factory.addToggleOption(
            T.tl("autoAFK.autoAttack.mode"), 
            T.tl("autoAFK.autoAttack.mode.desc"), 
            CONFIG.attackMode.getDefaultValue(),
            CONFIG.attackMode::getValue,
            CONFIG.attackMode::setValue
        ));

        autoAFKCategory.group(AutoAttackGroup.build());
        autoAFKCategory.group(ListOption.<String>createBuilder()
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

        return autoAFKCategory;
    }
}
