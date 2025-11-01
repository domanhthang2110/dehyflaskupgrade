package com.yukami.dehyflaskupgrade.upgrade;

import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import com.tiviacz.travelersbackpack.inventory.upgrades.FilterSettingsBase;
import net.dehydration.item.LeatherFlask;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AutoDrinkFilterSettings extends FilterSettingsBase {
    
    // Mirror the constants from AutoDrinkUpgrade for consistency
    public static final int DIRTY_WATER_INDEX = 0; // Our setting is at index 0
    
    public AutoDrinkFilterSettings(ItemStackHandler storageAccess, List<ItemStack> filterStacks, List<Integer> filter) {
        super(storageAccess, filterStacks, filter, 0); // 0 filter slots since we don't need item filtering
    }
    
    @Override
    public boolean matchesFilter(Player player, ItemStack stack) {
        // Auto drink upgrade should match all flasks in storage
        // The actual filtering (pure vs dirty) is done in the upgrade logic
        return stack.getItem() instanceof LeatherFlask && !LeatherFlask.isFlaskEmpty(stack);
    }
}