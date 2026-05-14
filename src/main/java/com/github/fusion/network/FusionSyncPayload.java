package com.github.fusion.network;

import com.github.fusion.FusionMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record FusionSyncPayload(
        int entityId,         
        boolean active,       
        String playerAUUID,   
        String playerBUUID,   
        String playerAName,   
        String playerBName,   
        String controlMode,   
        String orientation    
) implements CustomPacketPayload {

    public static final Type<FusionSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FusionMod.MOD_ID, "fusion_sync"));

    public static final StreamCodec<ByteBuf, FusionSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, FusionSyncPayload::entityId,
                    ByteBufCodecs.BOOL, FusionSyncPayload::active,
                    ByteBufCodecs.STRING_UTF8, FusionSyncPayload::playerAUUID,
                    ByteBufCodecs.STRING_UTF8, FusionSyncPayload::playerBUUID,
                    ByteBufCodecs.STRING_UTF8, FusionSyncPayload::playerAName,
                    ByteBufCodecs.STRING_UTF8, FusionSyncPayload::playerBName,
                    ByteBufCodecs.STRING_UTF8, FusionSyncPayload::controlMode,
                    ByteBufCodecs.STRING_UTF8, FusionSyncPayload::orientation,
                    FusionSyncPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

