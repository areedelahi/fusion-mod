package com.github.fusion.client;

import com.github.fusion.network.GuestInputSyncPayload;
import com.github.fusion.network.ArmActionPayload;
import com.github.fusion.network.PlayerInputPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = com.github.fusion.FusionMod.MOD_ID, value = Dist.CLIENT)
public class FusionInputHandler {

    public static volatile boolean isLocalPlayerFused = false;
    public static volatile int fusedEntityId = -1;
    public static volatile String playerAUUID = null;
    public static volatile String playerBUUID = null;

    public static double guestDeltaYaw = 0.0;
    public static double guestDeltaPitch = 0.0;

    public static volatile GuestInputSyncPayload latestGuestInput = null;

    public static void receiveGuestInput(GuestInputSyncPayload payload) {
        latestGuestInput = payload;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !isLocalPlayerFused) return;

        boolean isGuest = mc.player.getId() != fusedEntityId;

        var options = mc.options;
        float forward = 0f;
        float strafe = 0f;

        if (options.keyUp.isDown()) forward += 1f;
        if (options.keyDown.isDown()) forward -= 1f;
        if (options.keyLeft.isDown()) strafe += 1f;
        if (options.keyRight.isDown()) strafe -= 1f;

        boolean jumping = options.keyJump.isDown();
        boolean sprinting = options.keySprint.isDown();
        boolean sneaking = options.keyShift.isDown();

        boolean leftClick = options.keyAttack.isDown();
        boolean rightClick = options.keyUse.isDown();

        if (isGuest) {

            float sendYaw = (float) guestDeltaYaw;
            float sendPitch = (float) guestDeltaPitch;

            guestDeltaYaw = 0.0;
            guestDeltaPitch = 0.0;

            PlayerInputPayload payload = new PlayerInputPayload(
                    forward, strafe, jumping, sprinting, sneaking, sendYaw, sendPitch,
                    leftClick, rightClick
            );
            PacketDistributor.sendToServer(payload);

            if (leftClick) {
                PacketDistributor.sendToServer(new ArmActionPayload(ArmActionPayload.ACTION_ATTACK, -1));
            }

            if (mc.level != null && fusedEntityId >= 0) {
                net.minecraft.world.entity.Entity hostEntity = mc.level.getEntity(fusedEntityId);
                if (hostEntity != null) {
                    if (mc.getCameraEntity() != hostEntity) {
                        mc.setCameraEntity(hostEntity);
                    }

                    player.setPosRaw(hostEntity.getX(), hostEntity.getY(), hostEntity.getZ());
                    player.xo = hostEntity.xo;
                    player.yo = hostEntity.yo;
                    player.zo = hostEntity.zo;

                    if (hostEntity instanceof net.minecraft.world.entity.player.Player hostPlayer) {

                        for (int i = 0; i < 9; i++) {
                            hostPlayer.getInventory().items.set(i, player.getInventory().items.get(i));
                        }
                        hostPlayer.getInventory().selected = player.getInventory().selected;
                        hostPlayer.getInventory().offhand.set(0, player.getInventory().offhand.get(0));

                        hostPlayer.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel());
                        hostPlayer.getFoodData().setSaturation(player.getFoodData().getSaturationLevel());
                    }
                }
            }
        } else {

            if (mc.getCameraEntity() != player) {
                mc.setCameraEntity(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetState();
    }

    public static void resetState() {
        isLocalPlayerFused = false;
        fusedEntityId = -1;
        playerAUUID = null;
        playerBUUID = null;
        guestDeltaYaw = 0.0;
        guestDeltaPitch = 0.0;
        latestGuestInput = null;
    }
}
