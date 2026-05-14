package com.github.fusion.mixin;

import com.github.fusion.client.FusionInputHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class ClientPlayerCollisionMixin {

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void onIsPickable(CallbackInfoReturnable<Boolean> cir) {
        if (FusionInputHandler.isLocalPlayerFused) {
            Player self = (Player) (Object) this;
            String selfUUID = self.getUUID().toString();
            String playerA = FusionInputHandler.playerAUUID;
            String playerB = FusionInputHandler.playerBUUID;

            if (selfUUID.equals(playerA) || selfUUID.equals(playerB)) {

                if (self.getId() != FusionInputHandler.fusedEntityId) {

                    cir.setReturnValue(false);
                }
            }
        }
    }
}
