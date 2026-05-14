package com.github.fusion.mixin;

import com.github.fusion.client.FusionInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class LocalPlayerMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onTurnPlayer(double movementTime, CallbackInfo ci) {
        if (!FusionInputHandler.isLocalPlayerFused) return;
        if (minecraft.player == null) return;

        boolean isGuest = minecraft.player.getId() != FusionInputHandler.fusedEntityId;

        if (isGuest) {

            double sens = minecraft.options.sensitivity().get() * 0.6F + 0.2F;
            double sensCubed = sens * sens * sens;
            double scale = sensCubed * 8.0;

            double dx = this.accumulatedDX * scale;
            double dy = this.accumulatedDY * scale;

            if (minecraft.options.invertYMouse().get()) {
                dy = -dy;
            }

            FusionInputHandler.guestDeltaYaw += dx;
            FusionInputHandler.guestDeltaPitch += dy;

            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;

            ci.cancel();
        }
    }
}
