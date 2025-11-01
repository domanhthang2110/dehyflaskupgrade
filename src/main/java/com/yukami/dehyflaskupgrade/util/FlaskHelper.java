package com.yukami.dehyflaskupgrade.util;

import com.tiviacz.travelersbackpack.inventory.FluidTank;
import com.yukami.dehyflaskupgrade.DehyFlaskUpgrade;

import net.dehydration.item.LeatherFlask;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FlaskHelper {
    
    // Pure water fluid from Dehydration mod
    private static final ResourceLocation PURE_WATER_ID = new ResourceLocation("dehydration", "purified_water");
    
    /**
     * Fills an empty flask based on the fluid type in the tank.
     * Consumes fluid from the tank based on consumption mode.
     * 
     * @param flaskStack The empty flask ItemStack
     * @param tank The fluid tank to drain from
     * @return A new filled flask ItemStack, or empty stack if operation failed
     */
    public static ItemStack fillFlaskFromTank(ItemStack flaskStack, FluidTank tank) {
        return fillFlaskFromTank(flaskStack, tank, true); // Default: consume fluid
    }
    
    /**
     * Fills an empty flask based on the fluid type in the tank.
     * 
     * @param flaskStack The empty flask ItemStack
     * @param tank The fluid tank to check/drain from
     * @param consumeFluid Whether to actually consume fluid from the tank
     * @return A new filled flask ItemStack, or empty stack if operation failed
     */
    public static ItemStack fillFlaskFromTank(ItemStack flaskStack, FluidTank tank, boolean consumeFluid) {
        if (!(flaskStack.getItem() instanceof LeatherFlask)) {
            return ItemStack.EMPTY;
        }
        
        if (LeatherFlask.isFlaskFull(flaskStack)) {
            return ItemStack.EMPTY; // Don't process already full flasks
        }
        
        // Determine water contamination - any dirty water contaminates the whole flask
        FluidVariant fluidVariant = tank.getFluid().fluidVariant();
        int tankWaterPurity = determineWaterPurity(fluidVariant);
        
        // Get current flask purity if it has any content
        int currentFlaskLevel = flaskStack.getOrCreateTag().getInt("leather_flask");
        int finalWaterPurity = tankWaterPurity; // Default to tank's purity
        
        if (currentFlaskLevel > 0) {
            int flaskWaterPurity = flaskStack.getOrCreateTag().getInt("purified_water");
            
            // Water mixing logic: same type = same type, different types = impurified
            if (flaskWaterPurity == tankWaterPurity) {
                finalWaterPurity = flaskWaterPurity; // same type stays same
            } else {
                finalWaterPurity = 1; // different types = impurified
            }
        }
        
        if (tank.getFluid().isEmpty()) {
            return ItemStack.EMPTY; // Tank must have fluid
        }
        
        // Get current and max flask levels
        LeatherFlask flask = (LeatherFlask) flaskStack.getItem();
        int maxCapacity = 2 + flask.addition;
        int currentLevel = currentFlaskLevel; // Use the level we already got
        
        // Calculate fluid cost based on current fill level (for partial fills)
        long fluidCost = DehyFlaskUpgrade.CONFIG.calculateTopUpCost(currentLevel, maxCapacity);
        
        // Check if we can fill at least one level
        long singleLevelCost = DehyFlaskUpgrade.CONFIG.fillAmount;
        if (consumeFluid && tank.getFluidAmount() < singleLevelCost) {
            return ItemStack.EMPTY; // Not enough fluid for any filling
        }
        
        // Calculate how many levels we can actually fill with available fluid
        int levelsToFill = maxCapacity - currentLevel;
        if (consumeFluid && DehyFlaskUpgrade.CONFIG.fillPerLevel) {
            // Calculate maximum levels we can afford
            int affordableLevels = (int) (tank.getFluidAmount() / singleLevelCost);
            levelsToFill = Math.min(levelsToFill, affordableLevels);
            fluidCost = DehyFlaskUpgrade.CONFIG.fillAmount * levelsToFill; // Recalculate actual cost
        }
        
        int finalLevel = currentLevel + levelsToFill;
        
        // Create a copy of the flask to fill
        ItemStack result = flaskStack.copy();
        result.setCount(1);
        
        // Use the final contaminated purity (calculated above)
        
        // Fill the flask
        CompoundTag nbt = result.getOrCreateTag();
        
        nbt.putInt("leather_flask", finalLevel); // Fill to calculated level
        nbt.putInt("purified_water", finalWaterPurity); // Use contaminated purity
        
        // Actually consume the fluid if requested
        if (consumeFluid) {
            tank.drain(fluidCost, false); // false = actually drain
        }
        
        return result;
    }
    
    /**
     * Determines the water purity based on the fluid type.
     * 
     * @param fluidVariant The fluid variant from the tank
     * @return 0 = purified, 1 = impurified, 2 = dirty
     */
    private static int determineWaterPurity(FluidVariant fluidVariant) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid());
        
        // Check if it's pure water from Dehydration mod
        if (PURE_WATER_ID.equals(fluidId)) {
            return 0; // purified
        }
        
        // Check if it's regular water (minecraft:water)
        if ("minecraft".equals(fluidId.getNamespace()) && "water".equals(fluidId.getPath())) {
            return 2; // dirty
        }
        
        // For any other fluid types, default to dirty water
        return 2; // dirty
    }
    
    /**
     * Checks if the fluid in the tank can be used to fill flasks.
     * 
     * @param tank The fluid tank to check
     * @return true if the tank contains water-type fluids
     */
    public static boolean canFillFlaskFromTank(FluidTank tank) {
        return canFillFlaskFromTank(tank, null); // No specific flask - just check fluid type
    }
    
    /**
     * Checks if the fluid in the tank can be used to fill a specific flask.
     * 
     * @param tank The fluid tank to check
     * @param flaskStack The specific flask to check for, or null to skip fluid amount checking
     * @return true if the tank contains water-type fluids (and has enough for the specific flask if provided)
     */
    public static boolean canFillFlaskFromTank(FluidTank tank, ItemStack flaskStack) {
        if (tank.getFluid().isEmpty()) {
            return false;
        }
        
        FluidVariant fluidVariant = tank.getFluid().fluidVariant();
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid());
        
        // Accept water and pure water
        boolean canFill = PURE_WATER_ID.equals(fluidId) || 
               ("minecraft".equals(fluidId.getNamespace()) && "water".equals(fluidId.getPath()));
        
        // Also check if there's enough fluid for at least one level of the specific flask
        if (canFill && flaskStack != null && flaskStack.getItem() instanceof LeatherFlask) {
            LeatherFlask flask = (LeatherFlask) flaskStack.getItem();
            int maxCapacity = 2 + flask.addition;
            int currentLevel = flaskStack.getOrCreateTag().getInt("leather_flask");
            
            // Check if flask is already full
            if (currentLevel >= maxCapacity) {
                canFill = false;
            } else {
                // All fluid types can mix (contamination logic handled in fillFlaskFromTank)
                // Check if we have enough fluid for at least one level
                long singleLevelCost = DehyFlaskUpgrade.CONFIG.fillAmount;
                if (tank.getFluidAmount() < singleLevelCost) {
                    canFill = false;
                }
            }
        }
        
        return canFill;
    }
}