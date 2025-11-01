# Dehydration Flask Upgrades for Traveler's Backpack

This mod adds two powerful upgrades for the Traveler's Backpack that enhance your hydration management when using the Dehydration mod.

## Features

### Flask Upgrade
- **Tank-to-Flask Filling**: Right-click empty leather flasks to fill them with water from your backpack tanks
- **Smart Water Type Handling**: Outputs dirty water flasks when using dirty water from tanks
- **Configurable Cost Scaling**: Choose whether flask filling costs scale with the flask's capacity level
- **Adjustable Fluid Consumption**: Configure how much fluid is consumed per flask level

### Drinking Upgrade
- **Automatic Hydration**: Automatically drinks flasks from your backpack when you get thirsty
- **Water Type Filtering**: Configure whether to drink only pure water or allow dirty/impure water
- **Configurable Timing**: Adjust how often the upgrade checks for thirst and retry delays
- **Smart Integration**: Respects enable/disable toggle and works seamlessly with other upgrades

## How to Use

### Flask Upgrade
1. Craft the Flask Upgrade using leather flasks and water bottles
2. Install the upgrade in your Traveler's Backpack
3. Ensure your backpack has water in its tanks
4. Right-click empty leather flasks to fill them with water from the tanks

### Drinking Upgrade  
1. Craft the Drinking Upgrade using purified water buckets, blank upgrade, and redstone
2. Install the upgrade in your Traveler's Backpack
3. Ensure you have filled flasks in your backpack storage
4. The upgrade will automatically drink flasks when you get thirsty
5. Use the filter button to configure water type preferences

## Configuration

The mod includes a comprehensive configuration system:

### Flask Upgrade Settings
- `fillPerLevel`: Whether flask filling cost scales with the flask's fill level (default: true)
- `fillAmount`: Base fluid amount consumed per flask level in fluid units (default: 27000)

### Drinking Upgrade Settings
- `drinkingUpgradeTickRate`: How often the upgrade checks for thirst in ticks (default: 60)
- `drinkingUpgradeRetryDelay`: Delay between drinking attempts when still thirsty in ticks (default: 20)

## Installation

1. Install Fabric Loader
2. Install Fabric API  
3. Install Dehydration mod
4. Install Traveler's Backpack (Fabric version)
5. Place this mod in your mods folder
6. Both upgrades will appear in the Traveler's Backpack creative tab

## Compatibility

- Minecraft 1.20.1
- Fabric Loader 0.14.21+
- Java 17+
- Dehydration mod
- Traveler's Backpack (Fabric)
- Integrates seamlessly with other Traveler's Backpack upgrades