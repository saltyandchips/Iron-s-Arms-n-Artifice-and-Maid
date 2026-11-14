package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.generated.maidartificearms.MaidArtificeArms;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.neoforged.fml.ModList;

@LittleMaidExtension
public class MaidGunExtension implements ILittleMaid {

    @Override
    public void addMaidTask(TaskManager manager) {
        if (ModList.get().isLoaded(IaaGunAttackTask.IAA_MOD_ID)) {
            manager.add(new IaaGunAttackTask());
            MaidArtificeArms.LOGGER.info("[MaidArtificeArms] Registered maid task: {}", IaaGunAttackTask.UID);
        }
    }
}
