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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MaidArtificeArms.MOD_ID)
public final class MaidArtificeArms {
    public static final String MOD_ID = "irons_arms_n_artifice_and_maid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidArtificeArms() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CompatConfig.SPEC);

        MinecraftForge.EVENT_BUS.addListener(OwnerCombatTracker::onAttackEntity);
        MinecraftForge.EVENT_BUS.addListener(OwnerCombatTracker::onServerTick);

        if (isIaaLoaded()) {

            
            
            MinecraftForge.EVENT_BUS.addListener(ConditionalDamageHooks::onBulletImpact);
            LOGGER.info("[MaidArtificeArms] ConditionalDamageHooks registered (IAA loaded).");
        }

        if (isCompatActive()) {
            
            MinecraftForge.EVENT_BUS.addListener(MaidAmmoKeeper::onLivingTick);
            
            MinecraftForge.EVENT_BUS.addListener(MaidAimGuard::onLivingTick);
            MinecraftForge.EVENT_BUS.register(ShotBalanceTuner.class);

            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MaidGunPoseClient::init);
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
