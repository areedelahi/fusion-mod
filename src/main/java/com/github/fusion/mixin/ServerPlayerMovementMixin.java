package com.github.fusion.mixin;

import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import com.github.fusion.network.NetworkHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayerMovementMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void onHandleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        FusionManager manager = FusionManager.get(player.serverLevel());
        FusionInstance fusion = manager.getFusionForPlayer(player.getUUID());

        if (fusion != null && fusion.isGuest(player.getUUID()) && fusion.isHostOnline()) {

            ServerPlayer host = player.serverLevel().getServer()
                    .getPlayerList().getPlayer(fusion.getHost());
            if (host != null) {
                player.moveTo(host.getX(), host.getY(), host.getZ());
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", at = @At("HEAD"), cancellable = true)
    private void onHandleInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
        FusionManager manager = FusionManager.get(player.serverLevel());
        FusionInstance fusion = manager.getFusionForPlayer(player.getUUID());

        if (fusion != null && fusion.isGuest(player.getUUID()) && fusion.isHostOnline()) {
            ci.cancel();
        }
    }
}
