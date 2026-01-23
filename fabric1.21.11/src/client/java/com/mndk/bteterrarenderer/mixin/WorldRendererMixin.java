package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.core.tile.RenderManager;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapperImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void btetr$renderTilesAtEnd(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null || client.player == null) return;

        // We do not have the event-provided matrices/consumers anymore, so we use the client ones.
        MatrixStack stack = new MatrixStack();
        VertexConsumerProvider.Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();

        WorldDrawContextWrapper context = new WorldDrawContextWrapperImpl(stack, provider);

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();

        RenderManager.renderTiles(context, cameraPos.x, cameraPos.y, cameraPos.z);
    }
}

