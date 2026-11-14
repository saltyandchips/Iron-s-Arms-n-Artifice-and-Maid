package cn.blockforge.generated.maidartificearms.modifier;

import io.redspace.irons_artifice.modifier.ModifierItem;
import net.minecraft.world.item.Item;

public final class ModModifierItems {
    private ModModifierItems() {}

    public static Item tacticalGoat() {
        return modifier(ModModifiers.tacticalGoat());
    }

    public static Item lethalStrike() {
        return modifier(ModModifiers.lethalStrike());
    }

    public static Item silverCore() {
        return modifier(ModModifiers.silverCore());
    }

    public static Item rifling() {
        return modifier(ModModifiers.rifling());
    }

    public static Item reaper() {
        return modifier(ModModifiers.reaper());
    }

    public static Item calmCrosshair() {
        return modifier(ModModifiers.calmCrosshair());
    }

    private static Item modifier(io.redspace.irons_artifice.modifier.GunModifier modifier) {
        return new ModifierItem(new Item.Properties().stacksTo(1), modifier);
    }
}
