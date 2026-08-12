package com.carlmod.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Records, per player, the dimension and block position they were standing at right
 * before the Big Mouth Staff pulled them into the Carl Dimension, so a second use of
 * the staff can send them back to that exact spot.
 * <p>
 * Backed by vanilla's {@link PersistentState} mechanism, so this data survives
 * server restarts (it is saved under the overworld's {@code data/} folder).
 */
public class CarlTeleportState extends PersistentState {

    private static final String STATE_ID = "carlmod_teleport_data";

    private final Map<UUID, ReturnPoint> returnPoints = new HashMap<>();

    public record ReturnPoint(RegistryKey<World> dimension, BlockPos pos) {
    }

    public static CarlTeleportState getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(CarlTeleportState::createFromNbt, CarlTeleportState::new, STATE_ID);
    }

    public void setReturnPoint(UUID playerUuid, RegistryKey<World> dimension, BlockPos pos) {
        returnPoints.put(playerUuid, new ReturnPoint(dimension, pos));
        markDirty();
    }

    public ReturnPoint getReturnPoint(UUID playerUuid) {
        return returnPoints.get(playerUuid);
    }

    public void clearReturnPoint(UUID playerUuid) {
        returnPoints.remove(playerUuid);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, ReturnPoint> entry : returnPoints.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putUuid("player", entry.getKey());
            entryNbt.putString("dimension", entry.getValue().dimension().getValue().toString());
            entryNbt.putInt("x", entry.getValue().pos().getX());
            entryNbt.putInt("y", entry.getValue().pos().getY());
            entryNbt.putInt("z", entry.getValue().pos().getZ());
            list.add(entryNbt);
        }
        nbt.put("returnPoints", list);
        return nbt;
    }

    public static CarlTeleportState createFromNbt(NbtCompound nbt) {
        CarlTeleportState state = new CarlTeleportState();
        NbtList list = nbt.getList("returnPoints", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entryNbt = list.getCompound(i);
            UUID player = entryNbt.getUuid("player");
            RegistryKey<World> dim = RegistryKey.of(RegistryKeys.WORLD,
                    new Identifier(entryNbt.getString("dimension")));
            BlockPos pos = new BlockPos(
                    entryNbt.getInt("x"), entryNbt.getInt("y"), entryNbt.getInt("z"));
            state.returnPoints.put(player, new ReturnPoint(dim, pos));
        }
        return state;
    }
}
