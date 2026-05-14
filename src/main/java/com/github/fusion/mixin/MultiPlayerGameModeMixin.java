package com.github.fusion.mixin;

import com.github.fusion.client.FusionInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void onUseItemOnHead(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (FusionInputHandler.isLocalPlayerFused && FusionInputHandler.fusedEntityId >= 0) {
            boolean isGuest = player.getId() != FusionInputHandler.fusedEntityId;
            if (isGuest) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null) {
                    net.minecraft.world.entity.Entity host = mc.level.getEntity(FusionInputHandler.fusedEntityId);
                    if (host != null) {

                        host.setPos(host.getX(), -1000, host.getZ());
                    }
                }
            }
        }
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void onUseItemOnReturn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (FusionInputHandler.isLocalPlayerFused && FusionInputHandler.fusedEntityId >= 0) {
            boolean isGuest = player.getId() != FusionInputHandler.fusedEntityId;
            if (isGuest) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null) {
                    net.minecraft.world.entity.Entity host = mc.level.getEntity(FusionInputHandler.fusedEntityId);
                    if (host != null) {

                        host.setPos(host.getX(), player.getY(), host.getZ());
                    }
                }
            }
        }
    }
}
