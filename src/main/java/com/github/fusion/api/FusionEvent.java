package com.github.fusion.api;

import com.github.fusion.data.ControlMode;
import com.github.fusion.data.FusionInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

public abstract class FusionEvent extends Event {

    private final FusionInstance fusionInstance;

    protected FusionEvent(FusionInstance fusionInstance) {
        this.fusionInstance = fusionInstance;
    }

    public FusionInstance getFusionInstance() {
        return fusionInstance;
    }

    public static class Pre extends FusionEvent implements ICancellableEvent {
        private final ServerPlayer playerA;
        private final ServerPlayer playerB;

        public Pre(FusionInstance instance, ServerPlayer playerA, ServerPlayer playerB) {
            super(instance);
            this.playerA = playerA;
            this.playerB = playerB;
        }

        public ServerPlayer getPlayerA() { return playerA; }
        public ServerPlayer getPlayerB() { return playerB; }
    }

    public static class Post extends FusionEvent {
        public Post(FusionInstance instance) {
            super(instance);
        }
    }

    public static class Unfuse extends FusionEvent implements ICancellableEvent {
        public Unfuse(FusionInstance instance) {
            super(instance);
        }
    }

    public static class ControlModeChanged extends FusionEvent {
        private final ControlMode oldMode;
        private final ControlMode newMode;

        public ControlModeChanged(FusionInstance instance, ControlMode oldMode, ControlMode newMode) {
            super(instance);
            this.oldMode = oldMode;
            this.newMode = newMode;
        }

        public ControlMode getOldMode() { return oldMode; }
        public ControlMode getNewMode() { return newMode; }
    }

    public static class InputProcessed extends FusionEvent {
        private Vec3 combinedMovement;
        private boolean jump;
        private boolean sprint;
        private boolean sneak;

        public InputProcessed(FusionInstance instance, Vec3 combinedMovement,
                               boolean jump, boolean sprint, boolean sneak) {
            super(instance);
            this.combinedMovement = combinedMovement;
            this.jump = jump;
            this.sprint = sprint;
            this.sneak = sneak;
        }

        public Vec3 getCombinedMovement() { return combinedMovement; }
        public void setCombinedMovement(Vec3 movement) { this.combinedMovement = movement; }

        public boolean isJump() { return jump; }
        public void setJump(boolean jump) { this.jump = jump; }

        public boolean isSprint() { return sprint; }
        public void setSprint(boolean sprint) { this.sprint = sprint; }

        public boolean isSneak() { return sneak; }
        public void setSneak(boolean sneak) { this.sneak = sneak; }
    }
}
