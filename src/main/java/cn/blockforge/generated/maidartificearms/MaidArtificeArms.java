package cn.blockforge.generated.maidartificearms;

import cn.blockforge.generated.maidartificearms.client.MaidGunPoseClient;
import cn.blockforge.generated.maidartificearms.common.OwnerCombatTracker;
import cn.blockforge.generated.maidartificearms.compat.MaidAimGuard;
import cn.blockforge.generated.maidartificearms.compat.MaidAmmoKeeper;
import cn.blockforge.generated.maidartificearms.compat.ShotBalanceTuner;
import cn.blockforge.generated.maidartificearms.modifier.ConditionalDamageHooks;
import cn.blockforge.generated.maidartificearms.registry.ModCreativeTabs;
import cn.blockforge.generated.maidartificearms.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(MaidArtificeArms.MOD_ID)
public final class MaidArtificeArms {
    public static final String MOD_ID = "irons_arms_n_artifice_and_maid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidArtificeArms(IEventBus modBus, ModContainer container) {
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);

        container.registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC);

        NeoForge.EVENT_BUS.addListener(OwnerCombatTracker::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(OwnerCombatTracker::onServerTick);

        if (isIaaLoaded()) {
            NeoForge.EVENT_BUS.addListener(ConditionalDamageHooks::onBulletImpact);
            LOGGER.info("[MaidArtificeArms] ConditionalDamageHooks registered (IAA loaded).");
        }

        if (isCompatActive()) {
            NeoForge.EVENT_BUS.addListener(MaidAmmoKeeper::onLivingTick);
            NeoForge.EVENT_BUS.addListener(MaidAimGuard::onLivingTick);
            NeoForge.EVENT_BUS.register(ShotBalanceTuner.class);
            if (FMLEnvironment.dist == Dist.CLIENT) {
                MaidGunPoseClient.init();
            }
            LOGGER.info("[MaidArtificeArms] Touhou Little Maid + Iron's Arms detected. Maid gun compatibility enabled.");
        } else {
            LOGGER.info("[MaidArtificeArms] Soft dependencies missing, compatibility stays dormant.");
        }
    }

    public static boolean isIaaLoaded() {
        return ModList.get().isLoaded("irons_artifice");
    }

    public static boolean isCompatActive() {
        return ModList.get().isLoaded("touhou_little_maid") && isIaaLoaded();
    }
}
