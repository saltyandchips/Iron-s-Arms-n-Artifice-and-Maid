package cn.blockforge.generated.maidartificearms.compat;

import cn.blockforge.touhou_little_maid_personality.PersonalityLogic;
import cn.blockforge.touhou_little_maid_personality.capability.PersonalityCapabilities;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

final class PersonalityCompat {

    private PersonalityCompat() {}

    static boolean isFocused(EntityMaid maid) {
        return has(maid, PersonalityLogic.FOCUSED);
    }

    static boolean isCalm(EntityMaid maid) {
        return has(maid, PersonalityLogic.CALM);
    }

    static boolean isVigilant(EntityMaid maid) {
        return has(maid, PersonalityLogic.VIGILANT);
    }

    private static boolean has(EntityMaid maid, int personality) {
        return PersonalityCapabilities.of(maid)
                .map(data -> data.getFirstRoll() == personality || data.getSecondRoll() == personality)
                .orElse(false);
    }
}
