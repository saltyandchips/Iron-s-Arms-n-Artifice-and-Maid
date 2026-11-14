package cn.blockforge.generated.maidartificearms.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GunCombatMoveBehavior {

    private static final double SCAN_RADIUS = 18.0D;

    private static final int SCAN_INTERVAL = 20;

    private static final int REPATH_INTERVAL = 20;

    private static final int MAX_MARKS = 32;

    private static final float WANDER_SPEED = 1.5F;

    private static final float AIM_SLOW_SPEED = 0.5F;

    private static final Map<UUID, BlockPos> WANDER_POINT = new HashMap<>();
    private static final Map<UUID, Long> NEXT_REPATH = new HashMap<>();
    private static final Map<UUID, MarkCache> MARKS = new HashMap<>();

    private GunCombatMoveBehavior() {}

    public static BehaviorControl<EntityMaid> create() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.present(MemoryModuleType.ATTACK_TARGET)
        ).apply(instance, (walkTarget, attackTarget) ->
                (level, maid, gameTime) -> tick(level, maid, gameTime)));
    }

    private static boolean tick(ServerLevel level, EntityMaid maid, long gameTime) {
        boolean holding = IaaGunAttackTask.holdingGun(maid);
        boolean hasTarget = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
        DebugLog.info("move:" + maid.getUUID(), 20,
                "move tick holding={} target={} task={}", holding, hasTarget, maid.getTask().getUid());
        if (!holding) {
            clear(maid);
            return false;
        }
        if (!hasTarget) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            clear(maid);
            return false;
        }

        BlockPos point = WANDER_POINT.get(maid.getUUID());
        long next = NEXT_REPATH.getOrDefault(maid.getUUID(), 0L);
        if (point == null || gameTime >= next) {
            List<Vec3> marks = markedEnemies(level, maid, gameTime);
            point = pickWanderPoint(level, maid, marks);
            DebugLog.info("move:point:" + maid.getUUID(), 20,
                    "repath marks={} point={}", marks.size(), point);
            if (point == null) {
                DebugLog.info("move:none:" + maid.getUUID(), 20,
                        "repath produced null point (no enemies), stop");
                maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                return true;
            }
            WANDER_POINT.put(maid.getUUID(), point);
            NEXT_REPATH.put(maid.getUUID(), gameTime + REPATH_INTERVAL);
            prune(gameTime);
        }

        float speed = IaaGunShootBehavior.isAimSlow(maid, gameTime) ? AIM_SLOW_SPEED : WANDER_SPEED;
        DebugLog.info("move:speed:" + maid.getUUID(), 20,
                "walk speed={} slow={} point={}", speed, IaaGunShootBehavior.isAimSlow(maid, gameTime), point);
        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(new BlockPosTracker(point), speed, 0));
        return true;
    }

    private static List<Vec3> markedEnemies(ServerLevel level, EntityMaid maid, long gameTime) {
        MarkCache cache = MARKS.computeIfAbsent(maid.getUUID(), k -> new MarkCache());
        if (gameTime - cache.refreshedAt >= SCAN_INTERVAL) {
            double radius = Math.max(SCAN_RADIUS, IaaGunAttackTask.shootRange(maid));
            List<LivingEntity> raw = level.getEntitiesOfClass(LivingEntity.class,
                    maid.getBoundingBox().inflate(radius),
                    e -> e != maid && e.isAlive() && !e.isRemoved());
            List<LivingEntity> found = new ArrayList<>(raw.size());
            for (LivingEntity e : raw) {
                if (maid.canAttack(e)) {
                    found.add(e);
                }
            }
            DebugLog.info("marks:" + maid.getUUID(), 20,
                    "scan raw={} attackable={} radius={}", raw.size(), found.size(), radius);
            
            found.sort(Comparator.comparingDouble(e -> e.distanceToSqr(maid)));
            cache.marks.clear();
            int limit = Math.min(found.size(), MAX_MARKS);
            for (int i = 0; i < limit; i++) {
                LivingEntity e = found.get(i);
                cache.marks.add(new Mark(new WeakReference<>(e), e.position()));
            }
            cache.refreshedAt = gameTime;
        } else {
            cache.marks.removeIf(mark -> {
                LivingEntity e = mark.ref.get();
                return e == null || !e.isAlive() || e.isRemoved();
            });
        }
        List<Vec3> positions = new ArrayList<>(cache.marks.size());
        for (Mark mark : cache.marks) {
            positions.add(mark.pos);
        }
        return positions;
    }

    private static BlockPos pickWanderPoint(ServerLevel level, EntityMaid maid, List<Vec3> enemies) {
        Vec3 pos = maid.position();
        
        LivingEntity target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            double dist = Math.sqrt(maid.distanceToSqr(target));
            boolean visible = maid.canSee(target);
            double engage = Math.min(12.0D, IaaGunAttackTask.shootRange(maid) * 0.55D);
            if (!visible || dist > engage * 1.5D) {
                Vec3 d = target.position().subtract(pos);
                if (d.lengthSqr() > 1.0E-6D) {
                    Vec3 dir = new Vec3(d.x, 0.0D, d.z).normalize();
                    double step = Math.min(Math.max(engage, 4.0D), Math.max(2.0D, dist - 2.0D));
                    DebugLog.info("move:approach:" + maid.getUUID(), 20,
                            "approach target dist={} visible={} engage={} step={}", dist, visible, engage, step);
                    return BlockPos.containing(pos.x + dir.x * step, pos.y, pos.z + dir.z * step);
                }
            }
        }
        
        if (enemies.isEmpty()) {
            return null;
        }
        Vec3 away = Vec3.ZERO;
        double nearest = Double.MAX_VALUE;
        for (Vec3 enemy : enemies) {
            Vec3 delta = pos.subtract(enemy);
            double dist = Math.max(0.75D, delta.length());
            nearest = Math.min(nearest, dist);
            away = away.add(delta.normalize().scale(1.0D / (dist * dist)));
        }
        if (away.lengthSqr() < 1.0E-6D) {
            return null;
        }
        away = away.normalize();
        Vec3 side = new Vec3(-away.z, 0.0D, away.x).normalize();
        double sign = maid.getRandom().nextBoolean() ? 1.0D : -1.0D;
        double radius = nearest < 8.0D ? 8.0D : 5.0D;
        BlockPos point = wanderPoint(pos, away, side, sign, radius);
        if (!isCliff(level, maid, point)) {
            return point;
        }
        
        BlockPos alt = wanderPoint(pos, away, side, -sign, radius);
        if (!isCliff(level, maid, alt)) {
            return alt;
        }
        DebugLog.info("move:cliff:" + maid.getUUID(), 20,
                "retreat blocked by cliff drop > 2, hold position");
        return null;
    }

    private static BlockPos wanderPoint(Vec3 pos, Vec3 away, Vec3 side, double sign, double radius) {
        Vec3 dir = away.scale(0.75D).add(side.scale(0.25D * sign)).normalize();
        return BlockPos.containing(pos.x + dir.x * radius, pos.y, pos.z + dir.z * radius);
    }

    private static boolean isCliff(ServerLevel level, EntityMaid maid, BlockPos point) {
        int ground = groundY(level, point);
        return ground < maid.getY() - 2.0D;
    }

    private static int groundY(ServerLevel level, BlockPos pos) {
        int bottom = pos.getY() - 10;
        for (int y = pos.getY() + 3; y >= bottom; y--) {
            BlockPos probe = new BlockPos(pos.getX(), y, pos.getZ());
            if (level.getBlockState(probe).blocksMotion()) {
                return y + 1;
            }
        }
        return bottom;
    }

    private static void clear(EntityMaid maid) {
        WANDER_POINT.remove(maid.getUUID());
        NEXT_REPATH.remove(maid.getUUID());
        MARKS.remove(maid.getUUID());
    }

    private static void prune(long gameTime) {
        if (MARKS.size() > 256) {
            MARKS.values().removeIf(cache -> gameTime - cache.refreshedAt > 200L);
        }
        if (WANDER_POINT.size() > 256) {
            WANDER_POINT.clear();
            NEXT_REPATH.clear();
        }
    }

    private static final class Mark {
        private final WeakReference<LivingEntity> ref;
        private final Vec3 pos;

        private Mark(WeakReference<LivingEntity> ref, Vec3 pos) {
            this.ref = ref;
            this.pos = pos;
        }
    }

    private static final class MarkCache {
        private final List<Mark> marks = new ArrayList<>();
        private long refreshedAt = Long.MIN_VALUE;
    }
}
