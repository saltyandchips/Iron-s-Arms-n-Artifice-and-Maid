package cn.blockforge.generated.maidartificearms.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;

public class MaidGunAimBehavior extends Behavior<EntityMaid> {

    public MaidGunAimBehavior() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        return IaaGunAttackTask.holdingGun(maid) && isHostile(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        return IaaGunAttackTask.holdingGun(maid) && isHostile(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        applyAim(maid, true);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        applyAim(maid, true);
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        applyAim(maid, false);
    }

    private static boolean isHostile(EntityMaid maid) {
        Optional<LivingEntity> target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        return target.filter(t -> t != maid && t.isAlive() && !t.isRemoved()).isPresent();
    }

    static void applyAim(EntityMaid maid, boolean aiming) {
        if (maid.isAiming() != aiming) {
            DebugLog.info("aim:" + maid.getUUID(), 0, "aiming -> {}", aiming);
            maid.setAiming(aiming);
        }
        if (maid.isSwingingArms() != aiming) {
            maid.setSwingingArms(aiming);
        }
    }
}
