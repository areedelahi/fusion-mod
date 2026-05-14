package com.github.fusion.mixin;

import com.github.fusion.client.FusionInputHandler;
import com.github.fusion.network.GuestInputSyncPayload;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class HostKeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void mergeGuestInput(CallbackInfo ci) {
        if (!FusionInputHandler.isLocalPlayerFused) return;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        KeyboardInput self = (KeyboardInput) (Object) this;

        boolean isGuest = mc.player.getId() != FusionInputHandler.fusedEntityId;

        if (isGuest) {

            self.forwardImpulse = 0;
            self.leftImpulse    = 0;
            self.keyPresses = new Input(false, false, false, false, false, false, false);
            return;
        }

        GuestInputSyncPayload guestInput = FusionInputHandler.latestGuestInput;
        if (guestInput == null) return;

        self.forwardImpulse += guestInput.forward();
        self.leftImpulse    += guestInput.strafe();

        if (self.forwardImpulse >  1.0f) self.forwardImpulse =  1.0f;
        if (self.forwardImpulse < -1.0f) self.forwardImpulse = -1.0f;
        if (self.leftImpulse    >  1.0f) self.leftImpulse    =  1.0f;
        if (self.leftImpulse    < -1.0f) self.leftImpulse    = -1.0f;

        Input existing = self.keyPresses;
        if (guestInput.jumping() || guestInput.sneaking() || guestInput.sprinting()) {
            self.keyPresses = new Input(
                    existing.forward()   || (guestInput.forward()  > 0),
                    existing.backward()  || (guestInput.forward()  < 0),
                    existing.left()      || (guestInput.strafe()   > 0),
                    existing.right()     || (guestInput.strafe()   < 0),
                    existing.jump()      || guestInput.jumping(),
                    existing.shift()     || guestInput.sneaking(),
                    existing.sprint()    || guestInput.sprinting()
            );
        }
    }
}
