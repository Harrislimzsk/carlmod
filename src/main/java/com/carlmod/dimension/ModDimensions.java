package com.carlmod.dimension;

import com.carlmod.CarlMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * The Carl Dimension is defined as data ({@code data/carlmod/dimension_type/carl_dimension_type.json}
 * and {@code data/carlmod/dimension/carl_dimension.json}), which Fabric loads as part of the
 * mod's built-in resources at world creation — no Java registration call is needed for the
 * dimension itself. This class only exposes the resulting {@link RegistryKey} so other code
 * (the staff, {@code CarlTeleporter}, spawn placement rules) can reference it type-safely.
 */
public class ModDimensions {

    public static final RegistryKey<World> CARL_DIMENSION_KEY = RegistryKey.of(
            RegistryKeys.WORLD,
            new Identifier(CarlMod.MOD_ID, "carl_dimension")
    );
}
