package cn.blockforge.generated.maidartificearms;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CompatConfig {
    private CompatConfig() {}

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue RANGE_LEVEL_0 = BUILDER
            .defineInRange("range_favorability_0", 15, 1, 128);

    public static final ModConfigSpec.IntValue RANGE_LEVEL_1 = BUILDER
            .defineInRange("range_favorability_1", 20, 1, 128);

    public static final ModConfigSpec.IntValue RANGE_LEVEL_2 = BUILDER
            .defineInRange("range_favorability_2", 25, 1, 128);

    public static final ModConfigSpec.IntValue RANGE_LEVEL_3 = BUILDER
            .defineInRange("range_favorability_3", 30, 1, 128);

    public static final ModConfigSpec.DoubleValue ENGAGE_RANGE_RATIO = BUILDER
            .defineInRange("engage_range_ratio", 0.45, 0.1, 0.95);

    public static final ModConfigSpec.BooleanValue DEBUG_LOG = BUILDER
            .define("debug_logging", true);

    public static final ModConfigSpec.BooleanValue RAISE_GUN_POSE = BUILDER
            .define("raise_gun_when_hostile", true);

    public static final ModConfigSpec.BooleanValue FOLLOW_OWNER_TARGET = BUILDER
            .define("follow_owner_target", true);

    public static final ModConfigSpec.BooleanValue CONSUME_AMMO = BUILDER
            .define("consume_ammo_from_backpack", true);

    public static final ModConfigSpec.IntValue RELOAD_MAX_TICKS = BUILDER
            .defineInRange("reload_max_ticks", 40, 0, 400);

    public static final ModConfigSpec.BooleanValue APPLY_MOB_NERFS = BUILDER
            .define("apply_mob_gun_nerfs", false);

    public static final ModConfigSpec.IntValue OWNER_TARGET_TTL = BUILDER
            .defineInRange("owner_target_ttl_ticks", 100, 20, 600);

    public static final ModConfigSpec.BooleanValue COIN_PRIORITY = BUILDER
            .define("prioritize_soulfire_coin", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
