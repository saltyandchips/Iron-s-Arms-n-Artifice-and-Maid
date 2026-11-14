package cn.blockforge.generated.maidartificearms.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

public final class MaidAmmoUtil {
    private MaidAmmoUtil() {}

    public static int countAmmo(EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        int total = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(IronsArtificeTags.AMMO)) {
                total += stack.getCount();
            } else if (stack.getItem() instanceof BulletContainerItem) {
                total += BulletContainerItem.count(stack);
            }
        }
        return total;
    }

    public static int extractAmmo(EntityMaid maid, int amount) {
        if (amount <= 0) {
            return 0;
        }
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        int remaining = amount;
        for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.is(IronsArtificeTags.AMMO)) {
                continue;
            }
            int take = Math.min(remaining, stack.getCount());
            remaining -= inv.extractItem(i, take, false).getCount();
        }
        for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!(stack.getItem() instanceof BulletContainerItem)) {
                continue;
            }
            remaining -= BulletContainerItem.drain(stack, remaining);
        }
        return amount - remaining;
    }
}
