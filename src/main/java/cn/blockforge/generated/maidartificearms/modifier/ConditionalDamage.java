package cn.blockforge.generated.maidartificearms.modifier;

import io.redspace.irons_artifice.entity.Bullet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;

public interface ConditionalDamage {

    double damageBonusFor(Entity victim, HitResult hit, Bullet bullet);
}
