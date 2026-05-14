package com.github.fusion.core;

import com.github.fusion.FusionMod;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistry {

    private ModRegistry() {} 

    public static void register(IEventBus modEventBus) {

        FusionMod.LOGGER.debug("Registered mod objects");
    }
}
