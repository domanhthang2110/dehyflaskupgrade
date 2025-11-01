package com.yukami.dehyflaskupgrade.item;

import com.yukami.dehyflaskupgrade.upgrade.AutoDrinkUpgrade;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tiviacz.travelersbackpack.items.upgrades.UpgradeItem;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;

public class AutoDrinkUpgradeItem extends UpgradeItem {
    
    public AutoDrinkUpgradeItem(Properties properties) {
        super(properties, "auto_drink_upgrade");
    }

    @Override
    public boolean isTickingUpgrade() {
        return true;
    }

    @Override
    public Class<? extends UpgradeBase<?>> getUpgradeClass() {
        return AutoDrinkUpgrade.class;
    }

    @Override
    public TriFunction<UpgradeManager, Integer, ItemStack, Optional<? extends UpgradeBase<?>>> getUpgrade() {
        return (upgradeManager, dataHolderSlot, provider) -> 
            Optional.of(new AutoDrinkUpgrade(upgradeManager, dataHolderSlot, net.minecraft.core.NonNullList.create()));
    }
}