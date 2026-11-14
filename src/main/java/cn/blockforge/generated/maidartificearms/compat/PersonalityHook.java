package cn.blockforge.generated.maidartificearms.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraftforge.fml.ModList;

public final class PersonalityHook {

    private static final boolean LOADED = ModList.get().isLoaded("touhou_little_maid_personality");

    private PersonalityHook() {}

    public static boolean isFocused(EntityMaid maid) {
        return LOADED && PersonalityCompat.isFocused(maid);
    }

    public static boolean isCalm(EntityMaid maid) {
        return LOADED && PersonalityCompat.isCalm(maid);
    }

    public static boolean isVigilant(EntityMaid maid) {
        return LOADED && PersonalityCompat.isVigilant(maid);
    }
}
