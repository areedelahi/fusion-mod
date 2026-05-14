package com.github.fusion.world;

import com.github.fusion.FusionMod;
import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import com.github.fusion.network.NetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = FusionMod.MOD_ID)
public class DeathHandler {

    private static boolean killingPartner = false;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer deadPlayer)) return;
        if (deadPlayer.level().isClientSide) return;
        if (killingPartner) return;

        ServerLevel level = (ServerLevel) deadPlayer.level();
        FusionManager manager = FusionManager.get(level);
        FusionInstance instance = manager.getFusionForPlayer(deadPlayer.getUUID());
        if (instance == null) return;

        MinecraftServer server = level.getServer();

        ServerPlayer host = server.getPlayerList().getPlayer(instance.getHost());
        ServerPlayer guest = server.getPlayerList().getPlayer(instance.getGuest());

        if (guest != null) {
            guest.setInvisible(false);
            guest.setInvulnerable(false);
            guest.noPhysics = false;
        }

        if (host != null) NetworkHandler.sendFusionSyncUnfused(host, instance);
        if (guest != null) NetworkHandler.sendFusionSyncUnfused(guest, instance);

        ServerPlayer partner;
        if (instance.isHost(deadPlayer.getUUID())) {
            partner = guest;
        } else {
            partner = host;
        }

        if (partner != null && partner.isAlive()) {
            killingPartner = true;
            try {
                partner.teleportTo(deadPlayer.getX(), deadPlayer.getY(), deadPlayer.getZ());
                partner.hurt(event.getSource(), Float.MAX_VALUE);
            } finally {
                killingPartner = false;
            }
        }

        FusionMod.LOGGER.info("Fused player {} died. Partner killed too. Fusion {} persists.",
                deadPlayer.getName().getString(), instance.getFusionId());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer respawned)) return;

        FusionManager manager = FusionManager.get(respawned.serverLevel());
        FusionInstance instance = manager.getFusionForPlayer(respawned.getUUID());
        if (instance == null) return;

        MinecraftServer server = respawned.getServer();
        if (server == null) return;

        ServerPlayer host = server.getPlayerList().getPlayer(instance.getHost());
        ServerPlayer guest = server.getPlayerList().getPlayer(instance.getGuest());

        if (host == null || guest == null) return;
        if (host.isDeadOrDying() || guest.isDeadOrDying()) return;

        if (guest.level() != host.level()) {
            guest.teleportTo((net.minecraft.server.level.ServerLevel) host.level(), host.getX(), host.getY(), host.getZ(), java.util.Set.of(), host.getYRot(), host.getXRot(), false);
        } else {
            guest.teleportTo(host.getX(), host.getY(), host.getZ());
        }
        guest.setInvisible(true);
        guest.setInvulnerable(true);
        guest.noPhysics = true;
        guest.stopRiding();

        int size = host.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            guest.getInventory().setItem(i, host.getInventory().getItem(i).copy());
        }
        guest.getInventory().selected = host.getInventory().selected;
        guest.setHealth(host.getHealth());
        guest.getFoodData().setFoodLevel(host.getFoodData().getFoodLevel());
        guest.getFoodData().setSaturation(host.getFoodData().getSaturationLevel());
        guest.experienceLevel = host.experienceLevel;
        guest.experienceProgress = host.experienceProgress;
        guest.totalExperience = host.totalExperience;

        NetworkHandler.sendFusionSync(host, guest, null, instance, true);

        FusionMod.LOGGER.info("Both players respawned. Fusion {} re-established.", instance.getFusionId());
    }
}
