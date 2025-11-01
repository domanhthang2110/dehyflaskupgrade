package com.yukami.dehyflaskupgrade.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * Flask Upgrade Configuration using Cloth Config
 * 
 * This config file can be found in config/dehyflaskupgrade.json5
 * Users can edit it to customize flask filling costs
 */
@Config(name = "dehyflaskupgrade")
public class FlaskConfig implements ConfigData {
    
    @ConfigEntry.Category("flask_economy")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean fillPerLevel = true;
    
    @ConfigEntry.Category("flask_economy") 
    @ConfigEntry.BoundedDiscrete(min = 100, max = 81000)
    @ConfigEntry.Gui.Tooltip(count = 5)
    public long fillAmount = 27000;
    
    @ConfigEntry.Category("auto_drink")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 200)
    @ConfigEntry.Gui.Tooltip(count = 3)
    public int drinkingUpgradeTickRate = 60;
    
    @ConfigEntry.Gui.Tooltip(count = 3)
    public int drinkingUpgradeRetryDelay = 20;
    
    /**
     * Calculate the actual fluid cost for filling a flask
     * @param flaskFillLevel The fill level the flask will have when full
     * @return The fluid cost in units
     */
    public long calculateFluidCost(int flaskFillLevel) {
        if (fillPerLevel) {
            return fillAmount * flaskFillLevel;
        } else {
            return fillAmount;
        }
    }
    
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
    
    /**
     * Get a human-readable description of the current config
     */
    public String getConfigDescription() {
        if (fillPerLevel) {
            return "Per-level cost: " + fillAmount + " fluid per fill level";
        } else {
            return "Flat cost: " + fillAmount + " fluid per flask";
        }
    }
}