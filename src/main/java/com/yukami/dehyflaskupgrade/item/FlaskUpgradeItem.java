package com.yukami.dehyflaskupgrade.item;

import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.tiviacz.travelersbackpack.items.upgrades.UpgradeItem;
import com.yukami.dehyflaskupgrade.upgrade.FlaskUpgrade;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;

public class FlaskUpgradeItem extends UpgradeItem {
    
    public FlaskUpgradeItem(Properties properties) {
        super(properties, "flask_upgrade");
    }

    @Override
    public boolean requiresEquippedBackpack() {
        return false;
    }

    @Override
    public Class<? extends UpgradeBase<?>> getUpgradeClass() {
        return FlaskUpgrade.class;
    }

    @Override
    public TriFunction<UpgradeManager, Integer, ItemStack, Optional<? extends UpgradeBase<?>>> getUpgrade() {
        return (upgradeManager, dataHolderSlot, provider) -> 
            Optional.of(new FlaskUpgrade(upgradeManager, dataHolderSlot));
    }
}