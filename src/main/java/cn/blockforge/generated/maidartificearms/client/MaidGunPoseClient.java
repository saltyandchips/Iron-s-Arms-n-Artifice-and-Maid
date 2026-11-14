package cn.blockforge.generated.maidartificearms.client;

import cn.blockforge.generated.maidartificearms.CompatConfig;
import cn.blockforge.generated.maidartificearms.MaidArtificeArms;
import cn.blockforge.generated.maidartificearms.compat.DebugLog;
import cn.blockforge.generated.maidartificearms.compat.IaaGunAttackTask;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.condition.ConditionManager;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.condition.ConditionalHold;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.file.AnimationFile;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Map;

public final class MaidGunPoseClient {

    private static final Map<ResourceLocation, PoseHold> WRAPPERS = new HashMap<>();

    private static long clientTick = 0L;

    private MaidGunPoseClient() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(MaidGunPoseClient::onClientTick);
        install();
        MaidArtificeArms.LOGGER.info("[MaidArtificeArms] Maid gun hold-pose hook installed (TLM built-in tac animations).");
        DebugLog.info("pose:init", 0, "hold-pose hook init, table={}", ConditionManager.HOLD_MAINHAND.getClass().getName());
        GunReloadAnimation.init();
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        clientTick++;
        
        if (clientTick % 20 == 0) {
            install();
        }
    }

    private static void install() {
        if (ConditionManager.HOLD_MAINHAND instanceof PoseMap) {
            return;
        }
        PoseMap map = new PoseMap();
        map.putAll(ConditionManager.HOLD_MAINHAND);
        ConditionManager.HOLD_MAINHAND = map;
        WRAPPERS.clear();
        DebugLog.info("pose:install", 0, "HOLD_MAINHAND replaced, workspace size={}", map.size());
    }

    static boolean shouldHold(IMaid maid) {
        if (!CompatConfig.RAISE_GUN_POSE.get()) {
            return false;
        }
        if (!(maid.asEntity() instanceof EntityMaid entity)) {
            return false;
        }
        if (!IaaGunAttackTask.UID.equals(entity.getTask().getUid())) {
            return false;
        }
        return IaaGunAttackTask.isIaaGun(entity.getMainHandItem());
    }

    private static final class PoseMap extends HashMap<ResourceLocation, ConditionalHold> {

        ConditionalHold original(ResourceLocation location) {
            return super.get(location);
        }

        @Override
        public ConditionalHold get(Object key) {
            if (key instanceof ResourceLocation location) {
                return WRAPPERS.computeIfAbsent(location, PoseHold::new);
            }
            return super.get(key);
        }
    }

    private static final class PoseHold extends ConditionalHold {

        private final ResourceLocation location;

        PoseHold(ResourceLocation location) {
            super(InteractionHand.MAIN_HAND);
            this.location = location;
        }

        @Override
        public String doTest(IMaid maid, InteractionHand hand) {
            if (hand == InteractionHand.MAIN_HAND && maid.asEntity() instanceof EntityMaid entity
                    && IaaGunAttackTask.UID.equals(entity.getTask().getUid())) {
                if (shouldHold(maid)) {
                    String pose = pickAnimation(maid);
                    DebugLog.info("pose:" + entity.getUUID(), 20,
                            "doTest hold=true pose={} hasAnim={} reload={} aiming={} sprint={}",
                            pose, pose != null, GunItem.isReloading(entity.getMainHandItem()),
                            entity.isAiming(), entity.isSprinting() && entity.onGround());
                    if (pose != null) {
                        return pose;
                    }
                } else {
                    DebugLog.info("pose:no:" + entity.getUUID(), 20,
                            "doTest hold=false config={} gun={} task={}",
                            CompatConfig.RAISE_GUN_POSE.get(), IaaGunAttackTask.isIaaGun(entity.getMainHandItem()),
                            entity.getTask().getUid());
                }
            }
            
            if (ConditionManager.HOLD_MAINHAND instanceof PoseMap map) {
                ConditionalHold original = map.original(location);
                return original == null ? null : original.doTest(maid, hand);
            }
            return null;
        }

        private String pickAnimation(IMaid maid) {
            if (!(maid.asEntity() instanceof EntityMaid entity)) {
                return null;
            }
            ItemStack stack = entity.getMainHandItem();
            if (!(stack.getItem() instanceof GunItem gun)) {
                return null;
            }
            String type = gun.getGun().armPoseKind() == ArmPoseKind.PISTOL ? "pistol" : "rifle";
            if (GunItem.isReloading(stack)) {

                return null;
            }
            String prefix;
            if (entity.isAiming()) {
                prefix = "aim";
            } else if (entity.isSprinting() && entity.onGround()) {
                prefix = "run";
            } else {
                prefix = "hold";
            }
            String name = "tac:" + prefix + ":" + type;
            return hasAnimation(name) ? name : null;
        }

        private boolean hasAnimation(String name) {
            AnimationFile file = GeckoLibCache.getInstance().getAnimations().get(location);
            return file != null && file.getAnimation(name) != null;
        }
    }
}
