package com.github.fusion.network;

import com.github.fusion.FusionMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ArmActionPayload(
        byte actionType,   
        int targetEntityId 
) implements CustomPacketPayload {

    public static final byte ACTION_ATTACK = 0;
    public static final byte ACTION_USE = 1;
    public static final byte ACTION_USE_ON_BLOCK = 2;
    public static final byte ACTION_USE_ON_ENTITY = 3;

    public static final Type<ArmActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FusionMod.MOD_ID, "arm_action"));

    public static final StreamCodec<ByteBuf, ArmActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE, ArmActionPayload::actionType,
                    ByteBufCodecs.INT, ArmActionPayload::targetEntityId,
                    ArmActionPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
