package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@RequiredArgsConstructor
public class TextWrapperImpl extends AbstractTextWrapper {

    @Nonnull public final Text delegate;

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return textRenderer.wrapLines(delegate, wrapWidth).stream().map(OrderedTextWrapperImpl::new).toList();
    }

    public int getWidth(FontWrapper fontWrapper) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return textRenderer.getWidth(delegate);
    }

    @Nullable
    public StyleWrapper getStyleComponentFromLine(FontWrapper fontWrapper, int mouseXFromLeft) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        Style style = findStyleAtX(textRenderer, delegate.asOrderedText(), mouseXFromLeft);
        return style != null ? new StyleWrapperImpl(style) : null;
    }

    public int drawWithShadow(FontWrapper fontWrapper, GuiDrawContextWrapper context, float x, float y, int color) {
        DrawContext drawContext = ((GuiDrawContextWrapperImpl) context).delegate;
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;

        // Prefer drawText(..., shadow=true) when drawTextWithShadow returns void.
        boolean used = false;
        try {
            var m = drawContext.getClass().getMethod(
                    "drawText", TextRenderer.class, Text.class, int.class, int.class, int.class, boolean.class
            );
            m.invoke(drawContext, textRenderer, delegate, (int) x, (int) y, color, true);
            used = true;
        } catch (Throwable ignored) {}

        if (!used) {
            try {
                // fallback if your mapping still has it
                drawContext.drawTextWithShadow(textRenderer, delegate, (int) x, (int) y, color);
            } catch (Throwable ignored) {}
        }

        return (int) x + textRenderer.getWidth(delegate);
    }

    @Nullable
    private static Style findStyleAtX(TextRenderer textRenderer, OrderedText text, int mouseXFromLeft) {
        int[] x = { 0 };
        Style[] result = { null };
        text.accept((index, style, codePoint) -> {
            int width = textRenderer.getWidth(OrderedText.styled(codePoint, style));
            if (mouseXFromLeft < x[0] + width) {
                result[0] = style;
                return false;
            }
            x[0] += width;
            return true;
        });
        return result[0];
    }
}

