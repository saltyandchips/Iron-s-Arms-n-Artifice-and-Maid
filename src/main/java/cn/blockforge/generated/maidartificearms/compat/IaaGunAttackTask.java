package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import cn.blockforge.generated.maidartificearms.MaidArtificeArms;
import cn.blockforge.generated.maidartificearms.registry.ModItems;
import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidUseShieldTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class IaaGunAttackTask implements IRangedAttackTask {

    public static final String IAA_MOD_ID = "irons_artifice";
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MaidArtificeArms.MOD_ID, "iaa_gun_attack");

    public static boolean holdingGun(EntityMaid maid) {
        return isIaaGun(maid.getMainHandItem());
    }

    public static boolean isIaaGun(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof GunItem || stack.is(IronsArtificeTags.GUNS);
    }

    public static int favorabilityLevel(EntityMaid maid) {
        int level;
        try {
            level = maid.getFavorabilityManager().getLevel();
        } catch (RuntimeException e) {
            level = 0;
        }
        return Math.max(0, Math.min(3, level));
    }

    public static float shootRange(EntityMaid maid) {
        float range = switch (favorabilityLevel(maid)) {
            case 1 -> CompatConfig.RANGE_LEVEL_1.get().floatValue();
            case 2 -> CompatConfig.RANGE_LEVEL_2.get().floatValue();
            case 3 -> CompatConfig.RANGE_LEVEL_3.get().floatValue();
            default -> CompatConfig.RANGE_LEVEL_0.get().floatValue();
        };
        
        if (PersonalityHook.isVigilant(maid)) {
            range += 10.0F;
        }
        return range;
    }

    public static boolean canAttackWithGun(EntityMaid maid) {
        if (!holdingGun(maid)) {
            return false;
        }
        ItemStack gun = maid.getMainHandItem();
        if (!(gun.getItem() instanceof GunItem)) {
            return false; 
        }
        if (!GunItem.getMagazine(gun).isEmpty() || GunItem.isReloading(gun)) {
            return true;
        }
        if (CompatConfig.CONSUME_AMMO.get()) {
            return MaidAmmoUtil.countAmmo(maid) > 0;
        }
        
        return true;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModItems.IRON_GUN_MODE_ICON.get());
    }

    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return SoundUtil.attackSound(maid, InitSounds.MAID_RANGE_ATTACK.get(), 1.0F);
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false; 
    }

    @Override
    public float searchRadius(EntityMaid maid) {
        return shootRange(maid);
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        List<Pair<Integer, BehaviorControl<? super EntityMaid>>> tasks = Lists.newArrayList();
        DebugLog.info("brain:" + maid.getUUID(), 0, "createBrainTasks range={} gun={}",
                shootRange(maid), holdingGun(maid));
        
        tasks.add(Pair.of(1, new MaidGunAimBehavior()));
        tasks.add(Pair.of(2, StartAttacking.<EntityMaid>create(
                IaaGunAttackTask::canAttackWithGun,
                this::findTargetFor)));

        
        
        tasks.add(Pair.of(3, StopAttackingIfTargetInvalid.<EntityMaid>create(
                target -> shouldDropTarget(maid, target))));

        tasks.add(Pair.of(4, GunCombatMoveBehavior.create()));
        tasks.add(Pair.of(5, new IaaGunShootBehavior()));
        tasks.add(Pair.of(6, new MaidUseShieldTask()));
        return tasks;
    }

    private static boolean shouldDropTarget(EntityMaid maid, LivingEntity target) {
        return !holdingGun(maid) || farAway(maid, target);
    }

    private static boolean farAway(EntityMaid maid, LivingEntity target) {
        return maid.distanceTo(target) > shootRange(maid);
    }

    private Optional<LivingEntity> findTargetFor(EntityMaid maid) {
        if (CompatConfig.FOLLOW_OWNER_TARGET.get()) {
            LivingEntity followed = OwnerTargetLookup.followedTarget(maid);
            if (followed != null && huntable(maid, followed)) {
                return Optional.of(followed);
            }
        }
        LivingEntity revenge = maid.getTarget();
        if (revenge != null && huntable(maid, revenge)) {
            return Optional.of(revenge);
        }
        return maid.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .flatMap(visible -> visible.findClosest(target -> huntable(maid, target)));
    }

    private boolean huntable(EntityMaid maid, LivingEntity target) {
        if (target == maid || !target.isAlive() || target.isRemoved()) {
            return false;
        }
        if (target.level() != maid.level()) {
            return false;
        }
        if (!maid.canSee(target)) {
            return false;
        }
        if (!IRangedAttackTask.super.canAttack(maid, target)) {
            return false;
        }
        return !farAway(maid, target);
    }

    @Override
    public void performRangedAttack(EntityMaid maid, LivingEntity target, float velocity) {
        if (!holdingGun(maid)) {
            return;
        }
        maid.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ());
        GunplayManager.tryFire(maid, target.position().subtract(maid.position()).normalize());
    }
}
