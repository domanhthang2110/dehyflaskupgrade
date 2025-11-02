package com.yukami.dehyflaskupgrade.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

/**
 * Flask Upgrade Configuration using Cloth Config
 * 
 * This config file can be found in config/dehyflaskupgrade.json5
 * Users can edit it to customize flask filling costs
 */
@Config(name = "dehyflaskupgrade")
public class FlaskConfig implements ConfigData {
    
    @ConfigEntry.Category("flask_upgrade")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @Comment("Whether flask filling cost scales with the flask's fill level")
    public boolean fillPerLevel = true;
    
    @ConfigEntry.Category("flask_upgrade") 
    @ConfigEntry.Gui.Tooltip(count = 4)
    @Comment("Base fluid amount consumed per flask level in fluid units (27000 = 1 bottle, 81000 = 1 bucket)")
    public long fillAmount = 27000;
    
    @ConfigEntry.Category("auto_drink")
    @Comment("How often the Drinking Upgrade checks if player needs hydration (in ticks)")
    public int drinkingUpgradeTickRate = 100;

    @ConfigEntry.Category("auto_drink")
    @Comment("Delay between drinking attempts when still thirsty (in ticks)")
    public int drinkingUpgradeRetryDelay = 60;
    
    /**
     * Calculate the fluid cost for topping up a partially filled flask
     * @param currentFillLevel The current fill level of the flask
     * @param maxFillLevel The maximum fill level the flask will have when full
     * @return The fluid cost in units for the missing levels
     */
    public long calculateTopUpCost(int currentFillLevel, int maxFillLevel) {
        if (fillPerLevel) {
            int missingLevels = maxFillLevel - currentFillLevel;
            return fillAmount * missingLevels;
        } else {
            return fillAmount;
        }
    }
}