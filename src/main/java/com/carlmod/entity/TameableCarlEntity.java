package com.carlmod.entity;

import com.carlmod.dimension.ModDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ServerWorldAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Tameable Carl — placeholder.
 * <p>
 * For now this only inherits Wild Carl's slow movement, hatred immunity, and 20x20x20
 * erase aura unchanged, which is enough to wire up natural-spawn placement inside the
 * Carl Dimension in this stage. Taming via Heart of the Sea, owner/pet exclusion from
 * the erase aura, and melee counter-attack AI when struck are added in the next stage.
 */
public class TameableCarlEntity extends WildCarlEntity {

    public TameableCarlEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Natural-spawn gate: only allowed inside the Carl Dimension, never in the overworld
     * or any other dimension, regardless of which biome a future datapack might reuse
     * {@code carlmod:carl_biome} in.
     */
    public static boolean canTameableCarlSpawn(EntityType<TameableCarlEntity> type,
                                                ServerWorldAccess world, SpawnReason reason,
                                                BlockPos pos, Random random) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return false;
        }
        if (!serverWorld.getRegistryKey().equals(ModDimensions.CARL_DIMENSION_KEY)) {
            return false;
        }
        return world.getBlockState(pos.down()).allowsSpawning(world, pos.down(), type);
    }
}
