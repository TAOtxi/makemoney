package cn.taotxi.Makemoney.module.MenuClick;

import cn.taotxi.Makemoney.gui.ConfigScreen;
import cn.taotxi.Makemoney.gui.Factory;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;

public class MenuClickConfigGui {
    private static final MenuClickConfig CONFIG = MenuClickConfig.getInstance();

    public static ConfigCategory.Builder createMenuClickConfigCategory(Screen parent) {
        ConfigCategory.Builder moduleCategory = ConfigCategory.createBuilder()
                .name(T.tl("menuClick.name"));

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("menuClick.reload"))
                .description(OptionDescription.of(T.tl("menuClick.reload.desc")))
                .action((screen, option) -> {
                    CONFIG.reloadConfig();
                    ConfigScreen.reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("menuClick.removeAll"))
                .description(OptionDescription.of(T.tl("menuClick.removeAll.desc")))
                .action((screen, option) -> {
                    CONFIG.removeAllTasks();
                    CONFIG.saveConfig();
                    ConfigScreen.reload(screen, parent, true, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("menuClick.add"))
                .action((screen, option) -> {
                    CONFIG.addClickTask();
                    CONFIG.saveConfig();
                    ConfigScreen.reload(screen, parent, true, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        MenuClickTask defaultTask = CONFIG.getDefaultTask();
        for (int i = 0; i < CONFIG.tasks.size(); i++) {
            final int index = i;

            String description = CONFIG.getTaskDescription(index);
            OptionGroup.Builder group = OptionGroup.createBuilder()
                .name(description.isEmpty() ? 
                    T.tl("menuClick.group", index) : T.l(description));

            group.option(Option.<String>createBuilder()
                .name(T.tl("menuClick.task.name"))
                .description(
                    OptionDescription.of(T.tl("menuClick.task.name.desc", CONFIG.getTaskName(index))))
                .binding(
                    defaultTask.name + "_" + index,
                    () -> CONFIG.getTaskName(index),
                    (name) -> CONFIG.setTaskName(index, name)
                )
                .controller(StringControllerBuilder::create)
                .build()
            );

            group.option(Factory.addToggleOption(
                T.tl("menuClick.task.isLoop"),
                T.tl("menuClick.task.isLoop.desc"),
                defaultTask.isLoop,
                () -> CONFIG.getTaskIsLoop(index),
                (isLoop) -> CONFIG.setTaskIsLoop(index, isLoop)
            ));

            group.option(Option.<Integer>createBuilder()
                .name(T.tl("menuClick.task.startDelay"))
                .description(
                    OptionDescription.of(T.tl("menuClick.task.startDelay.desc")))
                .binding(
                    defaultTask.startDelay,
                    () -> CONFIG.getTaskStartDelay(index),
                    (startDelay) -> CONFIG.setTaskStartDelay(index, startDelay)
                )
                .controller((opt) -> IntegerFieldControllerBuilder.create(opt)
                    .min(1))
                .build()
            );

            group.option(Option.<Integer>createBuilder()
                .name(T.tl("menuClick.task.delay"))
                .description(
                    OptionDescription.of(T.tl("menuClick.task.delay.desc")))
                .binding(
                    defaultTask.delay,
                    () -> CONFIG.getTaskDelay(index),
                    (delay) -> CONFIG.setTaskDelay(index, delay)
                )
                .controller((opt) -> IntegerFieldControllerBuilder.create(opt)
                    .min(1))
                .build()
            );

            group.option(ButtonOption.createBuilder()
                .name(T.tl("menuClick.task.remove"))
                .description(
                    OptionDescription.of(T.tl("menuClick.task.remove.desc")))
                .action((screen, option) -> {
                    CONFIG.removeTask(index);
                    CONFIG.saveConfig();
                    ConfigScreen.reload(screen, parent, true, ConfigScreen::getConfigScreen);
                })
                .build()
            );

            moduleCategory.group(group.build());
            moduleCategory.group(ListOption.<String>createBuilder()
                    .name(T.tl("menuClick.task.list"))
                    .description(OptionDescription.of(T.tl("menuClick.task.list.desc")))
                    .binding(
                        CONFIG.getTaskActions(index),
                        () -> CONFIG.getTaskActions(index),
                        val -> CONFIG.setTaskActions(index, val)
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }

        return moduleCategory;
    }
}
