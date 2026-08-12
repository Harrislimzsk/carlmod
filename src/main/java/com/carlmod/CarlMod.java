package com.carlmod;

import com.carlmod.entity.ModEntities;
import com.carlmod.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarlMod implements ModInitializer {

    public static final String MOD_ID = "carlmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[CarlMod] Initializing Big Mouth Carl Mod...");

        ModItems.register();
        ModEntities.register();

        LOGGER.info("[CarlMod] Initialization complete.");
    }
}
