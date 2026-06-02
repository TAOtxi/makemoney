package cn.taotxi.Makemoney.gui.dialog;

import cn.taotxi.Makemoney.util.T;
import cn.taotxi.Makemoney.util.TaskUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmWindow extends Screen {
    private Component diaglogTitle = Component.empty();
    private Component confirmText = Component.empty();
    private Component cancelText = Component.empty();
    private Runnable onConfirm = () -> {};
    private Runnable onCancel = () -> {};
    private Runnable commonAction = () -> {};
    private Screen parent;

    public ConfirmWindow(Component title) {
        super(title);
        System.out.println(font);
        System.out.println(minecraft.font);
    }

	public ConfirmWindow(Component diaglogTitle, Component confirmText, Runnable onConfirm, Component cancelText, Runnable onCancel) {
		super(T.l("111"));
        this.diaglogTitle = diaglogTitle;
        this.confirmText = confirmText;
        this.onConfirm = onConfirm;
        this.cancelText = cancelText;
        this.onCancel = onCancel;
	}

	@Override
	protected void init() {
        int buttonWidth = 120;
        int buttonHeight = 20;

        Button confirmButton = Button.builder(confirmText, (btn) -> {
            commonAction.run();
            onConfirm.run();
            close();
        }).bounds(width / 2 - 20 - buttonWidth, height / 2 + 20, buttonWidth, buttonHeight).build();
        
        Button cancelButton = Button.builder(cancelText, (btn) -> {
            commonAction.run();
            onCancel.run();
            close();
        }).bounds(width / 2 + 20, height / 2 + 20, buttonWidth, buttonHeight).build();

        this.addRenderableWidget(confirmButton);
        this.addRenderableWidget(cancelButton);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
        int offsetX = this.font.width(diaglogTitle) / 2;

		graphics.drawString(this.font, diaglogTitle, width / 2 - offsetX, height / 2 - this.font.lineHeight, 0xFFFFFFFF, true);
	}

    public ConfirmWindow setDiaglogTitle(Component title) {
        this.diaglogTitle = title;
        return this;
    }

    public ConfirmWindow setConfirmText(Component confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    public ConfirmWindow setCancelText(Component cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    public ConfirmWindow setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
        return this;
    }

    public ConfirmWindow setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    public ConfirmWindow setCommonAction(Runnable commonAction) {
        this.commonAction = commonAction;
        return this;
    }

    public void close() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        close();
    }

    public void open() {
        parent = Minecraft.getInstance().screen;
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreen(this);
        });
    }

    public void open(int delay) {
        TaskUtil.createTimeTask("openConfirmWindow", () -> {
            open();
        }, delay);
    }
}