package com.github.fusion.interaction;

import com.github.fusion.FusionMod;
import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = FusionMod.MOD_ID)
public class ArmController {

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        FusionManager manager = FusionManager.get(player.serverLevel());
        FusionInstance instance = manager.getFusionForPlayer(player.getUUID());
        if (instance == null) return;

        if (instance.isHost(player.getUUID())) return;

        if (!instance.isHostOnline()) return;
        event.setCanceled(true);

        Entity target = event.getTarget();
        FusionMod.LOGGER.debug("Guest {} attack on {} suppressed (handled via ArmActionPayload)",
                player.getName().getString(), target.getName().getString());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        FusionManager manager = FusionManager.get(player.serverLevel());
        FusionInstance instance = manager.getFusionForPlayer(player.getUUID());
        if (instance == null) return;

        if (instance.isHost(player.getUUID())) return;

        if (!instance.isHostOnline()) return;
        event.setCanceled(true);
        FusionMod.LOGGER.debug("Guest {} item use suppressed", player.getName().getString());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        FusionManager manager = FusionManager.get(player.serverLevel());
        FusionInstance instance = manager.getFusionForPlayer(player.getUUID());
        if (instance == null) return;

        if (instance.isHost(player.getUUID())) return;

        if (!instance.isHostOnline()) return;
        event.setCanceled(true);
        FusionMod.LOGGER.debug("Guest {} block use at {} suppressed",
                player.getName().getString(), event.getPos());
    }
}
