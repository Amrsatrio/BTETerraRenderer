package com.mndk.bteterrarenderer.mcconnector.client;

import net.minecraft.client.util.Window;

public record WindowDimensionImpl(Window window) implements WindowDimension {

    @Override
    public int getPixelWidth() { return this.window.getFramebufferWidth(); }

    @Override
    public int getPixelHeight() { return this.window.getFramebufferHeight(); }

    @Override
    public int getScaledWidth() { return this.window.getScaledWidth(); }

    @Override
    public int getScaledHeight() { return this.window.getScaledHeight(); }

    @Override
    public float getScaleFactorX() {
        int scaled = getScaledWidth();
        return scaled == 0 ? 0f : (float) getPixelWidth() / (float) scaled;
    }

    @Override
    public float getScaleFactorY() {
        int scaled = getScaledHeight();
        return scaled == 0 ? 0f : (float) getPixelHeight() / (float) scaled;
    }
}
