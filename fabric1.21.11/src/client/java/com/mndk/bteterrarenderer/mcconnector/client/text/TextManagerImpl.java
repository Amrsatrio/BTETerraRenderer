package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextFormatCopy;
import com.mndk.bteterrarenderer.mcconnector.client.gui.text.TextManager;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import javax.annotation.Nonnull;

public class TextManagerImpl implements TextManager {

    @Override
    public TextWrapper fromJson(@Nonnull String json) {
        try {
            // 1.21+: decode Text via codecs instead of Text.Serialization
            var element = com.google.gson.JsonParser.parseString(json);

            var result = TextCodecs.CODEC
                    .parse(DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE), element)
                    .result()
                    .orElse(null);

            return result != null ? new TextWrapperImpl(result) : null;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public TextWrapper fromString(@Nonnull String text) {
        return new TextWrapperImpl(Text.literal(text));
    }

    @Override
    public StyleWrapper emptyStyle() {
        return new StyleWrapperImpl(Style.EMPTY);
    }

    @Override
    public StyleWrapper styleWithColor(StyleWrapper styleWrapper, TextFormatCopy textColor) {
        Style style = ((StyleWrapperImpl) styleWrapper).delegate();
        return new StyleWrapperImpl(style.withColor(textColor.getColorIndex()));
    }

    @Override
    public boolean handleClick(@Nonnull StyleWrapper styleWrapper) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen == null) return false;

        Style style = ((StyleWrapperImpl) styleWrapper).delegate();

        if (style.getClickEvent() == null) return false;

        try {
            var method = Screen.class.getDeclaredMethod(
                    "handleClickEvent",
                    net.minecraft.text.ClickEvent.class,
                    MinecraftClient.class,
                    Screen.class
            );
            method.setAccessible(true);
            method.invoke(null, style.getClickEvent(), MinecraftClient.getInstance(), currentScreen);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
