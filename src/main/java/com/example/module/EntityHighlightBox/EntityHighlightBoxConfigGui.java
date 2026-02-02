package com.example.module.EntityHighlightBox;

import java.awt.Color;

import com.example.gui.ConfigScreen;
import com.example.gui.Factory;
import com.example.util.StringUtil;
import com.example.util.T;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;

public class EntityHighlightBoxConfigGui {
    public static ConfigCategory.Builder createEntityHighlightBoxCategoryBuilder(Screen parent) {
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
                    ConfigScreen.reload(yaclScreen, parent);
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

}
