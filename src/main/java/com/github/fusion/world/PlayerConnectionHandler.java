package com.github.fusion.world;

import com.github.fusion.FusionMod;
import com.github.fusion.data.FusionManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = FusionMod.MOD_ID)
public class PlayerConnectionHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FusionManager manager = FusionManager.get(player.serverLevel());
            manager.onPlayerLogout(player);
            FusionMod.LOGGER.debug("Handled logout for fused player: {}", player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FusionManager manager = FusionManager.get(player.serverLevel());
            manager.onPlayerLogin(player);
            FusionMod.LOGGER.debug("Handled login for fused player: {}", player.getName().getString());
        }
    }
}
