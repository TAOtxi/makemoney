package cn.taotxi.Makemoney.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.lwjgl.glfw.GLFW;
import com.google.common.base.Function;
import com.mojang.blaze3d.platform.Window;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoRepair.AutoRepairConfigGui;
import cn.taotxi.Makemoney.module.AutoRide.AutoRide;
import cn.taotxi.Makemoney.module.EntityHighlightBox.EntityHighlightBoxConfigGui;
import cn.taotxi.Makemoney.util.T;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;


public class ConfigScreen {
    // TODO: 待完善
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {

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

        OptionGroup.Builder autoRideGroup = OptionGroup.createBuilder()
                .name(T.tl("autoride"))
                .description(OptionDescription.of(T.tl("autoride.desc")));

        autoRideGroup.option(Factory.addToggleOption(
                T.tl("autoride.enabled"),
                T.tl("autoride.enabled.desc"),
                false,
                () -> AutoRide.enabled,
                value -> AutoRide.enabled = value
        ));

        autoRideGroup.option(Option.<Integer>createBuilder()
                .name(T.tl("autoride.interval"))
                .description(OptionDescription.of(T.tl("autoride.interval.desc")))
                .binding(
                    5,
                    () -> AutoRide.runInterval,
                    value -> AutoRide.runInterval = value
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
                    6.0,
                    () -> AutoRide.minDistance,
                    value -> AutoRide.minDistance = value
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
                    "Gzn12138",
                    () -> AutoRide.targetPlayer,
                    value -> AutoRide.targetPlayer = value
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

    public static boolean isOpenYaclScreen() {
        return Minecraft.getInstance().screen instanceof YACLScreen;
    }
}
