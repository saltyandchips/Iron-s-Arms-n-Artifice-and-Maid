package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.irons_artifice.entity.SoulfireCoin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class SoulCoinSeeker {
    private SoulCoinSeeker() {}

    public static Entity findNearestCoin(EntityMaid maid) {
        if (!CompatConfig.COIN_PRIORITY.get()) {
            return null;
        }
        Level level = maid.level();
        if (level.isClientSide()) {
            return null;
        }
        double range = IaaGunAttackTask.shootRange(maid);
        AABB searchBox = maid.getBoundingBox().inflate(range);
        List<SoulfireCoin> coins = level.getEntitiesOfClass(
                SoulfireCoin.class, searchBox, coin -> !coin.isRemoved());

        SoulfireCoin best = null;
        double bestDistance = range * range + 1.0D;
        for (SoulfireCoin coin : coins) {
            double distance = coin.distanceToSqr(maid);
            if (distance > range * range || distance >= bestDistance) {
                continue;
            }
            if (!maid.hasLineOfSight(coin)) {
                continue;
            }
            best = coin;
            bestDistance = distance;
        }
        return best;
    }

    public static double aimHeight(Entity coin) {
        return coin.getY() + coin.getBbHeight() * 0.5D;
    }
}
