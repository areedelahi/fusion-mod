package com.github.fusion.world;

import com.github.fusion.FusionMod;
import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = FusionMod.MOD_ID)
public class PortalHandler {

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer hostPlayer)) return;

        ServerLevel newLevel = (ServerLevel) hostPlayer.level();
        FusionManager manager = FusionManager.get(newLevel.getServer());
        FusionInstance instance = manager.getFusionForPlayer(hostPlayer.getUUID());
        if (instance == null) return;

        if (instance.isHost(hostPlayer.getUUID())) {
            FusionMod.LOGGER.info("Host {} traveled to dimension. Moving Guest.", hostPlayer.getName().getString());

            var server = newLevel.getServer();
            ServerPlayer guest = server.getPlayerList().getPlayer(instance.getGuest());

            if (guest != null && guest.level() != newLevel) {
                guest.teleportTo(newLevel, hostPlayer.getX(), hostPlayer.getY(), hostPlayer.getZ(),
                        java.util.Set.of(), hostPlayer.getYRot(), hostPlayer.getXRot(), false);
            }
        }
    }
}
