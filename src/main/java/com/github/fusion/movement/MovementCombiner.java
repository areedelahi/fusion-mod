package com.github.fusion.movement;

import com.github.fusion.data.ControlMode;
import com.github.fusion.entity.InputState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class MovementCombiner {

    private MovementCombiner() {} 

    public static Vec3 combine(InputState a, InputState b, ControlMode mode,
                                @Nullable UUID dominant, UUID playerA) {
        return switch (mode) {
            case SHARED -> {

                float fwd = (a.forward() + b.forward()) / 2f;
                float str = (a.strafe() + b.strafe()) / 2f;
                yield new Vec3(str, 0, fwd);
            }
            case PASSENGER -> {

                InputState driver = isDominantA(dominant, playerA) ? a : b;
                yield new Vec3(driver.strafe(), 0, driver.forward());
            }
            case OVERRIDE -> {

                InputState dom = isDominantA(dominant, playerA) ? a : b;
                InputState sub = isDominantA(dominant, playerA) ? b : a;
                float fwd = dom.forward() != 0 ? dom.forward() : sub.forward();
                float str = dom.strafe() != 0 ? dom.strafe() : sub.strafe();
                yield new Vec3(str, 0, fwd);
            }
        };
    }

    public static boolean shouldJump(InputState a, InputState b, ControlMode mode,
                                      @Nullable UUID dominant, UUID playerA) {
        return switch (mode) {
            case SHARED -> a.jumping() || b.jumping();
            case PASSENGER -> {
                InputState driver = isDominantA(dominant, playerA) ? a : b;
                yield driver.jumping();
            }
            case OVERRIDE -> {
                InputState dom = isDominantA(dominant, playerA) ? a : b;
                InputState sub = isDominantA(dominant, playerA) ? b : a;
                yield dom.jumping() || sub.jumping();
            }
        };
    }

    public static boolean shouldSneak(InputState a, InputState b, ControlMode mode,
                                       @Nullable UUID dominant, UUID playerA) {
        return switch (mode) {
            case SHARED -> a.sneaking() || b.sneaking();
            case PASSENGER -> {
                InputState driver = isDominantA(dominant, playerA) ? a : b;
                yield driver.sneaking();
            }
            case OVERRIDE -> {
                InputState dom = isDominantA(dominant, playerA) ? a : b;
                InputState sub = isDominantA(dominant, playerA) ? b : a;
                yield dom.sneaking() || sub.sneaking();
            }
        };
    }

    public static boolean shouldSprint(InputState a, InputState b, ControlMode mode,
                                        @Nullable UUID dominant, UUID playerA) {
        return switch (mode) {
            case SHARED -> a.sprinting() && b.sprinting();
            case PASSENGER -> {
                InputState driver = isDominantA(dominant, playerA) ? a : b;
                yield driver.sprinting();
            }
            case OVERRIDE -> {
                InputState dom = isDominantA(dominant, playerA) ? a : b;
                yield dom.sprinting();
            }
        };
    }

    public static float combineBodyYaw(InputState a, InputState b, ControlMode mode,
                                        @Nullable UUID dominant, UUID playerA) {
        return switch (mode) {
            case SHARED -> averageAngle(a.headYaw(), b.headYaw());
            case PASSENGER, OVERRIDE -> {
                InputState driver = isDominantA(dominant, playerA) ? a : b;
                yield driver.headYaw();
            }
        };
    }

    private static boolean isDominantA(@Nullable UUID dominant, UUID playerA) {
        return dominant != null && dominant.equals(playerA);
    }

    private static float averageAngle(float a, float b) {
        double ax = Math.cos(Math.toRadians(a));
        double ay = Math.sin(Math.toRadians(a));
        double bx = Math.cos(Math.toRadians(b));
        double by = Math.sin(Math.toRadians(b));
        return (float) Math.toDegrees(Math.atan2((ay + by) / 2, (ax + bx) / 2));
    }
}
