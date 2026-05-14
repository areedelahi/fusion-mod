package com.github.fusion.world;

import com.github.fusion.FusionMod;
import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = FusionMod.MOD_ID)
public class FusionTickHandler {

    private static ItemStack[] lastHostInventory = null;
    private static ItemStack[] lastGuestInventory = null;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (server == null) return;

        FusionManager manager = FusionManager.get(server);
        for (FusionInstance fusion : manager.getAllFusions()) {
            ServerPlayer host = server.getPlayerList().getPlayer(fusion.getHost());
            ServerPlayer guest = server.getPlayerList().getPlayer(fusion.getGuest());
            if (host == null || guest == null) continue;

            if (host.isDeadOrDying() || guest.isDeadOrDying()) continue;

            ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(guest.getId());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player != guest) { 
                    player.connection.send(removePacket);
                }
            }

            if (guest.level() != host.level()) {
                guest.teleportTo((net.minecraft.server.level.ServerLevel) host.level(), host.getX(), host.getY(), host.getZ(), java.util.Set.of(), host.getYRot(), host.getXRot(), false);
            } else {
                guest.moveTo(host.getX(), host.getY(), host.getZ());
            }

            guest.setBoundingBox(new net.minecraft.world.phys.AABB(0, 0, 0, 0, 0, 0));

            guest.setHealth(host.getHealth());

            guest.getFoodData().setFoodLevel(host.getFoodData().getFoodLevel());
            guest.getFoodData().setSaturation(host.getFoodData().getSaturationLevel());

            guest.experienceLevel = host.experienceLevel;
            guest.experienceProgress = host.experienceProgress;
            guest.totalExperience = host.totalExperience;

            syncInventory(host, guest);
        }
    }

    private static void syncInventory(ServerPlayer host, ServerPlayer guest) {
        int size = host.getInventory().getContainerSize();

        if (lastHostInventory == null || lastHostInventory.length != size) {
            lastHostInventory = new ItemStack[size];
            lastGuestInventory = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                lastHostInventory[i] = host.getInventory().getItem(i).copy();
                lastGuestInventory[i] = guest.getInventory().getItem(i).copy();
            }
        }

        boolean hostInventoryChanged = false;
        for (int i = 0; i < size; i++) {
            ItemStack currentStack = host.getInventory().getItem(i);
            if (!ItemStack.matches(currentStack, lastHostInventory[i])) {
                hostInventoryChanged = true;
                lastHostInventory[i] = currentStack.copy();
            }
        }

        boolean guestInventoryChanged = false;
        for (int i = 0; i < size; i++) {
            ItemStack currentStack = guest.getInventory().getItem(i);
            if (!ItemStack.matches(currentStack, lastGuestInventory[i])) {
                guestInventoryChanged = true;
                lastGuestInventory[i] = currentStack.copy();
            }
        }

        if (hostInventoryChanged) {
            for (int i = 0; i < size; i++) {
                guest.getInventory().setItem(i, host.getInventory().getItem(i).copy());
            }
            for (int i = 0; i < size; i++) {
                lastGuestInventory[i] = guest.getInventory().getItem(i).copy();
            }
        } else if (guestInventoryChanged) {
            for (int i = 0; i < size; i++) {
                host.getInventory().setItem(i, guest.getInventory().getItem(i).copy());
            }
            for (int i = 0; i < size; i++) {
                lastHostInventory[i] = host.getInventory().getItem(i).copy();
            }
        }

        guest.getInventory().selected = host.getInventory().selected;

        host.inventoryMenu.broadcastChanges();
        guest.inventoryMenu.broadcastChanges();

        for (int i = 0; i < size; i++) {
            guest.connection.send(new ClientboundContainerSetSlotPacket(
                    -2, 0, i, host.getInventory().getItem(i).copy()));
        }

        guest.connection.send(new ClientboundSetHeldSlotPacket(host.getInventory().selected));
    }

    public static void resetTracking() {
        lastHostInventory = null;
        lastGuestInventory = null;
    }
}
