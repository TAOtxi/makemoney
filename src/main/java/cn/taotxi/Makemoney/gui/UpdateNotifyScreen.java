package cn.taotxi.Makemoney.gui;

import cn.taotxi.Makemoney.module.UpdateCheck.UpdateCheck;
import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;

/**
 * 发现新版本时的提示界面。
 *
 * <p>复用 {@link ConfirmScreen} 的标题 / 正文 / 居中布局，只把按钮区域替换成竖排的三个按钮。
 * 按 ESC 或点击界面外关闭等价于“稍后再说”：直接回到主菜单，不写入任何记录，下次启动还会提示。
 */
public class UpdateNotifyScreen extends ConfirmScreen {
    private static final int BUTTON_WIDTH = 220;

    private final Screen parent;
    private final String latestVersion;
    private final String modrinthUrl;
    private final String githubUrl;

    public UpdateNotifyScreen(
        Screen parent,
        String currentVersion,
        String latestVersion,
        String modrinthUrl,
        String githubUrl
    ) {
        super(
            // ESC 时触发，等同于“稍后再说”
            confirmed -> Minecraft.getInstance().gui.setScreen(parent),
            T.tl(
                "gui.dialog.update.title",
                T.l("v" + VersionUtil.normalize(currentVersion)).withStyle(ChatFormatting.GRAY),
                T.l("v" + VersionUtil.normalize(latestVersion)).withStyle(ChatFormatting.GREEN)
            ),
            T.tl("gui.dialog.update.message")
        );
        this.parent = parent;
        this.latestVersion = latestVersion;
        this.modrinthUrl = modrinthUrl;
        this.githubUrl = githubUrl;
    }

    @Override
    protected void addButtons(LinearLayout layout) {
        LinearLayout column = layout.addChild(LinearLayout.vertical().spacing(Button.DEFAULT_SPACING));
        column.defaultCellSetting().alignHorizontallyCenter();

        column.addChild(Button.builder(
                T.tl("gui.dialog.update.modrinth"),
                button -> {
                    openLink(modrinthUrl);
                    ignoreAndBack();
                }
        ).width(BUTTON_WIDTH).build());

        column.addChild(Button.builder(
                T.tl("gui.dialog.update.github"),
                button -> {
                    openLink(githubUrl);
                    ignoreAndBack();
                }
        ).width(BUTTON_WIDTH).build());

        column.addChild(Button.builder(
                T.tl("gui.dialog.update.ignore"),
                button -> ignoreAndBack()
        ).width(BUTTON_WIDTH).build());
    }

    @Override
    public void onClose() {
        back();
    }

    private void openLink(String url) {
        Util.getPlatform().openUri(url);
        back();
    }

    private void ignoreAndBack() {
        UpdateCheck.ignoreVersion(latestVersion);
        back();
    }

    private void back() {
        this.minecraft.gui.setScreen(parent);
    }
}
