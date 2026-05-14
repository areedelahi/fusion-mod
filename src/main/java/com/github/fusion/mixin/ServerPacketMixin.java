package com.github.fusion.mixin;

import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPacketMixin {

    @Shadow
    public ServerPlayer player;

    private ServerPlayer getHost() {
        FusionManager manager = FusionManager.get(player.serverLevel());
        FusionInstance fusion = manager.getFusionForPlayer(player.getUUID());
        if (fusion != null && fusion.isGuest(player.getUUID())) {
            return player.serverLevel().getServer().getPlayerList().getPlayer(fusion.getHost());
        }
        return null;
    }

    @Inject(method = "handleContainerClick", at = @At("HEAD"), cancellable = true)
    private void onHandleContainerClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        ServerPlayer host = getHost();
        if (host != null && getFusion().isHostOnline()) {
            host.connection.handleContainerClick(packet);
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCarriedItem", at = @At("HEAD"))
    private void onHandleSetCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        ServerPlayer host = getHost();
        if (host != null && getFusion().isHostOnline()) {

            host.connection.handleSetCarriedItem(packet);

            host.connection.send(new net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket(packet.getSlot()));

        }
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void onHandlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        FusionInstance fusion = getFusion();
        if (fusion != null && fusion.isGuest(player.getUUID()) && fusion.isHostOnline()) ci.cancel();
    }

    @Inject(method = "handleUseItem", at = @At("HEAD"), cancellable = true)
    private void onHandleUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        FusionInstance fusion = getFusion();
        if (fusion != null && fusion.isGuest(player.getUUID()) && fusion.isHostOnline()) ci.cancel();
    }

    @Inject(method = "handleUseItemOn", at = @At("HEAD"), cancellable = true)
    private void onHandleUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        FusionInstance fusion = getFusion();
        if (fusion != null && fusion.isGuest(player.getUUID()) && fusion.isHostOnline()) ci.cancel();
    }

    private FusionInstance getFusion() {
        FusionManager manager = FusionManager.get(player.serverLevel());
        return manager.getFusionForPlayer(player.getUUID());
    }
}
