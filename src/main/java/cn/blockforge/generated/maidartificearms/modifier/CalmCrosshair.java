package cn.blockforge.generated.maidartificearms.modifier;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

public final class CalmCrosshair extends ValueStackModifier {

    public static final ValueModifier AIR_BENEFIT =
            new ValueModifier(-1.0D, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL);

    public static final ValueModifier RELOAD_PENALTY =
            new ValueModifier(-0.25D, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL);

    public CalmCrosshair() {
        super(Map.of(
                ShotComponents.IN_AIR_PENALTY, AIR_BENEFIT,
                ShotComponents.RELOAD_SPEED_MULTIPLIER, RELOAD_PENALTY));
    }
}
