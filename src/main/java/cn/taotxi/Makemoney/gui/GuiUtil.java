package cn.taotxi.Makemoney.gui;

import java.util.Map;
import java.util.Set;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.config.MakemoneyConfig;
import cn.taotxi.Makemoney.module.AutoAFK.AutoAFK;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDrop;
import cn.taotxi.Makemoney.module.AutoDrop.AutoDropConfigGui;
import cn.taotxi.Makemoney.module.AutoFish.AutoFish;
import cn.taotxi.Makemoney.module.MendingHelper.MendingHelper;
import cn.taotxi.Makemoney.module.MenuClick.MenuClick;
import cn.taotxi.Makemoney.module.MessageCommand.MessageCommand;
import cn.taotxi.Makemoney.module.NineteenWorld.AutoRide;
import cn.taotxi.Makemoney.module.NineteenWorld.IgnoreMessage;
import cn.taotxi.Makemoney.util.Message;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public class GuiUtil {
    private static final Minecraft client = Minecraft.getInstance();
    private static final Map<String, Integer> configTabIndexMap;

    static {
        configTabIndexMap = Map.of(
            AutoRide.MODULE_NAME, 0,
            AutoFish.MODULE_NAME, 1,
            MendingHelper.MODULE_NAME, 1,
            AutoAFK.MODULE_NAME, 2,
            IgnoreMessage.MODULE_NAME, 3,
            MessageCommand.MODULE_NAME, 4,
            MenuClick.MODULE_NAME, 5
        );
    }

    public static void openConfigChangeTipWindow(Set<String> configChangeNameSet) {
        TaskUtil.createOnceTimeTask("removeConfigChangeTipWindow", () -> {
            TaskUtil.removeTimeTask("configChangeTipWindow");
        }, 20 * 60);

        TaskUtil.createTimeTask("configChangeTipWindow", () -> {
            if (!(client.gui.screen() instanceof TitleScreen)) {
                return;
            }

            Screen originScreen = client.gui.screen();
            ConfirmScreen confirmScreen = new ConfirmScreen(
                (isConfirm) -> {
                    if (isConfirm) {
                        MakemoneyConfig.getInstance().resetConfig(configChangeNameSet);
                    }
                    MakemoneyConfig.getInstance().updateConfigVersionField();
                    client.gui.setScreen(originScreen);
                },
                T.tl("gui.dialog.configChange.title"),
                T.tl("gui.dialog.configChange.message"),
                T.tl("gui.dialog.configChange.confirm"),
                T.tl("gui.dialog.configChange.cancel")
            );
            client.gui.setScreen(confirmScreen);
            TaskUtil.removeTimeTask("configChangeTipWindow");
            TaskUtil.removeTimeTask("removeConfigChangeTipWindow");
        }, 5);
    }

    public static void openYaclScreen(String key, String tabName) {
        int tabIndex = configTabIndexMap.getOrDefault(tabName, 0);
        openYaclScreen(key, tabIndex);
    }

    public static void openYaclScreen(String key, int tabIndex) {
        TaskUtil.removeTimeTask("openYaclScreen");
        TaskUtil.createTimeTask("openYaclScreen", () -> {
            if (client.gui.screen() != null) return;
            TaskUtil.removeTimeTask("openYaclScreen");
            
            YACLScreen configScreen;
            if (key.equals(AutoDrop.MODULE_NAME)) {
                configScreen = (YACLScreen) AutoDropConfigGui.createScreen(null);
            } else if (key.equals(Makemoney.MOD_ID)) {
                configScreen = (YACLScreen) ConfigScreen.getConfigScreen(null);
            } else {
                Message.clientSideMsg("Can not find screen: " + key);
                return;
            }

            configScreen.init(
                client.getWindow().getGuiScaledWidth(), 
                client.getWindow().getGuiScaledHeight()
            );

            if (tabIndex < 0 || tabIndex >= configScreen.tabNavigationBar.getTabs().size()) {
                Message.clientSideMsg("Invalid tab: " + tabIndex);
                return;
            }

            configScreen.tabNavigationBar.selectTab(tabIndex, true);
            client.gui.setScreen(configScreen);
        }, 1);
    }

    public static void openYaclScreen(String key) {
        openYaclScreen(key, 0);
    }
}
