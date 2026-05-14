package com.github.fusion.data;

import com.github.fusion.network.NetworkHandler;

import com.github.fusion.FusionMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FusionManager extends SavedData {
    private static final String DATA_NAME = FusionMod.MOD_ID + "_fusions";

    private final Map<UUID, FusionInstance> fusions = new HashMap<>();
    private final Map<UUID, UUID> playerIndex = new HashMap<>();

    public static FusionManager get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return get(overworld);
    }

    public static FusionManager get(ServerLevel level) {
        return level.getServer().overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(FusionManager::new, FusionManager::load),
                        DATA_NAME
                );
    }

    public FusionManager() {
    }

    @Nullable
    public FusionInstance fuse(ServerPlayer playerA, ServerPlayer playerB,
                               ControlMode mode, FusionOrientation orientation) {
        if (isPlayerFused(playerA.getUUID()) || isPlayerFused(playerB.getUUID())) {
            return null;
        }

        UUID fusionId = UUID.randomUUID();
        long currentTick = playerA.level().getGameTime();

        FusionInstance instance = new FusionInstance(
                fusionId, playerA.getUUID(), playerB.getUUID(),
                mode, orientation, currentTick, null
        );

        hidePlayer(playerB, playerA);

        fusions.put(fusionId, instance);
        playerIndex.put(playerA.getUUID(), fusionId);
        playerIndex.put(playerB.getUUID(), fusionId);

        NetworkHandler.sendFusionSync(playerA, playerB, null, instance, true);

        setDirty();

        FusionMod.LOGGER.info("Created fusion {} between Host {} and Guest {}",
                fusionId, playerA.getName().getString(), playerB.getName().getString());

        return instance;
    }

    public boolean unfuse(UUID fusionId, MinecraftServer server) {
        FusionInstance instance = fusions.get(fusionId);
        if (instance == null) return false;

        ServerPlayer host = server.getPlayerList().getPlayer(instance.getHost());
        ServerPlayer guest = server.getPlayerList().getPlayer(instance.getGuest());

        if (guest != null && host != null) {
            restorePlayer(guest, host.getX(), host.getY(), host.getZ());
            NetworkHandler.sendFusionSync(host, guest, null, instance, false);
        } else {

            if (host != null) NetworkHandler.sendFusionSyncUnfused(host, instance);
            if (guest != null) NetworkHandler.sendFusionSyncUnfused(guest, instance);
        }

        fusions.remove(fusionId);
        playerIndex.remove(instance.getHost());
        playerIndex.remove(instance.getGuest());

        setDirty();

        FusionMod.LOGGER.info("Dissolved fusion {}", fusionId);
        return true;
    }

    public boolean unfusePlayer(UUID playerUUID, MinecraftServer server) {
        UUID fusionId = playerIndex.get(playerUUID);
        if (fusionId == null) return false;
        return unfuse(fusionId, server);
    }

    @Nullable
    public FusionInstance getFusionForPlayer(UUID playerUUID) {
        UUID fusionId = playerIndex.get(playerUUID);
        if (fusionId == null) return null;
        return fusions.get(fusionId);
    }

    public boolean isPlayerFused(UUID playerUUID) {
        return playerIndex.containsKey(playerUUID);
    }

    public Collection<FusionInstance> getAllFusions() {
        return Collections.unmodifiableCollection(fusions.values());
    }

    private void hidePlayer(ServerPlayer guest, ServerPlayer host) {

        guest.moveTo(host.getX(), host.getY(), host.getZ());
        guest.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        guest.setInvisible(true);
        guest.setInvulnerable(true);
        guest.noPhysics = true;

        guest.stopRiding();
    }

    private void restorePlayer(ServerPlayer guest, double x, double y, double z) {
        guest.teleportTo(x, y, z);
        guest.setInvisible(false);
        guest.setInvulnerable(false);
        guest.noPhysics = false;
    }

    private void syncPlayerState(ServerPlayer activePlayer, ServerPlayer returningPlayer) {

        int size = activePlayer.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            returningPlayer.getInventory().setItem(i, activePlayer.getInventory().getItem(i).copy());
        }
        returningPlayer.getInventory().selected = activePlayer.getInventory().selected;

        returningPlayer.setHealth(activePlayer.getHealth());
        returningPlayer.getFoodData().setFoodLevel(activePlayer.getFoodData().getFoodLevel());
        returningPlayer.getFoodData().setSaturation(activePlayer.getFoodData().getSaturationLevel());
        returningPlayer.experienceLevel = activePlayer.experienceLevel;
        returningPlayer.experienceProgress = activePlayer.experienceProgress;
        returningPlayer.totalExperience = activePlayer.totalExperience;

        com.github.fusion.world.FusionTickHandler.resetTracking();
    }

    public void onPlayerLogout(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        FusionInstance instance = getFusionForPlayer(playerUUID);
        if (instance == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (instance.isHost(playerUUID)) {
            instance.setHostOnline(false);
            ServerPlayer guest = server.getPlayerList().getPlayer(instance.getGuest());
            if (guest != null) {

                restorePlayer(guest, guest.getX(), guest.getY(), guest.getZ());
                NetworkHandler.sendFusionSyncUnfused(guest, instance);
                FusionMod.LOGGER.info("Host logged out. Unfused guest {} temporarily.", guest.getName().getString());
            }
        } else {
            instance.setGuestOnline(false);
            ServerPlayer host = server.getPlayerList().getPlayer(instance.getHost());
            if (host != null) {

                NetworkHandler.sendFusionSyncUnfused(host, instance);
                FusionMod.LOGGER.info("Guest logged out. Unfused host {} temporarily.", host.getName().getString());
            }
        }
        setDirty();
    }

    public void onPlayerLogin(ServerPlayer player) {
        FusionInstance instance = getFusionForPlayer(player.getUUID());
        if (instance == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (instance.isHost(player.getUUID())) {
            instance.setHostOnline(true);
            ServerPlayer guest = server.getPlayerList().getPlayer(instance.getGuest());
            if (guest != null) {

                syncPlayerState(guest, player);

                double gx = guest.getX(), gy = guest.getY(), gz = guest.getZ();
                player.teleportTo(gx, gy, gz);

                guest.moveTo(gx, gy, gz);
                guest.setInvisible(true);
                guest.setInvulnerable(true);
                guest.noPhysics = true;
                guest.stopRiding();
                NetworkHandler.sendFusionSync(player, guest, null, instance, true);
                FusionMod.LOGGER.info("Host {} rejoined guest {}. Fusion restored.", player.getName().getString(), guest.getName().getString());
            } else {

                restorePlayer(player, player.getX(), player.getY(), player.getZ());
                NetworkHandler.sendFusionSyncUnfused(player, instance);
            }
        } else {
            instance.setGuestOnline(true);
            ServerPlayer host = server.getPlayerList().getPlayer(instance.getHost());
            if (host != null) {

                syncPlayerState(host, player);

                hidePlayer(player, host);
                NetworkHandler.sendFusionSync(host, player, null, instance, true);
                FusionMod.LOGGER.info("Reattached guest {} to host {}. Fusion restored.", player.getName().getString(), host.getName().getString());
            } else {

                restorePlayer(player, player.getX(), player.getY(), player.getZ());
                NetworkHandler.sendFusionSyncUnfused(player, instance);
            }
        }

        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag fusionList = new ListTag();
        for (FusionInstance instance : fusions.values()) {
            fusionList.add(instance.save());
        }
        tag.put("Fusions", fusionList);
        return tag;
    }

    public static FusionManager load(CompoundTag tag, HolderLookup.Provider registries) {
        FusionManager manager = new FusionManager();
        ListTag fusionList = tag.getList("Fusions", Tag.TAG_COMPOUND);
        for (int i = 0; i < fusionList.size(); i++) {
            FusionInstance instance = FusionInstance.load(fusionList.getCompound(i));
            manager.fusions.put(instance.getFusionId(), instance);
            manager.playerIndex.put(instance.getHost(), instance.getFusionId());
            manager.playerIndex.put(instance.getGuest(), instance.getFusionId());
        }
        FusionMod.LOGGER.info("Loaded {} active fusions", manager.fusions.size());
        return manager;
    }
}
