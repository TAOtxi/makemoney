package cn.taotxi.Makemoney.module.AutoRepair;

import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.client.gui.screens.Screen;

public class AutoRepairConfigGui {
    public static ConfigCategory.Builder createFishingCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("autorepair.name"))
                .tooltip(T.tl("autorepair.desc"));

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
                    val -> AutoRepair.config.checkoffHandInterval = Math.clamp(val, 1, 100000)
                )
                .controller(IntegerFieldControllerBuilder::create)
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
                    val -> AutoRepair.config.repairInterval = Math.clamp(val, 1, 100000)
                )
                .controller(IntegerFieldControllerBuilder::create)
                .build()
        );  

        category.group(enchantGroup.build());
        return category;
    }

}
