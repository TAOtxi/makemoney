package cn.taotxi.Makemoney.gui;


import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import com.google.common.base.Function;
import com.mojang.blaze3d.platform.Window;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.AutoFish.AutoFishConfig;
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
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.utils.OptionUtils;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.YACLScreen.CategoryTab;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import dev.isxander.yacl3.impl.utils.YACLConstants;


public class ConfigScreen {
    private static final StrangeConfig STRANGE_CONFIG = StrangeConfig.getInstance();
    private static final AutoFishConfig AUTOFISH_CONFIG = AutoFishConfig.getInstance();

    // TODO: 待完善
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    StrangeConfig.getInstance().saveConfig();
                    AutoFishConfig.getInstance().saveConfig();
                });

        ConfigCategory.Builder strangeCategory = createStrangeCategory(parent);
        builder.category(strangeCategory.build());
        
        ConfigCategory.Builder moduleCategory = createConfigCategory(parent);
        builder.category(moduleCategory.build());


        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }

    private static ConfigCategory.Builder createConfigCategory(Screen parent) {
        ConfigCategory.Builder moduleCategory = ConfigCategory.createBuilder()
                .name(T.tl("gui.config.module"));

        OptionGroup.Builder autodropGroup = OptionGroup.createBuilder()
                .name(T.tl("autodrop.name"))
                .description(OptionDescription.of(T.tl("autodrop.desc")));

        autodropGroup.option(ButtonOption.createBuilder()
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
        moduleCategory.group(autodropGroup.build());

        OptionGroup.Builder autorepairGroup = OptionGroup.createBuilder()
                .name(T.tl("autorepair.name"))
                .description(OptionDescription.of(T.tl("autorepair.desc")));

        autorepairGroup.option(ButtonOption.createBuilder()
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
        moduleCategory.group(autorepairGroup.build());

        OptionGroup.Builder entityhighlightboxGroup = OptionGroup.createBuilder()
                .name(T.tl("entityhighlightbox.name"))
                .description(OptionDescription.of(T.tl("entityhighlightbox.desc")));

        entityhighlightboxGroup.option(ButtonOption.createBuilder()
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
        moduleCategory.group(entityhighlightboxGroup.build());

        return moduleCategory;
    }
    

    private static ConfigCategory.Builder createStrangeCategory(Screen parent) {
        ConfigCategory.Builder strangeCategory = ConfigCategory.createBuilder()
                .name(T.tl("strange.name"))
                .tooltip(T.tl("strange.desc"));

        OptionGroup.Builder autoFishGroup = OptionGroup.createBuilder()
                .name(T.tl("autofish.name"))
                .description(OptionDescription.of(T.tl("autofish.desc")));

        autoFishGroup.option(Factory.addToggleOption(
            T.tl("autofish.enabled"), 
            T.tl("autofish.enabled.desc"), 
            AUTOFISH_CONFIG.enabled.getDefaultValue(),
            AUTOFISH_CONFIG.enabled::getValue,
            AUTOFISH_CONFIG.enabled::setValue
        ));

        autoFishGroup.option(Factory.addToggleOption(
            T.tl("autofish.rotation"), 
            T.tl("autofish.rotation.desc"), 
            AUTOFISH_CONFIG.rotation.getDefaultValue(),
            AUTOFISH_CONFIG.rotation::getValue,
            AUTOFISH_CONFIG.rotation::setValue
        ));

        autoFishGroup.option(Factory.addToggleOption(
            T.tl("autofish.randomDelay"), 
            T.tl("autofish.randomDelay.desc"), 
            AUTOFISH_CONFIG.randomDelay.getDefaultValue(),
            AUTOFISH_CONFIG.randomDelay::getValue,
            AUTOFISH_CONFIG.randomDelay::setValue
        ));

        autoFishGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autofish.throwDelay"))
                .description(OptionDescription.of(T.tl("autofish.throwDelay.desc")))
                .binding(
                    AUTOFISH_CONFIG.throwDelay.getDefaultValue(),
                    AUTOFISH_CONFIG.throwDelay::getValue,
                    AUTOFISH_CONFIG.throwDelay::setValue
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(0, 100)
                    .step(1)
                    .formatValue(val -> T.l(val + " tick"))
                )
                .build()
        );

        strangeCategory.group(autoFishGroup.build());

        strangeCategory.option(Factory.addToggleOption(
            T.tl("ignore.rightClickRide"), 
            T.tl("ignore.rightClickRide.desc"), 
            STRANGE_CONFIG.rightClickRideEnabled.getDefaultValue(), 
            STRANGE_CONFIG.rightClickRideEnabled::getValue,
            STRANGE_CONFIG.rightClickRideEnabled::setValue
        ));

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
                    STRANGE_CONFIG.autoRideRunInterval.getDefaultValue(),
                    STRANGE_CONFIG.autoRideRunInterval::getValue,
                    STRANGE_CONFIG.autoRideRunInterval::setValue
                )
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                    .range(1, 20)
                    .step(1)
                    .formatValue(val -> T.l(val + " tick"))
                )
                .build()
        );

        autoRideGroup.option(Option.<Float>createBuilder()
                .name(T.tl("autoride.distance"))
                .description(OptionDescription.of(T.tl("autoride.distance.desc")))
                .binding(
                    STRANGE_CONFIG.autoRideMinDistance.getDefaultValue(),
                    STRANGE_CONFIG.autoRideMinDistance::getValue,
                    STRANGE_CONFIG.autoRideMinDistance::setValue
                )
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                    .range(1.0f, 10.0f)
                    .step(1.0f)
                )
                .build()
        );

        autoRideGroup.option(Option.<String>createBuilder()
                .name(T.tl("autoride.target"))
                .description(OptionDescription.of(T.tl("autoride.target.desc")))
                .binding(
                    STRANGE_CONFIG.autoRideTargetPlayer.getDefaultValue(),
                    STRANGE_CONFIG.autoRideTargetPlayer::getValue,
                    STRANGE_CONFIG.autoRideTargetPlayer::setValue
                )
                .controller(StringControllerBuilder::create)
                .build()
        );

        autoRideGroup.option(Factory.addToggleOption(
            T.tl("autoride.enableShakeOffPlayer"), 
            T.tl("autoride.enableShakeOffPlayer.desc"), 
            STRANGE_CONFIG.autoRideEnableShakeOffPlayer.getDefaultValue(),
            STRANGE_CONFIG.autoRideEnableShakeOffPlayer::getValue,
            STRANGE_CONFIG.autoRideEnableShakeOffPlayer::setValue
        ));

        strangeCategory.group(autoRideGroup.build());


        OptionGroup.Builder ignoreGroup = OptionGroup.createBuilder()
                .name(T.tl("ignore"))
                .description(OptionDescription.of(T.tl("ignore.desc")));

        ignoreGroup.option(Factory.addToggleOption(
                T.tl("ignore.enabled"),
                T.tl("ignore.enabled.desc"),
                STRANGE_CONFIG.ignoreEnabled.getDefaultValue(),
                STRANGE_CONFIG.ignoreEnabled::getValue,
                STRANGE_CONFIG.ignoreEnabled::setValue
        ));

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.preset"))
                .description(OptionDescription.of(T.tl("ignore.preset.desc")))
                .action((screen, option) -> {
                    savePending(screen);
                    IgnoreMessage.addPresetIgnoreList();
                    StrangeConfig.getInstance().saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );

        ignoreGroup.option(ButtonOption.createBuilder()
                .name(T.tl("ignore.deleteAll").withStyle(ChatFormatting.RED))
                .description(OptionDescription.of(T.tl("ignore.deleteAll.desc")))
                .action((screen, option) -> {
                    savePending(screen);
                    STRANGE_CONFIG.ignoreList.clear();
                    STRANGE_CONFIG.saveConfig();
                    reload(screen, parent, false, ConfigScreen::getConfigScreen);
                })
                .build()
        );


        strangeCategory.group(ignoreGroup.build());
        strangeCategory.group(ListOption.<String>createBuilder()
                    .name(T.tl("ignore.regex"))
                    .description(OptionDescription.of(T.tl("ignore.regex.desc")))
                    .binding(
                        List.of(),
                        STRANGE_CONFIG.ignoreList::getValueAsStringList,
                        STRANGE_CONFIG.ignoreList::setStringValue
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

    public static void openConfigScreen(Screen parent) {
        Minecraft.getInstance().setScreen(getConfigScreen(parent));
    }
}
