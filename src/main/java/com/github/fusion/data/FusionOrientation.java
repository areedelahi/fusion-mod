package com.github.fusion.data;

import net.minecraft.util.StringRepresentable;

public enum FusionOrientation implements StringRepresentable {
    SIDE_BY_SIDE("side_by_side"),
    BACK_TO_BACK("back_to_back");

    private final String serializedName;

    FusionOrientation(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static FusionOrientation byName(String name) {
        for (FusionOrientation o : values()) {
            if (o.serializedName.equalsIgnoreCase(name)) {
                return o;
            }
        }
        return SIDE_BY_SIDE;
    }
}
