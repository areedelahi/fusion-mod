package com.github.fusion.mixin;

import com.github.fusion.client.FusionInputHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRenderMixin<T extends Entity> {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (!FusionInputHandler.isLocalPlayerFused) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean isGuest = mc.player.getId() != FusionInputHandler.fusedEntityId;

        if (isGuest) {

            if (entity.getId() == mc.player.getId()) {
                cir.setReturnValue(false);
                return;
            }

            if (mc.options.getCameraType() == CameraType.FIRST_PERSON
                    && entity.getId() == FusionInputHandler.fusedEntityId) {
                cir.setReturnValue(false);
            }
        }
    }
}
