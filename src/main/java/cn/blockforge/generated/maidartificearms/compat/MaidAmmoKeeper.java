package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MaidAmmoKeeper {
    private MaidAmmoKeeper() {}

    private static final class Snapshot {
        int mag;
        boolean wasReloading;
        long seenGameTime;
        WeakReference<ItemStack> gun;

        Snapshot(int mag, boolean wasReloading, long seenGameTime, ItemStack gun) {
            this.mag = mag;
            this.wasReloading = wasReloading;
            this.seenGameTime = seenGameTime;
            this.gun = new WeakReference<>(gun);
        }
    }

    private static final Map<UUID, Snapshot> TRACKED = new HashMap<>();

    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof EntityMaid maid) || maid.level().isClientSide) {
            return;
        }
        long now = maid.level().getGameTime();
        ItemStack gun = maid.getMainHandItem();

        if (!(gun.getItem() instanceof GunItem) || !CompatConfig.CONSUME_AMMO.get()) {
            TRACKED.remove(maid.getUUID());
            return;
        }

        int mag = GunItem.getMagazine(gun).count();
        boolean reloading = GunItem.isReloading(gun);
        Snapshot prev = TRACKED.get(maid.getUUID());

        if (prev != null && prev.wasReloading && !reloading && mag > prev.mag
                && prev.gun.get() == gun) {
            int gained = mag - prev.mag;
            int removed = MaidAmmoUtil.extractAmmo(maid, gained);
            if (removed < gained) {
                
                GunItem.setMagazine(gun, MagazineContents.get(gun).with(mag - (gained - removed)));
            }
        }

        TRACKED.put(maid.getUUID(), new Snapshot(mag, reloading, now, gun));

        if (TRACKED.size() > 256 && now % 100 == 0) {
            TRACKED.values().removeIf(s -> now - s.seenGameTime > 60 || s.gun.get() == null);
        }
    }
}
