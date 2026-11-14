package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.common.OwnerCombatTracker;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class OwnerTargetLookup {
    private OwnerTargetLookup() {}

    public static LivingEntity followedTarget(EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        if (!(owner instanceof Player player) || !player.isAlive()) {
            return null;
        }
        return OwnerCombatTracker.recentTarget(player, maid.level().getGameTime());
    }
}
