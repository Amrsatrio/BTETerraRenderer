package com.mndk.bteterrarenderer.mcconnector.client.gui.screen;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.input.InputKey;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;

import javax.annotation.Nonnull;

public record NativeGuiScreenWrapperImpl(@Nonnull Screen delegate) implements NativeGuiScreenWrapper {

    @Override
    public void onDisplayed() {}

    /**
     * 1.21.x: Screen.init is init(int,int)
     */
    @Override
    public void initGui(int width, int height) {
        delegate.init(width, height);
    }

    /**
     * 1.21.x: Screen.resize is now resize(int,int)
     */
    @Override
    public void setScreenSize(int width, int height) {
        delegate.resize(width, height);
    }

    @Override
    public void tick() {
        delegate.tick();
    }

    @Override
    public void drawScreen(@Nonnull GuiDrawContextWrapper context, int mouseX, int mouseY, float partialTicks) {
        delegate.render(((GuiDrawContextWrapperImpl) context).delegate, mouseX, mouseY, partialTicks);
    }

    /**
     * 1.21.x: mouseClicked takes (Click, boolean)
     */
    @Override
    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        Click click = makeClick(mouseX, mouseY, mouseButton);
        return delegate.mouseClicked(click, false);
    }

    /**
     * 1.21.x: mouseReleased takes (Click)
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        Click click = makeClick(mouseX, mouseY, mouseButton);
        return delegate.mouseReleased(click);
    }

    /**
     * 1.21.x: mouseDragged takes (Click, double, double)
     * where the doubles are deltas
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double pMouseX, double pMouseY) {
        Click click = makeClick(mouseX, mouseY, mouseButton);
        double deltaX = mouseX - pMouseX;
        double deltaY = mouseY - pMouseY;

        return delegate.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        // Screen still has mouseScrolled(double,double,double,double) in 1.21.x
        return delegate.mouseScrolled(mouseX, mouseY, 0, scrollAmount);
    }

    /**
     * 1.21.x: charTyped takes (CharInput)
     */
    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        CharInput charInput = new CharInput(typedChar, keyCode);
        return delegate.charTyped(charInput);
    }

    /**
     * 1.21.x: keyPressed takes (KeyInput)
     */
    @Override
    public boolean keyPressed(InputKey key, int scanCode, int modifiers) {
        KeyInput keyInput = new KeyInput(key.glfwKeyCode, scanCode, modifiers);
        return delegate.keyPressed(keyInput);
    }

    @Override
    public void onRemoved() {
        delegate.removed();
    }

    @Override
    public boolean doesScreenPauseGame() {
        return delegate.shouldPause();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return delegate.shouldCloseOnEsc();
    }

    @Override
    public boolean alsoListensForKeyPress() {
        return true;
    }

    // ---------------- input factories ----------------

    private static Click makeClick(double x, double y, int button) {
        return new Click(x, y, new MouseInput(button, 0));
    }
}
