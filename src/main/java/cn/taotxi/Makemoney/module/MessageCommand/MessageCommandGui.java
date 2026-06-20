package cn.taotxi.Makemoney.module.MessageCommand;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;

public class MessageCommandGui {
    private static final MessageCommandConfig MESSAGE_COMMAND_CONFIG = MessageCommandConfig.getInstance();

    public static ConfigCategory.Builder createMessageRuleCategory(Screen parent) {
        ConfigCategory.Builder moduleCategory = ConfigCategory.createBuilder()
                .name(T.tl("messageCommand.name"))
                .tooltip(T.tl("messageCommand.desc"));

        moduleCategory.option(Factory.addToggleOption(
            T.tl("messageCommand.enabled"),
            T.tl("messageCommand.enabled.desc"),
            MESSAGE_COMMAND_CONFIG.enabled.getDefaultValue(),
            MESSAGE_COMMAND_CONFIG.enabled::getValue,
            MESSAGE_COMMAND_CONFIG.enabled::setValue
        ));

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("messageCommand.reload"))
                .description(OptionDescription.of(T.tl("messageCommand.reload.desc")))
                .action((screen, option) -> {
                    ConfigScreen.savePending(screen);
                    MESSAGE_COMMAND_CONFIG.reloadConfig();
                    ConfigScreen.reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("messageCommand.clear"))
                .description(OptionDescription.of(T.tl("messageCommand.clear.desc")))
                .action((screen, option) -> {
                    ConfigScreen.savePending(screen);
                    MESSAGE_COMMAND_CONFIG.messageRules.clear();
                    MESSAGE_COMMAND_CONFIG.saveConfig();
                    ConfigScreen.reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("messageCommand.add"))
                .description(OptionDescription.of(T.tl("messageCommand.add.desc")))
                .action((screen, option) -> {
                    ConfigScreen.savePending(screen);
                    MESSAGE_COMMAND_CONFIG.addRule();
                    MESSAGE_COMMAND_CONFIG.saveConfig();
                    ConfigScreen.reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        MessageRule defaultRule = MESSAGE_COMMAND_CONFIG.getDefaultRule();
        for (int i = 0; i < MESSAGE_COMMAND_CONFIG.messageRules.size(); i++) {
            final int index = i;

            String description = MESSAGE_COMMAND_CONFIG.getRuleDescription(index);
            OptionGroup.Builder group = OptionGroup.createBuilder()
                .name(MESSAGE_COMMAND_CONFIG.getRuleDescription(index).isEmpty() ? 
                    T.tl("messageCommand.match.name") : T.l(description))
                .description(
                    OptionDescription.of(T.tl("messageCommand.match.desc")));

            group.option(Factory.addToggleOption(
                T.tl("messageCommand.rule.enabled"),
                T.tl("messageCommand.rule.enabled.desc"),
                defaultRule.enabled,
                () -> MESSAGE_COMMAND_CONFIG.getRuleEnabled(index),
                (enabled) -> MESSAGE_COMMAND_CONFIG.setRuleEnabled(index, enabled)
            ));

            group.option(Option.<String>createBuilder()
                .name(T.tl("messageCommand.rule.matcher"))
                .description(
                    OptionDescription.of(T.tl("messageCommand.rule.matcher.desc")))
                .binding(
                    defaultRule.matcher,
                    () -> MESSAGE_COMMAND_CONFIG.getRuleMatcher(index),
                    (matcher) -> MESSAGE_COMMAND_CONFIG.setRuleMatcher(index, matcher)
                )
                .controller(StringControllerBuilder::create)
                .build()
            );

            group.option(Option.<String>createBuilder()
                .name(T.tl("messageCommand.rule.command"))
                .description(
                    OptionDescription.of(T.tl("messageCommand.rule.command.desc")))
                .binding(
                    defaultRule.command,
                    () -> MESSAGE_COMMAND_CONFIG.getRuleCommand(index),
                    (command) -> MESSAGE_COMMAND_CONFIG.setRuleCommand(index, command)
                )
                .controller(StringControllerBuilder::create)
                .build()
            );

            group.option(ButtonOption.createBuilder()
                .name(
                    T.tl("messageCommand.rule.remove"))
                .description(
                    OptionDescription.of(T.tl("messageCommand.rule.remove.desc")))
                .action((screen, option) -> {
                    ConfigScreen.savePending(screen);
                    MESSAGE_COMMAND_CONFIG.removeRule(index);
                    MESSAGE_COMMAND_CONFIG.saveConfig();
                    ConfigScreen.reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
            );

            moduleCategory.group(group.build());
        }

        return moduleCategory;
    }
}
