package com.carlmod.entity;

import com.carlmod.CarlMod;
import com.carlmod.dimension.ModBiomes;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public class ModEntities {

    public static final EntityType<WildCarlEntity> WILD_CARL = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(CarlMod.MOD_ID, "wild_carl"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WildCarlEntity::new)
                    .dimensions(EntityDimensions.fixed(0.9f, 1.6f))
                    .trackRangeChunks(10)
                    .build()
    );

    public static final EntityType<TameableCarlEntity> TAMEABLE_CARL = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(CarlMod.MOD_ID, "tameable_carl"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TameableCarlEntity::new)
                    .dimensions(EntityDimensions.fixed(0.9f, 1.6f))
                    .trackRangeChunks(10)
                    .build()
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(WILD_CARL, WildCarlEntity.createWildCarlAttributes());
        FabricDefaultAttributeRegistry.register(TAMEABLE_CARL, WildCarlEntity.createWildCarlAttributes());

        registerSpawnPlacement();

        CarlMod.LOGGER.info("[CarlMod] Entities registered.");
    }

    /**
     * Spawn placement rules (requirement 5):
     * <ul>
     *     <li>Wild Carl — overworld Plains surface only, spawn weight 1, group size 1-1,
     *         plus a manual "at most 1 per ~128x128 area" cap enforced in
     *         {@link WildCarlEntity#canWildCarlSpawn}.</li>
     *     <li>Tameable Carl — Carl Dimension only (never the overworld), spawn weight 10,
     *         group size 1-2.</li>
     * </ul>
     * {@code SpawnRestriction.register} controls *where on the terrain* a candidate position
     * has to look like (ground level, light, custom predicates); {@code BiomeModifications.addSpawn}
     * controls *which biome* the species is even considered in, plus weight and group size.
     */
    private static void registerSpawnPlacement() {
        SpawnRestriction.register(WILD_CARL, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, WildCarlEntity::canWildCarlSpawn);

        SpawnRestriction.register(TAMEABLE_CARL, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, TameableCarlEntity::canTameableCarlSpawn);

        // Wild Carl: overworld Plains biome only, weight 1, exactly 1 per spawn attempt.
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld().and(BiomeSelectors.includeByKey(BiomeKeys.PLAINS)),
                SpawnGroup.CREATURE,
                WILD_CARL,
                1,  // weight
                1,  // min group size
                1   // max group size
        );

        // Tameable Carl: only the Carl Dimension's dedicated biome, weight 10, 1-2 per spawn attempt.
        // (canTameableCarlSpawn above is an additional hard gate in case carl_biome is ever
        // reused by another dimension in a future datapack.)
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(ModBiomes.CARL_BIOME),
                SpawnGroup.CREATURE,
                TAMEABLE_CARL,
                10, // weight
                1,  // min group size
                2   // max group size
        );
    }
}
