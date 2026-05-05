package cn.taotxi.Makemoney.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import com.google.common.base.Function;
import com.mojang.blaze3d.platform.Window;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepairConfigGui;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBoxConfigGui;
import cn.taotxi.Makemoney.module.StrangeFunction.AutoRide;
import cn.taotxi.Makemoney.module.StrangeFunction.IgnoreMessage;
import cn.taotxi.Makemoney.module.StrangeFunction.RightClickRide;
import cn.taotxi.Makemoney.module.StrangeFunction.StrangeConfig;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.utils.OptionUtils;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.YACLScreen.CategoryTab;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import dev.isxander.yacl3.impl.utils.YACLConstants;


public class ConfigScreen {
    // TODO: 待完善
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    StrangeConfig.getInstance().saveConfig();
                });

        
        ConfigCategory.Builder moduleCategory = createConfigCategory(parent);
        builder.category(moduleCategory.build());

        ConfigCategory.Builder strangeCategory = createStrangeCategory(parent);
        builder.category(strangeCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }

    private static ConfigCategory.Builder createConfigCategory(Screen parent) {
        ConfigCategory.Builder moduleCategory = ConfigCategory.createBuilder()
                .name(T.tl("gui.config.module"));

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("gui.config.open.autodrop"))
                .text(T.tl("gui.config.open"))
                .action((screen, option) -> {
                    YACLScreen autodropScreem = (YACLScreen) AutoDropConfigGui.createScreen(screen);
                    Window window = Minecraft.getInstance().getWindow(); 
                    autodropScreem.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    Minecraft.getInstance().setScreen(autodropScreem);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("gui.config.open.autorepair"))
                .text(T.tl("gui.config.open"))
                .action((screen, option) -> {
                    YACLScreen autorepairScreem = (YACLScreen) AutoRepairConfigGui.createConfigScreen(screen);
                    Window window = Minecraft.getInstance().getWindow(); 
                    autorepairScreem.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    Minecraft.getInstance().setScreen(autorepairScreem);
                })
                .build()
        );

        moduleCategory.option(ButtonOption.createBuilder()
                .name(T.tl("gui.config.open.entityhighlightbox"))
                .text(T.tl("gui.config.open"))
                .action((screen, option) -> {
                    YACLScreen entityhighlightboxScreen = (YACLScreen) EntityHighlightBoxConfigGui.createConfigScreen(screen);
                    Window window = Minecraft.getInstance().getWindow(); 
                    entityhighlightboxScreen.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
                    Minecraft.getInstance().setScreen(entityhighlightboxScreen);
                })
                .build()
        );

        return moduleCategory;
    }
    

    private static ConfigCategory.Builder createStrangeCategory(Screen parent) {
        ConfigCategory.Builder strangeCategory = ConfigCategory.createBuilder()
                .name(T.tl("strange.name"))
                .tooltip(T.tl("strange.desc"));

        strangeCategory.option(Factory.addToggleOption(
            T.tl("ignore.rightClickRide"), 
            T.tl("ignore.rightClickRide.desc"), 
            RightClickRide.isEnabled(true), 
            () -> RightClickRide.isEnabled(false),
            RightClickRide::setEnabled));

        OptionGroup.Builder autoRideGroup = OptionGroup.createBuilder()
                .name(T.tl("autoride"))
                .description(OptionDescription.of(T.tl("autoride.desc")));

        autoRideGroup.option(Factory.addToggleOption(
                T.tl("autoride.enabled"),
                T.tl("autoride.enabled.desc"),
                false,
                AutoRide::isEnabled,
                AutoRide::setEnabled
        ));

        autoRideGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autoride.interval"))
                .description(OptionDescription.of(T.tl("autoride.interval.desc")))
                .binding(
                    AutoRide.getRunInterval(true),
                    () -> AutoRide.getRunInterval(false),
                    AutoRide::setRunInterval
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(1, 20)
                    .step(1)
                    .formatValue(val -> T.l(val + " tick"))
                )
                .build()
        );

        autoRideGroup.option(Option.<Double>createBuilder()
                .name(T.tl("autoride.distance"))
                .description(OptionDescription.of(T.tl("autoride.distance.desc")))
                .binding(
                    AutoRide.getMinDistance(true),
                    () -> AutoRide.getMinDistance(false),
                    AutoRide::setMinDistance
                )
                .controller(opt -> DoubleSliderControllerBuilder.create(opt)
                    .range(1.0, 10.0)
                    .step(1.0)
                )
                .build()
        );

        autoRideGroup.option(Option.<String>createBuilder()
                .name(T.tl("autoride.target"))
                .description(OptionDescription.of(T.tl("autoride.target.desc")))
                .binding(
                    AutoRide.getTargetPlayer(true),
                    () -> AutoRide.getTargetPlayer(false),
                    AutoRide::setTargetPlayer
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        autoRideGroup.option(ButtonOption.createBuilder()
                .name(T.tl("autoride.reset"))
                .description(OptionDescription.of(T.tl("autoride.reset.desc")))
                .action((screen, option) -> {
                    AutoRide.resetConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        strangeCategory.group(autoRideGroup.build());


        OptionGroup.Builder ignoreGroup = OptionGroup.createBuilder()
                .name(T.tl("ignore"))
                .description(OptionDescription.of(T.tl("ignore.desc")));

        ignoreGroup.option(Factory.addToggleOption(
                T.tl("ignore.enabled"),
                T.tl("ignore.enabled.desc"),
                IgnoreMessage.isEnabled(true),
                () -> IgnoreMessage.isEnabled(false),
                IgnoreMessage::setEnabled
        ));

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.19lottery"))
                .action((screen, option) -> {
                    savePending(screen);
                    String pattern = "^[拾玖福彩] 使用 /lottery 购买彩票，每张100元！每20小时自动开奖，当前倒计时：";
                    IgnoreMessage.addIgnoreList(pattern);
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.guessWord"))
                .description(OptionDescription.of(T.tl("ignore.guessWord.desc")))
                .action((screen, option) -> {
                    savePending(screen);
                    String pattern = "^【猜单词游戏】$|^拾玖喵不太认识这个单词：|^提示：|^用 /word <你的猜测> 回答本题（每人仅一次）$|^当前词库：\\w+（共 \\d+ 条）$|^----------------------$";
                    IgnoreMessage.addIgnoreList(pattern);
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.earthquake"))
                .description(OptionDescription.of(T.tl("ignore.earthquake.desc")))
                .action((screen, option) -> {
                    savePending(screen);
                    String pattern = "^地震信息$|^ 20\\d{2}年\\d{2}月\\d{2}日 \\d{2}时\\d{2}分\\d{2}秒 发生$|^ 震中: |^ 震级: \\d+(?:\\.\\d+)?$|^ 深度: \\d+(?:\\.\\d+)?km$|^ 最大震度: \\d+$|^ 海啸信息: |^ 最大烈度: \\d+$|^ 更新时间: \\d{4}[-/]\\d{2}[-/]\\d{2} \\d{2}:\\d{2}:\\d{2}$|地震.*? \\| 第\\d{1,2}报|^中国地震台网 \\((?:正式|自动)测定\\)$";
                    IgnoreMessage.addIgnoreList(pattern);
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.19catInfo"))
                .description(OptionDescription.of(T.tl("ignore.19catInfo.desc")))
                .action((screen, option) -> {
                    savePending(screen);
                    String pattern = "^拾玖喵小道消息 ";
                    IgnoreMessage.addIgnoreList(pattern);
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.19clean"))
                .action((screen, option) -> {
                    savePending(screen);
                    String pattern = "拾玖型扫地机器人";
                    IgnoreMessage.addIgnoreList(pattern);
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );
        
        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.changeServer"))
                .description(OptionDescription.of(T.tl("ignore.changeServer.desc")))
                .action((screen, option) -> {
                    savePending(screen);
                    String pattern = "^\\w{1,16} 从 \\w+ 切换到 \\w+|^\\w{1,16} 离开了 \\w+$|^\\w{1,16}(?:退出|加入)了游戏$|^\\w{1,16} joined \\w+$|^\\w{1,16} was disconnected$";
                    IgnoreMessage.addIgnoreList(pattern);
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.buyInfo"))
                .action((screen, option) -> {
                    savePending(screen);
                    String pattern = "^<\\w{1,16}> \\d+$";
                    IgnoreMessage.addIgnoreList(pattern);
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );
        strangeCategory.group(ignoreGroup.build());
        strangeCategory.group(ListOption.<String>createBuilder()
                    .name(T.tl("ignore.regex"))
                    .description(OptionDescription.of(T.tl("ignore.regex.desc")))
                    .binding(
                        IgnoreMessage.getIgnoreList(false),
                        () -> IgnoreMessage.getIgnoreList(false),
                        IgnoreMessage::setIgnoreList
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        
        return strangeCategory;
    }


    public static void reload(YACLScreen screen, Screen parent, boolean saveConfig, Function<Screen, Screen> createConfigScreen) {
        Minecraft client = Minecraft.getInstance();
        double x = client.mouseHandler.xpos();
        double y = client.mouseHandler.ypos();
        try {
            int tab = screen.tabNavigationBar == null
                    ? 0
                    : screen.tabNavigationBar.getTabs().indexOf(screen.tabManager.getCurrentTab());
            if (tab == -1)
                tab = 0;
            if (saveConfig) {
                screen.finishOrSave();
            }
            screen.onClose(); // In case finishOrSave doesn't close it.
            YACLScreen newScreen = (YACLScreen) createConfigScreen.apply(parent);
            newScreen.init(screen.width, screen.height);
            try {
                newScreen.tabNavigationBar.selectTab(tab, false);
            } catch (IndexOutOfBoundsException e) {
                Makemoney.LOGGER.warn(
                        "YACL reload hack attempted to select tab {} but max index was {}",
                        tab,
                        newScreen.tabNavigationBar.getTabs().size() - 1
                );
            }
            client.setScreen(newScreen);
            GLFW.glfwSetCursorPos(client.getWindow().handle(), x, y);

        } catch (Exception e) {
            client.setScreen(parent);
            Makemoney.LOGGER.error("YACL reload hack failed with exception\n{}", e);
        }
    }

    public static void savePending(YACLScreen screen) {
        if (!screen.pendingChanges()) return;
        Set<OptionFlag> flags = new HashSet<>();
        OptionUtils.forEachOptions(screen.config, option -> {
            if (option.applyValue()) {
                flags.addAll(option.flags());
            }
        });
        OptionUtils.forEachOptions(screen.config, option -> {
            if (option.changed()) {
                // if still changed after applying, reset to the current value from binding
                // as something has gone wrong.
                option.forgetPendingValue();
                YACLConstants.LOGGER.error("Option '{}' value mismatch after applying! Reset to binding's getter.", option.name().getString());
            }
        });
        screen.config.saveFunction().run();

        flags.forEach(flag -> flag.accept(Minecraft.getInstance()));

        // screen.pendingChanges = false;
        if (screen.tabManager.getCurrentTab() instanceof CategoryTab categoryTab) {
            categoryTab.undoButton.active = false;
            categoryTab.saveFinishedButton.setMessage(GuiUtils.translatableFallback("yacl.gui.done", CommonComponents.GUI_DONE));
            categoryTab.saveFinishedButton.setTooltip(Tooltip.create(T.tl("yacl.gui.finished.tooltip")));
            categoryTab.cancelResetButton.setMessage(T.tl("controls.reset"));
            categoryTab.cancelResetButton.setTooltip(Tooltip.create(T.tl("yacl.gui.reset.tooltip")));
        }
    }

    public static boolean isOpenYaclScreen() {
        return Minecraft.getInstance().screen instanceof YACLScreen;
    }
}
