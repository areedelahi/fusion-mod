package com.github.fusion.network;

import com.github.fusion.FusionMod;
import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkHandler {

    private NetworkHandler() {}

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(FusionMod.MOD_ID).versioned("1.0.0").optional();

        registrar.playToServer(
                PlayerInputPayload.TYPE,
                PlayerInputPayload.STREAM_CODEC,
                NetworkHandler::handlePlayerInput
        );

        registrar.playToServer(
                ArmActionPayload.TYPE,
                ArmActionPayload.STREAM_CODEC,
                NetworkHandler::handleArmAction
        );

        registrar.playToClient(
                FusionSyncPayload.TYPE,
                FusionSyncPayload.STREAM_CODEC,
                NetworkHandler::handleFusionSync
        );

        registrar.playToClient(
                GuestInputSyncPayload.TYPE,
                GuestInputSyncPayload.STREAM_CODEC,
                NetworkHandler::handleGuestInputSync
        );

        FusionMod.LOGGER.debug("Registered network payloads");
    }

    public static void sendFusionSync(ServerPlayer playerA, ServerPlayer playerB,
                                      Object dummyEntityParamToDeleteButSignatureUpdated, FusionInstance instance, boolean active) {
        int entityId = playerA.getId();
        FusionSyncPayload payload = new FusionSyncPayload(
                entityId,
                active,
                playerA.getUUID().toString(),
                playerB.getUUID().toString(),
                playerA.getName().getString(),
                playerB.getName().getString(),
                instance.getControlMode().getSerializedName(),
                instance.getOrientation().getSerializedName()
        );
        PacketDistributor.sendToPlayer(playerA, payload);
        PacketDistributor.sendToPlayer(playerB, payload);

        FusionMod.LOGGER.debug("Sent fusion sync (active={}) to {} and {}", active,
                playerA.getName().getString(), playerB.getName().getString());
    }

    public static void sendFusionSyncUnfused(ServerPlayer target, FusionInstance instance) {
        FusionSyncPayload payload = new FusionSyncPayload(
                -1,
                false,
                instance.getHost().toString(),
                instance.getGuest().toString(),
                "OfflineHost",
                "OfflineGuest",
                instance.getControlMode().getSerializedName(),
                instance.getOrientation().getSerializedName()
        );
        PacketDistributor.sendToPlayer(target, payload);
        FusionMod.LOGGER.debug("Sent default unfused sync to {}", target.getName().getString());
    }

    private static void handlePlayerInput(PlayerInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            FusionManager manager = FusionManager.get(serverPlayer.serverLevel());
            FusionInstance instance = manager.getFusionForPlayer(serverPlayer.getUUID());
            if (instance == null) return;

            if (instance.isGuest(serverPlayer.getUUID())) {
                ServerPlayer host = serverPlayer.serverLevel().getServer()
                        .getPlayerList().getPlayer(instance.getHost());
                if (host != null) {

                    GuestInputSyncPayload syncPayload = new GuestInputSyncPayload(
                            payload.forward(), payload.strafe(), payload.jumping(),
                            payload.sprinting(), payload.sneaking(), payload.headYaw(), payload.headPitch(),
                            payload.leftClick(), payload.rightClick()
                    );
                    PacketDistributor.sendToPlayer(host, syncPayload);
                }
            }
        });
    }

    private static void handleArmAction(ArmActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            FusionManager manager = FusionManager.get(serverPlayer.serverLevel());
            FusionInstance instance = manager.getFusionForPlayer(serverPlayer.getUUID());
            if (instance == null) return;

            if (!instance.isGuest(serverPlayer.getUUID())) return;

            ServerPlayer host = serverPlayer.serverLevel().getServer()
                    .getPlayerList().getPlayer(instance.getHost());
            if (host == null) return;

            switch (payload.actionType()) {
                case ArmActionPayload.ACTION_ATTACK -> {
                    if (payload.targetEntityId() >= 0) {
                        Entity target = serverPlayer.serverLevel().getEntity(payload.targetEntityId());
                        if (target != null) {
                            host.attack(target);
                        }
                    } else {

                        net.minecraft.world.phys.HitResult hostHit = host.pick(
                                host.blockInteractionRange(), 1.0f, false);
                        if (hostHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                            net.minecraft.world.phys.BlockHitResult blockHit =
                                    (net.minecraft.world.phys.BlockHitResult) hostHit;
                            host.gameMode.handleBlockBreakAction(
                                    blockHit.getBlockPos(),
                                    net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                                    blockHit.getDirection(),
                                    host.level().getMaxY(),
                                    0);
                        }
                    }
                }
                case ArmActionPayload.ACTION_USE -> {

                    host.gameMode.useItem(host, host.level(), host.getItemInHand(InteractionHand.MAIN_HAND),
                            InteractionHand.MAIN_HAND);
                }
                case ArmActionPayload.ACTION_USE_ON_BLOCK -> {

                    net.minecraft.world.phys.HitResult hostHit = host.pick(
                            host.blockInteractionRange(), 1.0f, false);
                    if (hostHit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                        net.minecraft.world.phys.BlockHitResult blockHit =
                                (net.minecraft.world.phys.BlockHitResult) hostHit;
                        host.gameMode.useItemOn(host, host.level(),
                                host.getItemInHand(InteractionHand.MAIN_HAND),
                                InteractionHand.MAIN_HAND, blockHit);
                    } else {

                        host.gameMode.useItem(host, host.level(),
                                host.getItemInHand(InteractionHand.MAIN_HAND),
                                InteractionHand.MAIN_HAND);
                    }
                }
                case ArmActionPayload.ACTION_USE_ON_ENTITY -> {
                    if (payload.targetEntityId() >= 0) {
                        Entity target = serverPlayer.serverLevel().getEntity(payload.targetEntityId());
                        if (target != null) {
                            host.interactOn(target, InteractionHand.MAIN_HAND);
                        }
                    }
                }
                default -> FusionMod.LOGGER.debug("Unknown arm action type: {}", payload.actionType());
            }
        });
    }

    private static void handleFusionSync(FusionSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.github.fusion.client.ClientPacketHandlers.handleFusionSync(payload));
    }

    private static void handleGuestInputSync(GuestInputSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.github.fusion.client.ClientPacketHandlers.handleGuestInputSync(payload));
    }
}
