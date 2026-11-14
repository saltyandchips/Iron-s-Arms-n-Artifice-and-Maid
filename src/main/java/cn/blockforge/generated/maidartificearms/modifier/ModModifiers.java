package cn.blockforge.generated.maidartificearms.modifier;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.modifier.OnShotEffect;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ModModifiers {
    private ModModifiers() {}

    private static final TagKey<EntityType<?>> RAIDERS = EntityTypeTags.RAIDERS;

    
    public static GunModifier rifling() {
        return new ValueStackModifier(Map.of(
                
                ShotComponents.FIRE_RATE,
                new ValueModifier(-0.5D, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL),
                ShotComponents.BULLET_SPEED,
                new ValueModifier(0.5D, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL),
                ShotComponents.DAMAGE,
                new ValueModifier(1.0D, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL)));
    }

    public static GunModifier calmCrosshair() {
        return new CalmCrosshair();
    }

    
    public static GunModifier tacticalGoat() {
        return new EffectModifier(
                map -> map.getOrCreate(ShotComponents.ON_HIT)
                        .getOrCreate(TacticalGoatOnHit.class, TacticalGoatOnHit::new),
                List.of(
                        ModifierText.mechanic("modifier.irons_arms_n_artifice_and_maid.tactical_goat.condition"),
                        ModifierText.conditionalDamage(TacticalGoatOnHit.DAMAGE_BONUS),
                        ModifierText.mechanic("modifier.irons_arms_n_artifice_and_maid.tactical_goat.drop")));
    }

    public static GunModifier lethalStrike() {
        return new EffectModifier(
                map -> map.getOrCreate(ShotComponents.ON_HIT)
                        .getOrCreate(LethalStrikeOnHit.class, LethalStrikeOnHit::new),
                List.of(
                        ModifierText.mechanic("modifier.irons_arms_n_artifice_and_maid.lethal_strike.condition"),
                        ModifierText.conditionalDamage(LethalStrikeOnHit.DAMAGE_BONUS)));
    }

    public static GunModifier silverCore() {
        return new EffectModifier(
                map -> map.getOrCreate(ShotComponents.ON_HIT)
                        .getOrCreate(SilverCoreOnHit.class, SilverCoreOnHit::new),
                List.of(
                        ModifierText.mechanic("modifier.irons_arms_n_artifice_and_maid.silver_core.condition"),
                        ModifierText.conditionalDamage(SilverCoreOnHit.DAMAGE_BONUS),
                        ModifierText.mechanic("modifier.irons_arms_n_artifice_and_maid.silver_core.refill")));
    }

    public static GunModifier reaper() {
        return new EffectModifier(
                map -> map.getOrCreate(ShotComponents.ON_SHOT)
                        .getOrCreate(ReaperLastRoundOnShot.class, ReaperLastRoundOnShot::new),
                List.of(
                        ModifierText.mechanic("modifier.irons_arms_n_artifice_and_maid.reaper.condition"),
                        ModifierText.conditionalDamage(ReaperLastRoundOnShot.DAMAGE_BONUS),
                        ModifierText.mildHarm("modifier.irons_arms_n_artifice_and_maid.reaper.capacity_note")));
    }

    
    static boolean killedByBullet(Entity victim, Bullet bullet) {
        if (!(victim instanceof LivingEntity living) || living.isAlive() || victim.isRemoved()) {
            return false;
        }
        return living.getLastDamageSource() != null
                && living.getLastDamageSource().getDirectEntity() == bullet;
    }

    
    private record EffectModifier(ApplyHook applyHook, List<Component> descriptionLines) implements GunModifier {
        @Override
        public void apply(ShotComponentMap components) {
            applyHook.apply(components);
        }

        @Override
        public void getDescriptionText(Consumer<Component> consumer) {
            descriptionLines.forEach(consumer);
        }
    }

    @FunctionalInterface
    private interface ApplyHook {
        void apply(ShotComponentMap components);
    }

    public static final class TacticalGoatOnHit implements OnHitEffect, ConditionalDamage {
        
        public static final double DAMAGE_BONUS = 0.7D;

        @Override
        public double damageBonusFor(Entity victim, HitResult hit, Bullet bullet) {
            return victim.getType().is(RAIDERS) ? DAMAGE_BONUS : 0.0D;
        }

        @Override
        public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator hits) {
            if (!(hitResult instanceof EntityHitResult entityHit)) {
                return;
            }
            Entity victim = entityHit.getEntity();
            if (victim.getType().is(RAIDERS) && killedByBullet(victim, bullet)) {
                dropEmerald(level, victim);
            }
        }

        private static void dropEmerald(ServerLevel level, Entity victim) {
            ItemEntity emerald = new ItemEntity(level,
                    victim.getX(), victim.getY() + 0.1D, victim.getZ(), new ItemStack(Items.EMERALD));
            emerald.setPickUpDelay(30);
            level.addFreshEntity(emerald);
        }
    }

    public static final class LethalStrikeOnHit implements OnHitEffect, ConditionalDamage {
        
        public static final double DAMAGE_BONUS = 3.0D;

        private static final double EYE_TOLERANCE = 0.1D;

        @Override
        public double damageBonusFor(Entity victim, HitResult hit, Bullet bullet) {
            Vec3 impact = hit.getLocation();
            
            if (!victim.getBoundingBox().contains(impact)) {
                return 0.0D;
            }
            return Math.abs(impact.y - victim.getEyeY()) <= EYE_TOLERANCE ? DAMAGE_BONUS : 0.0D;
        }

        @Override
        public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator hits) {
            
        }
    }

    public static final class SilverCoreOnHit implements OnHitEffect, ConditionalDamage {
        
        public static final double DAMAGE_BONUS = 0.7D;

        @Override
        public double damageBonusFor(Entity victim, HitResult hit, Bullet bullet) {
            return victim instanceof LivingEntity living && living.getType().is(EntityTypeTags.UNDEAD)
                    ? DAMAGE_BONUS : 0.0D;
        }

        @Override
        public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator hits) {
            if (!(hitResult instanceof EntityHitResult entityHit)) {
                return;
            }
            Entity victim = entityHit.getEntity();
            if (!(victim instanceof LivingEntity living) || !living.getType().is(EntityTypeTags.UNDEAD)) {
                return;
            }
            if (killedByBullet(victim, bullet)) {
                refillRound(bullet);
            }
        }

        private static void refillRound(Bullet bullet) {
            if (!(bullet.getOwner() instanceof LivingEntity shooter)) {
                return;
            }
            ItemStack gun = shooter.getMainHandItem();
            if (!(gun.getItem() instanceof GunItem gunItem)) {
                return;
            }
            MagazineContents magazine = GunItem.getMagazine(gun);
            int capacity = gunItem.magazineCapacity();

            if (capacity > 0 && magazine.count() <= capacity) {
                GunItem.setMagazine(gun, magazine.with(magazine.count() + 1));
            }
        }
    }

    public static final class ReaperLastRoundOnShot implements OnShotEffect {
        
        public static final double DAMAGE_BONUS = 1.0D;

        @Override
        public void onShot(ServerLevel level, LivingEntity shooter, ShotProfile profile) {
            ItemStack gun = shooter.getMainHandItem();
            if (!(gun.getItem() instanceof GunItem gunItem)) {
                return;
            }
            if (gunItem.magazineCapacity() < 2) {
                return;
            }
            
            if (!GunItem.getMagazine(gun).isEmpty()) {
                return;
            }
            profile.modifyValue(ShotComponents.DAMAGE,
                    new ValueModifier(DAMAGE_BONUS, ValueModifier.Operation.MULTIPLY_TOTAL,
                            ValueModifier.Type.BENEFICIAL));
        }
    }
}
