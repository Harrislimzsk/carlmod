package com.carlmod.entity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ServerWorldAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wild Carl.
 * <p>
 * - Extremely slow (0.1 movement speed), never targeted by any other mob's AI.
 * - Every 10 ticks, instantly discards every {@link LivingEntity} in a 20x20x20 box
 *   centered on itself, except players, non-living entities, and any other Carl-type
 *   entity. Each erased entity drops one randomly enchanted piece of equipment or an
 *   enchanted book (enchantment level 15-30).
 */
public class WildCarlEntity extends PathAwareEntity implements CarlEntityMarker {

    private static final int ERASE_INTERVAL_TICKS = 10;
    /** Expanding the bounding box by 10 in every direction yields the required 20x20x20 volume. */
    private static final double ERASE_RADIUS = 10.0;
    private static final int MIN_ENCHANT_LEVEL = 15;
    private static final int MAX_ENCHANT_LEVEL = 30;

    public WildCarlEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createWildCarlAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new WanderAroundGoal(this, 1.0));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
        // Deliberately no target-selection goals registered: Wild Carl never
        // initiates attacks against anything.
    }

    /**
     * Hatred immunity.
     * <p>
     * Vanilla target-selection goals (ActiveTargetGoal, RevengeGoal, NearestAttackableTargetGoal,
     * etc.) all route through {@code TargetPredicate.test(...)}, which checks the candidate
     * target's {@link #isAttackable()} before allowing another mob to lock onto it. Returning
     * {@code false} here means no other mob's AI — vanilla or modded, hostile or neutral — will
     * ever be able to select Wild Carl as an attack target, without needing to touch every other
     * mob's goal list individually.
     */
    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.age % ERASE_INTERVAL_TICKS == 0) {
            eraseNearbyLivingEntities();
        }
    }

    /**
     * Scans a 20x20x20 box centered on this entity (throttled to once every 10 ticks to avoid
     * a full-area scan every single tick) and instantly discards every valid target found.
     */
    protected void eraseNearbyLivingEntities() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Box eraseBox = this.getBoundingBox().expand(ERASE_RADIUS);
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(
                LivingEntity.class, eraseBox, this::canBeErased);

        for (LivingEntity target : targets) {
            Vec3d dropPos = target.getPos();
            target.discard(); // Instant removal — no hurt sound, no death animation/particles.
            dropRandomEnchantedLoot(serverWorld, dropPos);
        }
    }

    /**
     * Determines whether {@code target} should be erased.
     * <p>
     * Excludes: players, non-living entities (already excluded by the {@link LivingEntity}
     * class filter used in the query — armor stands and minecarts are not LivingEntity), and
     * any Carl-type entity (itself, other Wild Carls, Tameable Carls).
     * <p>
     * {@code TameableCarlEntity} overrides this to additionally exclude its owner and the
     * owner's other tamed pets.
     */
    protected boolean canBeErased(LivingEntity target) {
        if (target == this) return false;
        if (target instanceof PlayerEntity) return false;
        if (target instanceof CarlEntityMarker) return false;
        return target.isAlive();
    }

    /**
     * Spawns one randomly enchanted piece of equipment, or an enchanted book, at {@code pos}.
     * Enchantment power level is randomized between {@value #MIN_ENCHANT_LEVEL} and
     * {@value #MAX_ENCHANT_LEVEL} inclusive.
     */
    private void dropRandomEnchantedLoot(ServerWorld world, Vec3d pos) {
        ItemStack lootBase = pickRandomLootBase();
        int level = MIN_ENCHANT_LEVEL + this.random.nextInt(MAX_ENCHANT_LEVEL - MIN_ENCHANT_LEVEL + 1);

        List<EnchantmentLevelEntry> generated =
                EnchantmentHelper.generateEnchantments(this.random, lootBase, level, false);

        Map<Enchantment, Integer> enchantments = generated.stream()
                .collect(Collectors.toMap(e -> e.enchantment, e -> e.level, Math::max));

        ItemStack resultStack;
        if (lootBase.isOf(Items.BOOK)) {
            resultStack = new ItemStack(Items.ENCHANTED_BOOK);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                EnchantedBookItem.addEnchantment(resultStack,
                        new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
            }
        } else {
            resultStack = lootBase;
            EnchantmentHelper.set(enchantments, resultStack);
        }

        ItemEntity itemEntity = new ItemEntity(world, pos.x, pos.y, pos.z, resultStack);
        world.spawnEntity(itemEntity);
    }

    private ItemStack pickRandomLootBase() {
        ItemStack[] pool = new ItemStack[] {
                new ItemStack(Items.DIAMOND_SWORD),
                new ItemStack(Items.DIAMOND_PICKAXE),
                new ItemStack(Items.DIAMOND_AXE),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS),
                new ItemStack(Items.BOW),
                new ItemStack(Items.CROSSBOW),
                new ItemStack(Items.TRIDENT),
                new ItemStack(Items.BOOK) // Resolves to an ENCHANTED_BOOK in dropRandomEnchantedLoot.
        };
        return pool[this.random.nextInt(pool.length)].copy();
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        // Wild Carl is a designed encounter, not ambient wildlife — never auto-despawn it.
        return false;
    }

    /**
     * Half-width (in blocks) of the horizontal square used to enforce the "at most 1 per
     * area" natural-spawn cap. Vanilla has no built-in per-species area cap (only broad
     * per-category caps like "creature"/"monster"), so this is enforced manually: a spawn
     * attempt is rejected outright if another Wild Carl already exists anywhere within this
     * square, centered on the candidate spawn position.
     */
    private static final double AREA_CAP_HALF_WIDTH = 64.0;

    /**
     * Natural-spawn gate used by {@code SpawnRestriction.register} in {@code ModEntities}.
     * Enforces: solid ground beneath the spawn position, and the "region cap of 1" rule
     * (spawn weight and Plains-biome restriction are handled separately via
     * {@code BiomeModifications.addSpawn}).
     */
    public static boolean canWildCarlSpawn(EntityType<WildCarlEntity> type, ServerWorldAccess world,
                                            SpawnReason reason, BlockPos pos, Random random) {
        if (!world.getBlockState(pos.down()).allowsSpawning(world, pos.down(), type)) {
            return false;
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return false;
        }

        Box areaCapBox = new Box(
                pos.getX() - AREA_CAP_HALF_WIDTH, serverWorld.getBottomY(), pos.getZ() - AREA_CAP_HALF_WIDTH,
                pos.getX() + AREA_CAP_HALF_WIDTH, serverWorld.getTopY(), pos.getZ() + AREA_CAP_HALF_WIDTH);

        return serverWorld.getEntitiesByClass(WildCarlEntity.class, areaCapBox, e -> true).isEmpty();
    }
}
