package com.carlmod.item;

import com.carlmod.dimension.CarlTeleporter;
import com.carlmod.dimension.ModDimensions;
import com.carlmod.entity.CarlEntityMarker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Big Mouth Staff.
 * <p>
 * - Right-click any {@link CarlEntityMarker} entity -> teleport into the Carl Dimension
 *   (see {@link CarlTeleporter#teleportToCarlDimension}).
 * - Right-click empty air while already inside the Carl Dimension -> teleport back to
 *   the remembered origin (see {@link CarlTeleporter#teleportBack}).
 * <p>
 * All of the actual teleport mechanics (platform generation, origin-point bookkeeping)
 * live in {@link CarlTeleporter}; this class only decides when to invoke them.
 */
public class BigMouthStaffItem extends Item {

    public BigMouthStaffItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(entity instanceof CarlEntityMarker)) {
            return ActionResult.PASS;
        }
        if (!(user instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        CarlTeleporter.teleportToCarlDimension(serverPlayer);
        return ActionResult.CONSUME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer
                && world.getRegistryKey().equals(ModDimensions.CARL_DIMENSION_KEY)) {
            CarlTeleporter.teleportBack(serverPlayer);
            return TypedActionResult.consume(stack);
        }

        return TypedActionResult.pass(stack);
    }
}
