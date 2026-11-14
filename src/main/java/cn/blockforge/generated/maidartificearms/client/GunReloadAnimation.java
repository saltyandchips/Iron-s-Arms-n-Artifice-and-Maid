package cn.blockforge.generated.maidartificearms.client;

import cn.blockforge.generated.maidartificearms.MaidArtificeArms;
import cn.blockforge.generated.maidartificearms.compat.DebugLog;
import cn.blockforge.generated.maidartificearms.compat.IaaGunAttackTask;
import com.github.tartaricacid.touhoulittlemaid.api.entity.IMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationManager;
import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.world.item.ItemStack;

public final class GunReloadAnimation {

    private GunReloadAnimation() {}

    public static void init() {
        AnimationManager manager = AnimationManager.getInstance();
        manager.register(new AnimationState("tac:reload:rifle",
                ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME, 0,
                (maid, event) -> shouldPlay(maid, ArmPoseKind.RIFLE)));
        manager.register(new AnimationState("tac:reload:pistol",
                ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME, 0,
                (maid, event) -> shouldPlay(maid, ArmPoseKind.PISTOL)));
        MaidArtificeArms.LOGGER.info("[MaidArtificeArms] Reload animation states registered (hold-on-last-frame).");
    }

    private static boolean shouldPlay(IMaid maid, ArmPoseKind kind) {
        if (!(maid.asEntity() instanceof EntityMaid entity)) {
            return false;
        }
        if (!IaaGunAttackTask.UID.equals(entity.getTask().getUid())) {
            return false;
        }
        ItemStack stack = entity.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) {
            return false;
        }
        if (!GunItem.isReloading(stack)) {
            return false;
        }
        boolean match = gun.getGun().armPoseKind() == kind;
        if (match) {
            DebugLog.info("reload:anim:" + entity.getUUID(), 20, "reload animation state active (kind={})", kind);
        }
        return match;
    }
}
