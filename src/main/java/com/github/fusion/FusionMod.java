package com.github.fusion;

import com.github.fusion.core.ModRegistry;
import com.github.fusion.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FusionMod.MOD_ID)
public class FusionMod {
    public static final String MOD_ID = "fusion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public FusionMod(IEventBus modEventBus) {

        ModRegistry.register(modEventBus);

        modEventBus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.class, NetworkHandler::onRegisterPayloads);
    }
}
