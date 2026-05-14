package com.github.fusion.entity;

public record InputState(
        float forward,
        float strafe,
        boolean jumping,
        boolean sprinting,
        boolean sneaking,
        float headYaw,
        float headPitch
) {

    public static final InputState ZERO = new InputState(0f, 0f, false, false, false, 0f, 0f);

    public boolean hasMovement() {
        return forward != 0f || strafe != 0f;
    }

    public boolean hasAction() {
        return jumping || sprinting || sneaking;
    }
}
