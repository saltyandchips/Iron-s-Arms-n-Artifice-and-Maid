package cn.blockforge.generated.maidartificearms.common;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OwnerCombatTracker {
    private OwnerCombatTracker() {}

    private record Entry(WeakReference<LivingEntity> target, long expiresAtGameTime) {}

    private static final Map<UUID, Entry> RECENT = new ConcurrentHashMap<>();
    private static int cleanupTimer = 0;

    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (event.getTarget() instanceof LivingEntity victim && victim.isAlive()) {
            long expires = player.level().getGameTime() + CompatConfig.OWNER_TARGET_TTL.get();
            RECENT.put(player.getUUID(), new Entry(new WeakReference<>(victim), expires));
        }
    }

    public static LivingEntity recentTarget(Player owner, long gameTime) {
        Entry entry = RECENT.get(owner.getUUID());
        if (entry == null) {
            return null;
        }
        if (entry.expiresAtGameTime() < gameTime) {
            RECENT.remove(owner.getUUID());
            return null;
        }
        LivingEntity target = entry.target().get();
        if (target == null || !target.isAlive() || target.isRemoved()) {
            RECENT.remove(owner.getUUID());
            return null;
        }
        return target;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (++cleanupTimer >= 120) {
            cleanupTimer = 0;
            RECENT.values().removeIf(entry -> {
                LivingEntity held = entry.target().get();
                return held == null || !held.isAlive() || held.isRemoved();
            });
        }
    }
}
