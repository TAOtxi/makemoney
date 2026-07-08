package cn.taotxi.Makemoney.module.Highlight;

import java.util.List;
import java.awt.Color;

import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;

public class HighlightGui {
    private static final HighlightConfig CONFIG = HighlightConfig.getInstance();

    public static ConfigCategory.Builder createHighlightCategory(Screen parent) {
        ConfigCategory.Builder highlightCategory = ConfigCategory.createBuilder()
                .name(T.tl("highlight.name"))
                .tooltip(T.tl("highlight.desc"));

        highlightCategory.option(Factory.addToggleOption(
            T.tl("highlight.enabled"), 
            T.tl("highlight.enabled.desc"), 
            CONFIG.enabled.getDefaultValue(),
            CONFIG.enabled::getValue,
            CONFIG.enabled::setValue
        ));

        highlightCategory.option(Factory.addToggleOption(
            T.tl("highlight.colorful"), 
            T.tl("highlight.colorful.desc"), 
            CONFIG.colorful.getDefaultValue(),
            CONFIG.colorful::getValue,
            CONFIG.colorful::setValue
        ));

        highlightCategory.option(Factory.addToggleOption(
            T.tl("highlight.renderInList"), 
            T.tl("highlight.renderInList.desc"), 
            CONFIG.renderInList.getDefaultValue(),
            CONFIG.renderInList::getValue,
            CONFIG.renderInList::setValue
        ));

        highlightCategory.option(Option.<Integer>createBuilder()
                .name(T.tl("highlight.renderRadius"))
                .description(OptionDescription.of(T.tl("highlight.renderRadius.desc")))
                .binding(
                    CONFIG.renderRadius.getDefaultValue(),
                    CONFIG.renderRadius::getValue,
                    CONFIG.renderRadius::setValue
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .step(1)
                    .range(-1, 200))
                .build()
        );

        OptionGroup.Builder colorGroup = OptionGroup.createBuilder()
                .name(T.tl("highlight.color.name"))
                .description(OptionDescription.of(T.tl("highlight.color.desc")));

        colorGroup.option(Option.<Color>createBuilder()
                .name(T.tl("highlight.color.default"))
                .binding(
                    HighlightConfig.RGBA_StrToColor(CONFIG.defaultColor.getDefaultValue()),
                    () -> HighlightConfig.RGBA_StrToColor(CONFIG.defaultColor.getValue()),
                    color -> HighlightConfig.saveColor(CONFIG.defaultColor, color)
                )
                .controller(opt ->ColorControllerBuilder.create(opt)
                    .allowAlpha(true))
                .build()
        );

        colorGroup.option(Option.<Color>createBuilder()
                .name(T.tl("highlight.color.enemy"))
                .binding(
                    HighlightConfig.RGBA_StrToColor(CONFIG.enemyColor.getDefaultValue()),
                    () -> HighlightConfig.RGBA_StrToColor(CONFIG.enemyColor.getValue()),
                    color -> HighlightConfig.saveColor(CONFIG.enemyColor, color)
                )
                .controller(opt ->ColorControllerBuilder.create(opt)
                    .allowAlpha(true))
                .build()
        );

        colorGroup.option(Option.<Color>createBuilder()
                .name(T.tl("highlight.color.player"))
                .description(OptionDescription.of(T.tl("highlight.color.player.desc")))
                .binding(
                    HighlightConfig.RGBA_StrToColor(CONFIG.playerColor.getDefaultValue()),
                    () -> HighlightConfig.RGBA_StrToColor(CONFIG.playerColor.getValue()),
                    color -> HighlightConfig.saveColor(CONFIG.playerColor, color)
                )
                .controller(opt ->ColorControllerBuilder.create(opt)
                    .allowAlpha(true))
                .build()
        );

        colorGroup.option(Option.<Color>createBuilder()
                .name(T.tl("highlight.color.animal"))
                .binding(
                    HighlightConfig.RGBA_StrToColor(CONFIG.animalColor.getDefaultValue()),
                    () -> HighlightConfig.RGBA_StrToColor(CONFIG.animalColor.getValue()),
                    color -> HighlightConfig.saveColor(CONFIG.animalColor, color)
                )
                .controller(opt ->ColorControllerBuilder.create(opt)
                    .allowAlpha(true))
                .build()
        );

        colorGroup.option(Option.<Color>createBuilder()
                .name(T.tl("highlight.color.item"))
                .binding(
                    HighlightConfig.RGBA_StrToColor(CONFIG.itemColor.getDefaultValue()),
                    () -> HighlightConfig.RGBA_StrToColor(CONFIG.itemColor.getValue()),
                    color -> HighlightConfig.saveColor(CONFIG.itemColor, color)
                )
                .controller(opt ->ColorControllerBuilder.create(opt)
                    .allowAlpha(true))
                .build()
        );

        colorGroup.option(Option.<Color>createBuilder()
                .name(T.tl("highlight.color.decoration"))
                .description(OptionDescription.of(T.tl("highlight.color.decoration.desc")))
                .binding(
                    HighlightConfig.RGBA_StrToColor(CONFIG.decorationColor.getDefaultValue()),
                    () -> HighlightConfig.RGBA_StrToColor(CONFIG.decorationColor.getValue()),
                    color -> HighlightConfig.saveColor(CONFIG.decorationColor, color)
                )
                .controller(opt ->ColorControllerBuilder.create(opt)
                    .allowAlpha(true))
                .build()
        );

        highlightCategory.group(colorGroup.build());

        highlightCategory.group(ListOption.<String>createBuilder()
                    .name(T.tl("highlight.renderEntities"))
                    .description(OptionDescription.of(T.tl("highlight.renderEntities.desc")))
                    .binding(
                        List.of(),
                        CONFIG.renderEntities::getValueAsList,
                        CONFIG.renderEntities::setValue
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );

        return highlightCategory;
    }
}
