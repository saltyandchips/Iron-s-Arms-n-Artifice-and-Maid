package cn.blockforge.generated.maidartificearms.modifier;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ModifierText {
    private ModifierText() {}

    public static Component mechanic(String key, Object... args) {
        return styled(Component.translatable(key, args), ChatFormatting.AQUA);
    }

    public static Component benefit(String key, Object... args) {
        return styled(Component.translatable(key, args), ChatFormatting.GREEN);
    }

    public static Component harm(String key, Object... args) {
        return styled(Component.translatable(key, args), ChatFormatting.RED);
    }

    public static Component mildHarm(String key, Object... args) {
        return styled(Component.translatable(key, args), ChatFormatting.GOLD);
    }

    public static Component conditionalDamage(double multiplyTotal) {
        return Utils.formatValueModifierDescription(
                new ValueModifier(multiplyTotal, ValueModifier.Operation.MULTIPLY_TOTAL,
                        ValueModifier.Type.BENEFICIAL),
                ShotComponents.DAMAGE);
    }

    private static Component styled(Component component, ChatFormatting color) {
        if (component instanceof net.minecraft.network.chat.MutableComponent mutable) {
            return mutable.withStyle(color);
        }
        return component.copy().withStyle(color);
    }
}
