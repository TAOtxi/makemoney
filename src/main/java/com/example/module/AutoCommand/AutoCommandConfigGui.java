package com.example.module.AutoCommand;

import java.util.ArrayList;

import com.example.gui.ConfigScreen;
import com.example.gui.Factory;
import com.example.util.T;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;

public class AutoCommandConfigGui {
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
                    ConfigScreen.reload(yaclScreen, parent);
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
                    ConfigScreen.reload(yaclScreen, parent);
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

}
