package com.github.fusion.data;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FusionInstance {
    private final UUID fusionId;
    private final UUID host; 
    private final UUID guest; 
    private ControlMode controlMode;
    private FusionOrientation orientation;
    private final long fusedAtTick;
    @Nullable
    private UUID dominantPlayer; 
    private boolean hostOnline;
    private boolean guestOnline;

    public FusionInstance(UUID fusionId, UUID host, UUID guest,
                          ControlMode controlMode, FusionOrientation orientation,
                          long fusedAtTick, @Nullable UUID dominantPlayer) {
        this.fusionId = fusionId;
        this.host = host;
        this.guest = guest;
        this.controlMode = controlMode;
        this.orientation = orientation;
        this.fusedAtTick = fusedAtTick;
        this.dominantPlayer = dominantPlayer;
        this.hostOnline = true;
        this.guestOnline = true;
    }

    public UUID getFusionId() { return fusionId; }
    public UUID getHost() { return host; }
    public UUID getGuest() { return guest; }
    public ControlMode getControlMode() { return controlMode; }
    public FusionOrientation getOrientation() { return orientation; }
    public long getFusedAtTick() { return fusedAtTick; }
    @Nullable public UUID getDominantPlayer() { return dominantPlayer; }
    public boolean isHostOnline() { return hostOnline; }
    public boolean isGuestOnline() { return guestOnline; }

    public void setControlMode(ControlMode controlMode) { this.controlMode = controlMode; }
    public void setOrientation(FusionOrientation orientation) { this.orientation = orientation; }
    public void setDominantPlayer(@Nullable UUID dominantPlayer) { this.dominantPlayer = dominantPlayer; }
    public void setHostOnline(boolean online) { this.hostOnline = online; }
    public void setGuestOnline(boolean online) { this.guestOnline = online; }

    public boolean containsPlayer(UUID playerUUID) {
        return host.equals(playerUUID) || guest.equals(playerUUID);
    }

    @Nullable
    public UUID getPartner(UUID playerUUID) {
        if (host.equals(playerUUID)) return guest;
        if (guest.equals(playerUUID)) return host;
        return null;
    }

    public boolean isHost(UUID playerUUID) {
        return host.equals(playerUUID);
    }

    public boolean isGuest(UUID playerUUID) {
        return guest.equals(playerUUID);
    }

    public boolean isPlayerOnline(UUID playerUUID) {
        if (host.equals(playerUUID)) return hostOnline;
        if (guest.equals(playerUUID)) return guestOnline;
        return false;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("FusionId", fusionId);
        tag.putUUID("Host", host);
        tag.putUUID("Guest", guest);
        tag.putString("ControlMode", controlMode.getSerializedName());
        tag.putString("Orientation", orientation.getSerializedName());
        tag.putLong("FusedAtTick", fusedAtTick);
        if (dominantPlayer != null) {
            tag.putUUID("DominantPlayer", dominantPlayer);
        }
        tag.putBoolean("HostOnline", hostOnline);
        tag.putBoolean("GuestOnline", guestOnline);
        return tag;
    }

    public static FusionInstance load(CompoundTag tag) {
        FusionInstance instance = new FusionInstance(
                tag.getUUID("FusionId"),
                tag.getUUID("Host"),
                tag.getUUID("Guest"),
                ControlMode.byName(tag.getString("ControlMode")),
                FusionOrientation.byName(tag.getString("Orientation")),
                tag.getLong("FusedAtTick"),
                tag.contains("DominantPlayer") ? tag.getUUID("DominantPlayer") : null
        );
        instance.setHostOnline(tag.getBoolean("HostOnline"));
        instance.setGuestOnline(tag.getBoolean("GuestOnline"));
        return instance;
    }

    @Override
    public String toString() {
        return "FusionInstance{" +
                "fusionId=" + fusionId +
                ", host=" + host +
                ", guest=" + guest +
                ", mode=" + controlMode +
                ", orientation=" + orientation +
                '}';
    }
}
