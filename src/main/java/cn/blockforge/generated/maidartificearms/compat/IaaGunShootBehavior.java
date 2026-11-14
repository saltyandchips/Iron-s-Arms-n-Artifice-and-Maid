package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.ReloadState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class IaaGunShootBehavior extends Behavior<EntityMaid> {

    private int attackCooldown = -1;
    private int seeTime = 0;
    
    private long reloadDeadline = -1;

    private static final Map<UUID, Long> AIM_SLOW_UNTIL = new HashMap<>();

    static boolean isAimSlow(EntityMaid maid, long gameTime) {
        Long until = AIM_SLOW_UNTIL.get(maid.getUUID());
        return until != null && until >= gameTime;
    }

    public IaaGunShootBehavior() {

        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED), 1200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!IaaGunAttackTask.canAttackWithGun(maid)) {
            return false;
        }
        if (SoulCoinSeeker.findNearestCoin(maid) != null) {
            return true;
        }
        return maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(maid::canSee).isPresent();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        if (!IaaGunAttackTask.holdingGun(maid)) {
            return false;
        }
        return maid.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                || SoulCoinSeeker.findNearestCoin(maid) != null;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        
        Entity coin = SoulCoinSeeker.findNearestCoin(maid);
        if (coin != null) {
            MaidGunAimBehavior.applyAim(maid, true);
            double aimY = SoulCoinSeeker.aimHeight(coin);
            maid.getLookControl().setLookAt(coin.getX(), aimY, coin.getZ(), 30.0F, 30.0F);
            this.seeTime = Math.max(this.seeTime, 1);
            handleGunCycle(maid, level, gameTime, new Vec3(coin.getX(), aimY, coin.getZ()), true);
            return;
        }

        Optional<LivingEntity> optionalTarget = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (optionalTarget.isEmpty()) {
            return;
        }
        LivingEntity target = optionalTarget.get();

        maid.getLookControl().setLookAt(target, 30.0F, 30.0F);
        faceTarget(maid, target);

        boolean canSee = maid.canSee(target);
        if (canSee != (this.seeTime > 0)) {
            this.seeTime = 0;
        }
        if (canSee) {
            ++this.seeTime;
        } else {
            --this.seeTime;
        }

        handleGunCycle(maid, level, gameTime, target.getEyePosition(), canSee && this.seeTime > -60);
    }

    private static void faceTarget(EntityMaid maid, LivingEntity target) {
        
        maid.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (maid.getNavigation().isDone()) {
            maid.lookAt(target, 30.0F, 30.0F);
            maid.yBodyRot = maid.getYRot();
            maid.yHeadRot = maid.getYRot();
        } else {
            maid.yBodyRot = maid.yHeadRot;
        }
    }

    private void handleGunCycle(EntityMaid maid, ServerLevel level, long gameTime,
                                Vec3 aimPoint, boolean lineOfFire) {
        ItemStack gun = maid.getMainHandItem();
        if (!(gun.getItem() instanceof GunItem gunItem)) {
            return;
        }
        DebugLog.info("gun:" + maid.getUUID(), 20,
                "gun cycle reload={} mag={} cooldown={} lineOfFire={}",
                GunItem.isReloading(gun), GunItem.getMagazine(gun).count(), this.attackCooldown, lineOfFire);

        if (GunItem.isReloading(gun)) {
            if (this.reloadDeadline >= 0 && gameTime > this.reloadDeadline) {
                GunplayManager.attemptFinishReload(maid, gun, ReloadState.get(gun).roundsToLoad());
                this.reloadDeadline = -1;
            }
            return;
        }
        this.reloadDeadline = -1;

        int magazine = GunItem.getMagazine(gun).count();

        if (magazine <= 0) {
            boolean hasReserve = !CompatConfig.CONSUME_AMMO.get() || MaidAmmoUtil.countAmmo(maid) > 0;
            if (!hasReserve) {
                
                maid.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                this.attackCooldown = 20;
                return;
            }
            ReloadResult result = GunplayManager.attemptStartReload(maid, gun);
            if (result == ReloadResult.STARTING_RELOAD || result == ReloadResult.FINISHED_RELOAD) {
                int natural = gunItem.getGun().reloadTimeTicks();
                int cap = CompatConfig.RELOAD_MAX_TICKS.get();
                int effective = cap > 0 ? Math.min(natural, cap) : natural;
                this.reloadDeadline = cap > 0 ? gameTime + cap : -1;
                this.attackCooldown = effective + 2;
            } else {
                this.attackCooldown = 20;
            }
            return;
        }

        
        if (lineOfFire && this.attackCooldown == 10) {
            AIM_SLOW_UNTIL.put(maid.getUUID(), gameTime + 10L);
            DebugLog.info("slow:" + maid.getUUID(), 0, "aim-slow window opened for 10 ticks");
            if (AIM_SLOW_UNTIL.size() > 256) {
                AIM_SLOW_UNTIL.values().removeIf(until -> until < gameTime);
            }
        }

        if (--this.attackCooldown > 0 || !lineOfFire) {
            return;
        }

        
        Vec3 aim = aimPoint.subtract(maid.position()).normalize();
        FireOutcome outcome = GunplayManager.tryFire(maid, aim);
        DebugLog.info("fire:" + maid.getUUID(), 0, "tryFire outcome={} aim={}", outcome, aim);
        switch (outcome) {
            case FIRED -> this.attackCooldown =
                    gunItem.getGun().fireMode() == FireMode.AUTO ? 2 : 10 + level.random.nextInt(5);
            case FIRE_DELAY_ACTIVE -> this.attackCooldown = 2;
            case EMPTY_MAGAZINE -> this.attackCooldown = 1;
            case RELOADING -> this.attackCooldown = 5;
            default -> this.attackCooldown = 20;
        }
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        this.seeTime = 0;
        this.attackCooldown = -1;
        this.reloadDeadline = -1;
        AIM_SLOW_UNTIL.remove(maid.getUUID());

    }
}
