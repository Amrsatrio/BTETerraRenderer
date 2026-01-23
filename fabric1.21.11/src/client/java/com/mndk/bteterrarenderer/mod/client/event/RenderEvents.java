package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.tile.RenderManager;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapperImpl;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapperImpl;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;

public final class RenderEvents {

    private RenderEvents() {}

    public static void registerEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(RenderEvents::onWorldRender);
        HudRenderCallback.EVENT.register(RenderEvents::onHudRender);
    }

    @SuppressWarnings("resource")
    private static void onWorldRender(WorldRenderContext renderContext) {
        MinecraftClient client = renderContext.gameRenderer().getClient();
        if (client.world == null || client.player == null) return;

        MatrixStack stack = renderContext.matrices();
        VertexConsumerProvider provider = renderContext.consumers();
        if (stack == null || provider == null) return;

        WorldDrawContextWrapper context = new WorldDrawContextWrapperImpl(stack, provider);
        Camera camera = renderContext.gameRenderer().getCamera();
        Vec3d cameraPos = camera.getCameraPos();

        Profilers.get().swap("bteterrarenderer-hologram");
        RenderManager.renderTiles(context, cameraPos.x, cameraPos.y, cameraPos.z);
    }

    private static void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        GuiDrawContextWrapper wrapper = new GuiDrawContextWrapperImpl(drawContext);
        RenderManager.renderHud(wrapper);
    }
}
