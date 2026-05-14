package com.github.fusion.mixin;

import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class GuestCollisionMixin {

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void onIsPickable(CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        FusionManager manager = FusionManager.get(self.serverLevel());
        FusionInstance fusion = manager.getFusionForPlayer(self.getUUID());
        if (fusion != null && fusion.isGuest(self.getUUID())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
    private void onCanBeCollidedWith(CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        FusionManager manager = FusionManager.get(self.serverLevel());
        FusionInstance fusion = manager.getFusionForPlayer(self.getUUID());
        if (fusion != null && fusion.isGuest(self.getUUID())) {
            cir.setReturnValue(false);
        }
    }
}
