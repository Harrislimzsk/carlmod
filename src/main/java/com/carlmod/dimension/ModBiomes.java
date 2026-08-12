package com.carlmod.dimension;

import com.carlmod.CarlMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

/**
 * The Carl Dimension's terrain is defined as data ({@code data/carlmod/worldgen/biome/carl_biome.json})
 * and used as the fixed biome for {@code data/carlmod/dimension/carl_dimension.json}'s flat generator.
 * This key exists purely so Java code (spawn placement, biome modifications) can reference that
 * biome without hard-coding string identifiers everywhere.
 */
public class ModBiomes {

    public static final RegistryKey<Biome> CARL_BIOME = RegistryKey.of(
            RegistryKeys.BIOME, new Identifier(CarlMod.MOD_ID, "carl_biome"));
}
