package cn.blockforge.generated.maidartificearms.registry;

import cn.blockforge.generated.maidartificearms.MaidArtificeArms;
import cn.blockforge.generated.maidartificearms.modifier.ModModifierItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MaidArtificeArms.MOD_ID);

    
    public static final DeferredHolder<Item, Item> CALM_CROSSHAIR_MODIFIER =
            ITEMS.register("calm_crosshair_modifier", ModItems::calmCrosshair);

    public static final DeferredHolder<Item, Item> REAPER_MODIFIER =
            ITEMS.register("reaper_modifier", ModItems::reaper);

    public static final DeferredHolder<Item, Item> RIFLING_MODIFIER =
            ITEMS.register("rifling_modifier", ModItems::rifling);

    public static final DeferredHolder<Item, Item> SILVER_CORE_MODIFIER =
            ITEMS.register("silver_core_modifier", ModItems::silverCore);

    public static final DeferredHolder<Item, Item> TACTICAL_GOAT_MODIFIER =
            ITEMS.register("tactical_goat_modifier", ModItems::tacticalGoat);

    public static final DeferredHolder<Item, Item> LETHAL_STRIKE_MODIFIER =
            ITEMS.register("lethal_strike_modifier", ModItems::lethalStrike);

    
    public static final DeferredHolder<Item, Item> IRON_GUN_MODE_ICON =
            ITEMS.register("iron_gun_mode_icon", () -> new Item(new Item.Properties().stacksTo(1)));

    private ModItems() {}

    private static boolean iaaLoaded() {
        return ModList.get().isLoaded("irons_artifice");
    }

    private static Item calmCrosshair() {
        return iaaLoaded() ? ModModifierItems.calmCrosshair() : placeholder();
    }

    private static Item reaper() {
        return iaaLoaded() ? ModModifierItems.reaper() : placeholder();
    }

    private static Item rifling() {
        return iaaLoaded() ? ModModifierItems.rifling() : placeholder();
    }

    private static Item silverCore() {
        return iaaLoaded() ? ModModifierItems.silverCore() : placeholder();
    }

    private static Item tacticalGoat() {
        return iaaLoaded() ? ModModifierItems.tacticalGoat() : placeholder();
    }

    private static Item lethalStrike() {
        return iaaLoaded() ? ModModifierItems.lethalStrike() : placeholder();
    }

    private static Item placeholder() {
        return new Item(new Item.Properties().stacksTo(16));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
