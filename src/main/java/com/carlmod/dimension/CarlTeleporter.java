package com.carlmod.dimension;

import com.carlmod.state.CarlTeleportState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Owns the concrete two-way teleport behaviour between wherever the player currently
 * is and the Carl Dimension: safe-landing platform generation, persisting the origin
 * point before departure, and restoring it on the way back.
 * <p>
 * {@code BigMouthStaffItem} only decides *when* to call these (right-click Carl /
 * right-click air while inside the dimension); all of the actual mechanics live here
 * so they can be reused later (e.g. a future in-dimension "exit" block or NPC).
 */
public final class CarlTeleporter {

    /** Fixed landing point inside the Carl Dimension. */
    public static final BlockPos CARL_DIMENSION_SPAWN = new BlockPos(0, 100, 0);

    private CarlTeleporter() {
    }

    /**
     * Sends the player into the Carl Dimension, recording where they came from so
     * {@link #teleportBack} can send them home later.
     */
    public static void teleportToCarlDimension(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        ServerWorld carlWorld = server.getWorld(ModDimensions.CARL_DIMENSION_KEY);
        if (carlWorld == null) {
            player.sendMessage(Text.literal(
                    "The Carl Dimension could not be loaded on this server."), false);
            return;
        }

        // Guard against overwriting an existing return point if the player somehow
        // uses the staff again while already inside the Carl Dimension.
        if (!player.getWorld().getRegistryKey().equals(ModDimensions.CARL_DIMENSION_KEY)) {
            CarlTeleportState teleportState = CarlTeleportState.getServerState(server);
            teleportState.setReturnPoint(player.getUuid(), player.getWorld().getRegistryKey(),
                    player.getBlockPos());
        }

        generateBedrockPlatform(carlWorld, CARL_DIMENSION_SPAWN);

        player.teleport(carlWorld,
                CARL_DIMENSION_SPAWN.getX() + 0.5,
                CARL_DIMENSION_SPAWN.getY() + 1,
                CARL_DIMENSION_SPAWN.getZ() + 0.5,
                player.getYaw(), player.getPitch());

        playTeleportSound(carlWorld, CARL_DIMENSION_SPAWN);
    }

    /**
     * Sends the player back to the exact dimension and coordinates recorded before
     * they entered the Carl Dimension, then clears that record.
     */
    public static void teleportBack(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        CarlTeleportState teleportState = CarlTeleportState.getServerState(server);
        CarlTeleportState.ReturnPoint returnPoint = teleportState.getReturnPoint(player.getUuid());
        if (returnPoint == null) {
            player.sendMessage(Text.literal(
                    "No return point recorded — visit Carl in the overworld first."), false);
            return;
        }

        ServerWorld returnWorld = server.getWorld(returnPoint.dimension());
        if (returnWorld == null) {
            // The origin dimension no longer exists (e.g. a datapack was removed).
            player.sendMessage(Text.literal(
                    "Your origin dimension is no longer available."), false);
            return;
        }

        BlockPos pos = returnPoint.pos();

        // Safety net only — does nothing if the original terrain is still intact.
        ensureSafeLanding(returnWorld, pos);

        player.teleport(returnWorld,
                pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                player.getYaw(), player.getPitch());

        teleportState.clearReturnPoint(player.getUuid());

        playTeleportSound(returnWorld, pos);
    }

    /**
     * Carves a flat 3x3 bedrock platform centered on {@code landingPos}, with two
     * blocks of headroom cleared above it, so arrivals never fall into the void or
     * suffocate.
     */
    public static void generateBedrockPlatform(ServerWorld world, BlockPos landingPos) {
        BlockPos floor = landingPos.down();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos platformPos = floor.add(dx, 0, dz);
                world.setBlockState(platformPos, Blocks.BEDROCK.getDefaultState());
                world.setBlockState(platformPos.up(1), Blocks.AIR.getDefaultState());
                world.setBlockState(platformPos.up(2), Blocks.AIR.getDefaultState());
            }
        }
    }

    /**
     * Fills in a single bedrock block under {@code pos} only if the ground there has
     * since become air (terrain changed, block broken, etc. while the player was away).
     * Leaves intact terrain completely untouched.
     */
    private static void ensureSafeLanding(ServerWorld world, BlockPos pos) {
        BlockPos below = pos.down();
        if (world.getBlockState(below).isAir()) {
            world.setBlockState(below, Blocks.BEDROCK.getDefaultState());
        }
    }

    private static void playTeleportSound(ServerWorld world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
