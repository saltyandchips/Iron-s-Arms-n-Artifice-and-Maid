package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.IGunslingerMob;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ShotBalanceTuner {

    private static final double IAA_MOB_NERF = 0.25D;

    private static final double IAA_MOB_INVERSE = 1.0D / (1.0D - IAA_MOB_NERF) - 1.0D;

    private ShotBalanceTuner() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComposeShot(ComposeShotEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        ShotProfile profile = event.getShotProfile();
        if (CompatConfig.APPLY_MOB_NERFS.get()) {
            IGunslingerMob.applyDefaultMobNerfs(maid, profile);
        } else {
            
            profile.modifyValue(ShotComponents.DAMAGE,
                    new ValueModifier(IAA_MOB_INVERSE, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
            profile.modifyValue(ShotComponents.BULLET_SPEED,
                    new ValueModifier(IAA_MOB_INVERSE, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
            int difficultyIndex = maid.level().getDifficulty().getId();
            int spread = 4 - difficultyIndex;
            if (spread != 0) {
                profile.modifyValue(ShotComponents.SPREAD,
                        new ValueModifier(-spread, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL));
            }
        }
        applyPersonality(maid, profile);
    }

    private static void applyPersonality(EntityMaid maid, ShotProfile profile) {
        if (PersonalityHook.isFocused(maid)) {
            profile.modifyValue(ShotComponents.RELOAD_SPEED_MULTIPLIER,
                    new ValueModifier(0.5D, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
        }
        if (PersonalityHook.isCalm(maid)) {
            profile.modifyValue(ShotComponents.IN_AIR_PENALTY,
                    new ValueModifier(-0.3D, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
        }
    }
}
