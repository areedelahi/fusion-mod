package com.github.fusion.client;

import com.github.fusion.network.FusionSyncPayload;
import com.github.fusion.network.GuestInputSyncPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {}

    private static boolean wasLeftClickDown = false;
    private static boolean wasRightClickDown = false;

    private static boolean isPhysicallyDown(InputConstants.Key key, long window) {
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        } else {
            return InputConstants.isKeyDown(window, key.getValue());
        }
    }

    public static void handleFusionSync(FusionSyncPayload payload) {
        FusionInputHandler.isLocalPlayerFused = payload.active();

        if (payload.active()) {
            FusionInputHandler.fusedEntityId = payload.entityId();
            FusionInputHandler.playerAUUID = payload.playerAUUID();
            FusionInputHandler.playerBUUID = payload.playerBUUID();
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) mc.setCameraEntity(mc.player);
            FusionInputHandler.fusedEntityId = -1;
            FusionInputHandler.playerAUUID = null;
            FusionInputHandler.playerBUUID = null;
        }
    }

    public static void handleGuestInputSync(GuestInputSyncPayload payload) {

        FusionInputHandler.receiveGuestInput(payload);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !FusionInputHandler.isLocalPlayerFused) return;

        boolean isHost = mc.player.getId() == FusionInputHandler.fusedEntityId;
        if (!isHost) return;

        long window = mc.getWindow().getWindow();

        boolean mergedLeft = isPhysicallyDown(mc.options.keyAttack.getKey(), window) || payload.leftClick();
        KeyMapping.set(mc.options.keyAttack.getKey(), mergedLeft);
        if (payload.leftClick() && !wasLeftClickDown) {
            KeyMapping.click(mc.options.keyAttack.getKey());
        }
        wasLeftClickDown = payload.leftClick();

        boolean mergedRight = isPhysicallyDown(mc.options.keyUse.getKey(), window) || payload.rightClick();
        KeyMapping.set(mc.options.keyUse.getKey(), mergedRight);
        if (payload.rightClick() && !wasRightClickDown) {
            KeyMapping.click(mc.options.keyUse.getKey());
        }
        wasRightClickDown = payload.rightClick();

        float deltaYaw = payload.headYaw();
        float deltaPitch = payload.headPitch();
        if (deltaYaw != 0f || deltaPitch != 0f) {
            mc.player.turn(deltaYaw, deltaPitch);
        }
    }
}
