package com.yukami.dehyflaskupgrade.upgrade;

import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.FilterUpgradeWidgetBase;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tiviacz.travelersbackpack.inventory.upgrades.filter.ButtonStates;
import com.tiviacz.travelersbackpack.inventory.upgrades.filter.FilterButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@Environment(EnvType.CLIENT)
public class AutoDrinkWidget extends FilterUpgradeWidgetBase<AutoDrinkWidget, AutoDrinkUpgrade> {
    
    public AutoDrinkWidget(BackpackScreen screen, AutoDrinkUpgrade upgrade, Point pos) {
        super(screen, upgrade, pos, new Point(137, 0), "screen.travelersbackpack.drinking_upgrade");
        
        // Create a single filter button for dirty water mode
        FilterButton<AutoDrinkWidget> dirtyWaterButton = new FilterButton<>(this, 
            upgrade.getFilter().get(AutoDrinkFilterSettings.DIRTY_WATER_INDEX), 
            ButtonStates.ALLOW_FEEDING, 
            new Point(pos.x() + 11, pos.y() + 26));
        
        this.addFilterButton(dirtyWaterButton);
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (upgrade.isTabOpened()) {
            // Use your custom texture instead of the default FilterUpgradeBase background
            ResourceLocation customTexture = new ResourceLocation("dehyflaskupgrade", "textures/gui/drinking_upgrade.png");
            
            try {
                // Render your custom background texture
                guiGraphics.blit(customTexture, pos.x(), pos.y(), 0.0f, 0.0f, 40, 50, 40, 50);
                //                                              ↑    ↑    ↑   ↑   ↑   ↑
                //                                            u    v   width height texture_width texture_height
            } catch (Exception e) {
                // Fallback to default if texture not found
                super.renderBg(guiGraphics, x, y, mouseX, mouseY);
            }
            
            // Render the upgrade item icon on top
            guiGraphics.renderItem(screen.getWrapper().getUpgrades().getStackInSlot(this.dataHolderSlot), pos.x() + 4, pos.y() + 4);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(isTabOpened() && isBackpackOwner()) {
            // Handle our single button click
            FilterButton<AutoDrinkWidget> dirtyWaterButton = getFilterButton(ButtonStates.ALLOW_FEEDING);
            if(dirtyWaterButton.mouseClicked(pMouseX, pMouseY, pButton)) {
                // Send filter settings with proper 3-element structure
                com.tiviacz.travelersbackpack.util.PacketDistributorHelper.sendToServer(
                    new com.tiviacz.travelersbackpack.network.ServerboundFilterSettingsPacket(
                        this.dataHolderSlot, 
                        List.of(dirtyWaterButton.getCurrentState(), 0, 0))); // [dirty_water_mode, unused, unused]
                
                this.screen.playUIClickSound();
                return true;
            }
        }
        // Let parent handle tab opening/closing, enable toggle, remove button
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        if(isTabOpened()) {
            if(getFilterButton(ButtonStates.ALLOW_FEEDING).isMouseOver(mouseX, mouseY)) {
                guiGraphics.renderTooltip(screen.getFont(), DIRTY_WATER_TOOLTIPS.get(getFilterButton(ButtonStates.ALLOW_FEEDING).getCurrentState()), mouseX, mouseY);
            }
        }
    }

    private static final List<Component> DIRTY_WATER_TOOLTIPS = List.of(
            Component.literal("Will only drink pure water"),
            Component.literal("Will drink pure, impure, and dirty water"));
}