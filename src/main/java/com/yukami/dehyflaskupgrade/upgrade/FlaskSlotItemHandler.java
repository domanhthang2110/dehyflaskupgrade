package com.yukami.dehyflaskupgrade.upgrade;

import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import com.tiviacz.travelersbackpack.inventory.menu.slot.SlotItemHandler;
import net.minecraft.world.entity.player.Player;

public class FlaskSlotItemHandler extends SlotItemHandler {
    private final int index;
    public BackpackWrapper wrapper;
    public Player player;
    public FlaskUpgrade upgrade;

    public FlaskSlotItemHandler(Player player, FlaskUpgrade upgrade, BackpackWrapper wrapper, 
                               ItemStackHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.wrapper = wrapper;
        this.index = index;
        this.player = player;
        this.upgrade = upgrade;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        if (upgrade.isTabOpened()) {
            if (index == 1) { // Output slot
                return super.mayPickup(playerIn) && this.hasItem();
            }
            return super.mayPickup(playerIn);
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && upgrade.isTabOpened();
    }
}