package com.yukami.dehyflaskupgrade.upgrade;

import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.WidgetBase;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.FluidTank;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu;
import com.tiviacz.travelersbackpack.inventory.menu.slot.SlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tiviacz.travelersbackpack.inventory.upgrades.UpgradeBase;
import com.yukami.dehyflaskupgrade.util.FlaskHelper;

import net.dehydration.item.LeatherFlask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FlaskUpgrade extends UpgradeBase<FlaskUpgrade> {
    private final ItemStackHandler flaskSlotsHandler;

    public FlaskUpgrade(UpgradeManager manager, int dataHolderSlot) {
        super(manager, dataHolderSlot, new Point(56, 45)); // Match texture size
        this.flaskSlotsHandler = createFlaskHandler();
    }

    public ItemStackHandler getFlaskSlotsHandler() {
        return this.flaskSlotsHandler;
    }

    @Override
    public void remove() {
        // No special cleanup needed
    }



    @Override
    @Environment(EnvType.CLIENT)
    public WidgetBase<BackpackScreen> createWidget(BackpackScreen screen, int x, int y) {
        return new FlaskWidget(screen, this, new Point(screen.getGuiLeft() + x, screen.getGuiTop() + y));
    }
    
    @Environment(EnvType.CLIENT)
    private static class FlaskWidget extends com.tiviacz.travelersbackpack.client.screens.widgets.UpgradeWidgetBase<FlaskUpgrade> {
        private static final net.minecraft.resources.ResourceLocation FLASK_TEXTURE = 
            new net.minecraft.resources.ResourceLocation("dehyflaskupgrade", "textures/gui/flask_upgrade.png");
        
        public FlaskWidget(BackpackScreen screen, FlaskUpgrade upgrade, Point pos) {
            // Use a UV that points to an empty/transparent area or override the background rendering
            super(screen, upgrade, pos, new Point(200, 200), "screen.travelersbackpack.flask_upgrade");
        }
        
        @Override
        public void renderBg(net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
            if(isTabOpened()) {
                try {
                    // Try using the exact same blitting method as the default (line 91 in UpgradeWidgetBase)
                    // This should render at the correct scale
                    guiGraphics.blit(FLASK_TEXTURE, pos.x(), pos.y(), 0.0f, 0.0f, 56, 45, 56, 45);
                } catch (Exception e) {
                    // Fallback to default background if texture not found
                    super.renderBg(guiGraphics, x, y, mouseX, mouseY);
                }
                // Render the upgrade item icon on top
                guiGraphics.renderItem(screen.getWrapper().getUpgrades().getStackInSlot(this.dataHolderSlot), pos.x() + 4, pos.y() + 4);
            }
        }
    }

    @Override
    public List<SlotItemHandler> getUpgradeSlots(BackpackBaseMenu menu, BackpackWrapper wrapper, int x, int y) {
        List<SlotItemHandler> slots = new ArrayList<>();
        // Slot 0: Input slot (left side)
        slots.add(new FlaskSlotItemHandler(menu.player, this, wrapper, getFlaskSlotsHandler(), 0, x + 7, y + 23));
        // Slot 1: Output slot (right side) 
        slots.add(new FlaskSlotItemHandler(menu.player, this, wrapper, getFlaskSlotsHandler(), 1, x + 33, y + 23));
        return slots;
    }

    private ItemStackHandler createFlaskHandler() {
        return new ItemStackHandler(2) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                // Only allow flasks in input slot (slot 0)
                if (slot == 0) {
                    return stack.getItem() instanceof LeatherFlask && !LeatherFlask.isFlaskFull(stack);
                }
                // Output slot (slot 1) should not accept manual insertion
                return false;
            }

            @Override
            protected void onContentsChanged(int slot) {
                // Process flask when input slot changes (slot 0) OR when output slot is cleared (slot 1)
                if (slot == 0 || (slot == 1 && getStackInSlot(1).isEmpty())) {
                    processFlask();
                }
            }
            
            private void processFlask() {
                ItemStack inputStack = getStackInSlot(0);
                if (inputStack.isEmpty() || !(inputStack.getItem() instanceof LeatherFlask) || LeatherFlask.isFlaskFull(inputStack)) {
                    return;
                }

                var tanksUpgrade = getUpgradeManager().getUpgrade(com.tiviacz.travelersbackpack.inventory.upgrades.tanks.TanksUpgrade.class);
                if (tanksUpgrade.isEmpty()) {
                    return;
                }

                FluidTank leftTank = tanksUpgrade.get().getLeftTank();
                FluidTank rightTank = tanksUpgrade.get().getRightTank();

                // Check both tanks for compatible fluid with this specific flask
                ItemStack result = ItemStack.EMPTY;
                if (FlaskHelper.canFillFlaskFromTank(leftTank, inputStack)) {
                    result = FlaskHelper.fillFlaskFromTank(inputStack, leftTank);
                } else if (FlaskHelper.canFillFlaskFromTank(rightTank, inputStack)) {
                    result = FlaskHelper.fillFlaskFromTank(inputStack, rightTank);
                }

                if (!result.isEmpty()) {
                    // Try to put result in output slot
                    ItemStack existing = getStackInSlot(1);
                    if (existing.isEmpty()) {
                        setStackInSlot(1, result);
                        setStackInSlot(0, ItemStack.EMPTY); // Clear input
                    } else if (ItemStack.isSameItem(existing, result) && 
                              existing.getCount() < existing.getMaxStackSize()) {
                        existing.grow(1);
                        setStackInSlot(0, ItemStack.EMPTY); // Clear input
                    }
                }
            }
        };
    }
}
