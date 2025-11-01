package com.yukami.dehyflaskupgrade;

import com.mojang.logging.LogUtils;
import com.yukami.dehyflaskupgrade.config.FlaskConfig;
import com.yukami.dehyflaskupgrade.item.FlaskUpgradeItem;
import com.yukami.dehyflaskupgrade.item.AutoDrinkUpgradeItem;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

public class DehyFlaskUpgrade implements ModInitializer {
    public static final String MOD_ID = "dehyflaskupgrade";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    // Config instance
    public static FlaskConfig CONFIG;
    
    // Traveler's Backpack creative tab reference
    private static final ResourceKey<CreativeModeTab> TRAVELERS_BACKPACK_TAB = 
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("travelersbackpack", "travelers_backpack"));
    
    
    // Flask Upgrade Item
    public static final Item FLASK_UPGRADE = Registry.register(
        BuiltInRegistries.ITEM, 
        new ResourceLocation(MOD_ID, "flask_upgrade"), 
        new FlaskUpgradeItem(new Item.Properties().stacksTo(16))
    );
    
    // Drinking Upgrade Item
    public static final Item AUTO_DRINK_UPGRADE = Registry.register(
        BuiltInRegistries.ITEM,
        new ResourceLocation(MOD_ID, "drinking_upgrade"),
        new AutoDrinkUpgradeItem(new Item.Properties().stacksTo(16))
    );

    @Override
    public void onInitialize() {
        // Initialize Cloth Config
        AutoConfig.register(FlaskConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(FlaskConfig.class).getConfig();
        
        // Add our upgrades to Traveler's Backpack creative tab
        registerCreativeTabItems();
        
        LOGGER.info("Dehydration-Traveler's Backpack Flask Upgrade mod initialized!");
    }
    
    private void registerCreativeTabItems() {
        ItemGroupEvents.modifyEntriesEvent(TRAVELERS_BACKPACK_TAB).register(entries -> {
            // Add our upgrades after the existing upgrades in the tab
            entries.accept(FLASK_UPGRADE);
            entries.accept(AUTO_DRINK_UPGRADE);
        });
        
        LOGGER.info("Added Flask Upgrade and Drinking Upgrade to Traveler's Backpack creative tab");
    }
}