package com.github.fusion.network;

import com.github.fusion.FusionMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GuestInputSyncPayload(
        float forward,
        float strafe,
        boolean jumping,
        boolean sprinting,
        boolean sneaking,
        float headYaw,
        float headPitch,
        boolean leftClick,
        boolean rightClick
) implements CustomPacketPayload {

    public static final Type<GuestInputSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FusionMod.MOD_ID, "guest_input_sync"));

    public static final StreamCodec<ByteBuf, GuestInputSyncPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> payload.write(buf), GuestInputSyncPayload::new);

    public GuestInputSyncPayload(ByteBuf buf) {
        this(
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readBoolean()
        );
    }

    public void write(ByteBuf buf) {
        buf.writeFloat(forward);
        buf.writeFloat(strafe);
        buf.writeBoolean(jumping);
        buf.writeBoolean(sprinting);
        buf.writeBoolean(sneaking);
        buf.writeFloat(headYaw);
        buf.writeFloat(headPitch);
        buf.writeBoolean(leftClick);
        buf.writeBoolean(rightClick);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
