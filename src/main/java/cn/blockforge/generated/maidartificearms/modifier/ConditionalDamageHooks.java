package cn.blockforge.generated.maidartificearms.modifier;

import com.google.common.collect.ArrayListMultimap;
import io.redspace.irons_artifice.api.BulletImpactEvent;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import cn.blockforge.generated.maidartificearms.compat.DebugLog;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.modifier.OnHitEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ConditionalDamageHooks {
    private ConditionalDamageHooks() {}

    private static ValueModifier zoneBonus(double amount) {
        return new ValueModifier(amount, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.NEUTRAL);
    }

    private static boolean isZoneBonus(ValueModifier modifier) {
        return modifier.operation() == ValueModifier.Operation.MULTIPLY_TOTAL
                && modifier.type() == ValueModifier.Type.NEUTRAL;
    }

    public static void onBulletImpact(BulletImpactEvent event) {
        HitResult hit = event.getRayTraceResult();
        if (!(hit instanceof EntityHitResult entityHit)) {
            return;
        }
        Bullet bullet = event.getBullet();
        
        if (bullet.level().isClientSide()) {
            return;
        }
        ShotProfile profile = bullet.getProfile();
        if (profile == null) {
            return;
        }
        OnHitEffects effects = profile.peek(ShotComponents.ON_HIT);
        DebugLog.info("impact", 0, "bullet impact: onHitEffects={}", effects == null ? -1 : effects.all().size());
        if (effects == null || effects.isEmpty()) {
            return;
        }
        Entity victim = entityHit.getEntity();
        double bonus = 0.0D;
        boolean conditioned = false;
        for (OnHitEffect effect : effects.all()) {
            if (effect instanceof ConditionalDamage conditional) {
                conditioned = true;
                bonus += conditional.damageBonusFor(victim, hit, bullet);
            }
        }
        
        if (!conditioned) {
            return;
        }
        DebugLog.info("zone", 0, "conditional damage zone: bonus={} victim={}", bonus, victim.getType());
        applyDamageZoneBonus(profile.components(), bonus);
    }

    private static void applyDamageZoneBonus(ShotComponentMap components, double bonus) {
        if (!components.has(ShotComponents.DAMAGE)) {
            return;
        }
        Value current = components.getOrDefault(ShotComponents.DAMAGE);
        Value rebuilt = new Value(current.base(), ArrayListMultimap.create());
        for (ValueModifier modifier : current.modifiers()) {
            if (!isZoneBonus(modifier)) {
                rebuilt.addModifier(modifier);
            }
        }
        if (bonus > 0.0D) {
            rebuilt.addModifier(zoneBonus(bonus));
        }
        components.set(ShotComponents.DAMAGE, rebuilt);
    }
}
