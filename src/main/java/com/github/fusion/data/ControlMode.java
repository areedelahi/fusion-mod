package com.github.fusion.data;

import net.minecraft.util.StringRepresentable;

public enum ControlMode implements StringRepresentable {
    SHARED("shared"),
    PASSENGER("passenger"),
    OVERRIDE("override");

    private final String serializedName;

    ControlMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static ControlMode byName(String name) {
        for (ControlMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return SHARED;
    }
}
