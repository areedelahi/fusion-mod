package com.github.fusion.mixin;

import com.github.fusion.client.FusionInputHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (FusionInputHandler.isLocalPlayerFused && FusionInputHandler.fusedEntityId >= 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getId() != FusionInputHandler.fusedEntityId) {

                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean leftClick, CallbackInfo ci) {
        if (FusionInputHandler.isLocalPlayerFused && FusionInputHandler.fusedEntityId >= 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getId() != FusionInputHandler.fusedEntityId) {

                ci.cancel();
            }
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void onStartUseItem(CallbackInfo ci) {
        if (FusionInputHandler.isLocalPlayerFused && FusionInputHandler.fusedEntityId >= 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getId() != FusionInputHandler.fusedEntityId) {

                ci.cancel();
            }
        }
    }
}
