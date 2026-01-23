package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class OrderedTextWrapperImpl extends AbstractTextWrapper {

    @Nonnull public final OrderedText delegate;

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        StringVisitable text = this.toStringVisitable();
        return textRenderer.wrapLines(text, wrapWidth).stream().map(OrderedTextWrapperImpl::new).toList();
    }

    public int getWidth(FontWrapper fontWrapper) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return textRenderer.getWidth(delegate);
    }

    @Nullable
    public StyleWrapper getStyleComponentFromLine(FontWrapper fontWrapper, int mouseXFromLeft) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        Style style = findStyleAtX(textRenderer, delegate, mouseXFromLeft);
        return style != null ? new StyleWrapperImpl(style) : null;
    }

    public int drawWithShadow(FontWrapper fontWrapper, GuiDrawContextWrapper context, float x, float y, int color) {
        DrawContext drawContext = ((GuiDrawContextWrapperImpl) context).delegate;
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        drawContext.drawTextWithShadow(textRenderer, delegate, (int) x, (int) y, color);
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

    private StringVisitable toStringVisitable() {
        TextCollector textCollector = new TextCollector();
        AtomicReference<Style> lastStyle = new AtomicReference<>();
        AtomicReference<String> lastString = new AtomicReference<>("");
        delegate.accept((index, style, codePoint) -> {
            String append = new String(Character.toChars(codePoint));
            if (Objects.equals(style, lastStyle.get())) {
                lastString.set(lastString.get() + append);
                return true;
            }
            if (!lastString.get().isEmpty()) {
                textCollector.add(StringVisitable.styled(lastString.get(), lastStyle.get()));
            }
            lastStyle.set(style);
            lastString.set(append);
            return true;
        });
        if (!lastString.get().isEmpty()) {
            textCollector.add(StringVisitable.styled(lastString.get(), lastStyle.get()));
        }
        return textCollector.getCombined();
    }
}
