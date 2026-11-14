package cn.blockforge.generated.maidartificearms.registry;

import cn.blockforge.generated.maidartificearms.MaidArtificeArms;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MaidArtificeArms.MOD_ID);

    public static final String TAB_KEY = "itemGroup." + MaidArtificeArms.MOD_ID;

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable(TAB_KEY))
                    .icon(() -> new ItemStack(ModItems.IRON_GUN_MODE_ICON.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(new ItemStack(ModItems.CALM_CROSSHAIR_MODIFIER.get()));
                        output.accept(new ItemStack(ModItems.REAPER_MODIFIER.get()));
                        output.accept(new ItemStack(ModItems.RIFLING_MODIFIER.get()));
                        output.accept(new ItemStack(ModItems.SILVER_CORE_MODIFIER.get()));
                        output.accept(new ItemStack(ModItems.TACTICAL_GOAT_MODIFIER.get()));
                        output.accept(new ItemStack(ModItems.LETHAL_STRIKE_MODIFIER.get()));
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
