package com.mndk.bteterrarenderer.mcconnector.client.gui;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.WindowDimension;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.shape.GraphicsQuad;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.vertex.PosXY;
import com.mndk.bteterrarenderer.mcconnector.client.gui.widget.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.FontWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapperImpl;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class GuiDrawContextWrapperImpl extends AbstractGuiDrawContextWrapper {

    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED = Identifier.of("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED = Identifier.of("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED = Identifier.of("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX = Identifier.of("widget/checkbox");

    private static final ButtonTextures BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of("widget/button"),
            Identifier.of("widget/button_disabled"),
            Identifier.of("widget/button_highlighted")
    );

    private int scissorDepth;

    @Nonnull public final DrawContext delegate;

    public void translate(float x, float y, float z) {
        // GUI matrices are 2D in this version
        delegate.getMatrices().translate(x, y);
    }

    public void pushMatrix() {
        delegate.getMatrices().pushMatrix();
    }

    public void popMatrix() {
        delegate.getMatrices().popMatrix();
    }

    @Override
    protected boolean usesNativeScissorStack() {
        return true;
    }

    protected int[] getAbsoluteScissorDimension(int relX, int relY, int relWidth, int relHeight) {
        WindowDimension window = McConnector.client().getWindowSize();
        if (window.getScaledWidth() == 0 || window.getScaledHeight() == 0) {
            return new int[] { 0, 0, 0, 0 };
        }

        // DrawContext.enableScissor applies the current matrix transform.
        return new int[] { relX, relY, relWidth, relHeight };
    }

    protected void glEnableScissor(int x, int y, int width, int height) {
        // DrawContext scissor uses x1,y1,x2,y2
        delegate.enableScissor(x, y, x + width, y + height);
        scissorDepth++;
    }

    protected void glDisableScissor() {
        if (scissorDepth > 0) {
            delegate.disableScissor();
            scissorDepth--;
        }
    }

    public void fillQuad(GraphicsQuad<PosXY> quad, int color, float z) {
        // Avoid raw VertexConsumer access (changed in 1.21.x).
        // Approximate as an axis-aligned rectangle using bounds.
        float[] bounds = {
                Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY
        };
        quad.forEach(v -> {
            bounds[0] = Math.min(bounds[0], v.x);
            bounds[1] = Math.min(bounds[1], v.y);
            bounds[2] = Math.max(bounds[2], v.x);
            bounds[3] = Math.max(bounds[3], v.y);
        });

        delegate.fill((int) bounds[0], (int) bounds[1], (int) bounds[2], (int) bounds[3], color);
    }

    public void drawButton(int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
        boolean enabled = hoverState != AbstractWidgetCopy.HoverState.DISABLED;
        boolean focused = hoverState == AbstractWidgetCopy.HoverState.MOUSE_OVER;
        Identifier buttonTexture = BUTTON_TEXTURES.get(enabled, focused);

        delegate.drawGuiTexture(RenderPipelines.GUI_TEXTURED, buttonTexture, x, y, width, height);
    }

    public void drawCheckBox(int x, int y, int width, int height, boolean focused, boolean checked) {
        Identifier identifier = checked
                ? (focused ? CHECKBOX_SELECTED_HIGHLIGHTED : CHECKBOX_SELECTED)
                : (focused ? CHECKBOX_HIGHLIGHTED : CHECKBOX);

        delegate.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, x, y, width, height, ColorHelper.getWhite(1));
    }

    public void drawTextHighlight(int startX, int startY, int endX, int endY) {
        delegate.fill(startX, startY, endX, endY, 0xff0000ff);
    }

    public void drawImage(ResourceLocationWrapper res, int x, int y, int w, int h,
                          float u1, float u2, float v1, float v2) {
        Identifier texture = ((ResourceLocationWrapperImpl) res).delegate();

        delegate.drawTexturedQuad(texture, x, x + w, y, y + h, u1, u2, v1, v2);
    }

    public void drawWholeNativeImage(@Nonnull NativeTextureWrapper allocatedTextureObject, int x, int y, int w, int h) {
        Identifier texture = ((NativeTextureWrapperImpl) allocatedTextureObject).delegate;

        delegate.drawTexturedQuad(texture, x, x + w, y, y + h, 0, 1, 0, 1);
    }

    public void drawHoverEvent(StyleWrapper styleWrapper, int x, int y) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Style style = ((StyleWrapperImpl) styleWrapper).delegate();
        delegate.drawHoverEvent(textRenderer, style, x, y);
    }

    public int drawTextWithShadow(FontWrapper fontWrapper, String string, float x, float y, int color) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        delegate.drawTextWithShadow(textRenderer, string, (int) x, (int) y, color);

        return (int) x + textRenderer.getWidth(string);
    }
}
