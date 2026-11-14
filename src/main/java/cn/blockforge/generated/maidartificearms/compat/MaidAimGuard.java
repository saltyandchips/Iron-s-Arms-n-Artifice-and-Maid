package cn.blockforge.generated.maidartificearms.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Optional;

public final class MaidAimGuard {
    private MaidAimGuard() {}

    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid) || maid.level().isClientSide) {
            return;
        }
        if (!maid.isAiming()) {
            return;
        }
        
        if (!IaaGunAttackTask.UID.equals(maid.getTask().getUid())) {
            return;
        }
        if (!shouldKeepAiming(maid)) {
            MaidGunAimBehavior.applyAim(maid, false);
        }
    }

    private static boolean shouldKeepAiming(EntityMaid maid) {
        if (!IaaGunAttackTask.holdingGun(maid)) {
            return false;
        }
        if (SoulCoinSeeker.findNearestCoin(maid) != null) {
            return true;
        }
        Optional<LivingEntity> target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        return target.filter(t -> t != maid && t.isAlive() && !t.isRemoved()).isPresent();
    }
}
