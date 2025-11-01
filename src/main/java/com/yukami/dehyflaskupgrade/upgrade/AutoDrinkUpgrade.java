package com.yukami.dehyflaskupgrade.upgrade;

import com.yukami.dehyflaskupgrade.DehyFlaskUpgrade;
import com.tiviacz.travelersbackpack.client.screens.BackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.widgets.WidgetBase;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.StorageAccessWrapper;
import com.tiviacz.travelersbackpack.inventory.UpgradeManager;
import com.tiviacz.travelersbackpack.inventory.handler.ItemStackHandler;
import com.tiviacz.travelersbackpack.inventory.menu.BackpackBaseMenu;
import com.tiviacz.travelersbackpack.inventory.upgrades.FilterUpgradeBase;
import com.tiviacz.travelersbackpack.inventory.upgrades.IEnable;
import com.tiviacz.travelersbackpack.inventory.upgrades.ITickableUpgrade;
import com.tiviacz.travelersbackpack.inventory.upgrades.Point;
import com.tiviacz.travelersbackpack.inventory.upgrades.filter.FilterHandler;
import net.minecraft.core.NonNullList;
import com.tiviacz.travelersbackpack.util.InventoryHelper;
import net.dehydration.access.ThirstManagerAccess;
import net.dehydration.item.LeatherFlask;
import net.dehydration.thirst.ThirstManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoDrinkUpgrade extends FilterUpgradeBase<AutoDrinkUpgrade, AutoDrinkFilterSettings> implements IEnable, ITickableUpgrade {
    
    private static final int THIRST_THRESHOLD = 15; // Drink when thirst is below 75% (15/20)
    
    public AutoDrinkUpgrade(UpgradeManager manager, int dataHolderSlot, NonNullList<ItemStack> filter) {
        // FilterUpgradeBase calculates size as: Point(openTabSize.x(), openTabSize.y() + (18 * getRows()))
        // Since we have 0 filter slots, getRows() = 0, so final size = openTabSize
        // Let's try different openTabSize values to control the actual widget size:
        super(manager, dataHolderSlot, new Point(40, 50), 0, 0, filter, List.of());
    }



    @Override
    public void setEnabled(boolean enabled) {
        // Store enabled state in NBT
        ItemStack stack = getDataHolderStack().copy();
        com.tiviacz.travelersbackpack.util.NbtHelper.set(stack, 
            com.tiviacz.travelersbackpack.init.ModDataHelper.UPGRADE_ENABLED, enabled);
        getUpgradeManager().getUpgradesHandler().setStackInSlot(getDataHolderSlot(), stack);
    }


    @Override
    public int getTickRate() {
        return DehyFlaskUpgrade.CONFIG.drinkingUpgradeTickRate; // Use updated config
    }
    
    private int getRetryDelay() {
        return DehyFlaskUpgrade.CONFIG.drinkingUpgradeRetryDelay;
    }

    @Override
    public void tick(@Nullable Player player, Level level, BlockPos pos, int currentTick) {
        // Don't tick if upgrade is disabled
        if (!isEnabled(this)) {
            return;
        }
        
        if(getCooldown() == 0) {
            return;
        }
        if(currentTick % getCooldown() != 0) {
            return;
        }

        if(level.isClientSide) {
            return;
        }

        //Load storage if not loaded in artificial wrapper
        getUpgradeManager().getWrapper().loadAdditionally(BackpackWrapper.STORAGE_ID);

        if(drinkPlayerAndGetThirsty(player, level)) {
            setCooldown(getRetryDelay()); // Use configurable retry delay
            return;
        }

        if(!hasCooldown() || getCooldown() != getTickRate()) {
            setCooldown(getTickRate());
        }
    }

    private boolean drinkPlayerAndGetThirsty(Player player, Level level) {
        if(!isPlayerThirsty(player) || level.isClientSide) {
            return false;
        }
        return tryDrinkingFromStorage(player, level) && isPlayerThirsty(player);
    }

    private boolean isPlayerThirsty(Player player) {
        try {
            ThirstManager thirstManager = ((ThirstManagerAccess) player).getThirstManager();
            return thirstManager.getThirstLevel() < THIRST_THRESHOLD;
        } catch (Exception e) {
            // If Dehydration API fails, don't auto-drink
            return false;
        }
    }

    private boolean tryDrinkingFromStorage(Player player, Level level) {
        ItemStackHandler storage = getUpgradeManager().getWrapper().getStorage();
        return InventoryHelper.iterate(storage, (slot, stack) -> 
            tryDrinkingFlask(player, level, slot, stack, storage));
    }

    private boolean tryDrinkingFlask(Player player, Level level, int slot, ItemStack stack, ItemStackHandler storage) {
        if (!(stack.getItem() instanceof LeatherFlask) || LeatherFlask.isFlaskEmpty(stack)) {
            return false;
        }

        // Check if we should drink this flask type
        if (!shouldDrinkFlask(stack)) {
            return false;
        }

        // Follow exact feeding upgrade pattern
        ItemStack mainHandItem = player.getMainHandItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        ItemStack singleItemCopy = stack.copy();
        singleItemCopy.setCount(1);

        if(singleItemCopy.use(level, player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.CONSUME) {
            stack.shrink(1);
            storage.setStackInSlot(slot, stack);

            InteractionResultHolder<ItemStack> result = UseItemCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND);
            ItemStack resultItem = result.getObject();
            if(result.getResult() == InteractionResult.PASS) {
                resultItem = singleItemCopy.getItem().finishUsingItem(singleItemCopy, level, player);
            }

            if(!resultItem.isEmpty()) {
                ItemStack insertResult = InventoryHelper.addItemStackToHandler(new StorageAccessWrapper(getUpgradeManager().getWrapper(), storage), resultItem, false);
                if(!insertResult.isEmpty()) {
                    player.drop(insertResult, true);
                }
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
            return true;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
        return false;
    }

    private boolean shouldDrinkFlask(ItemStack flask) {
        boolean allowDirtyWater = getAllowDirtyWater();
        int waterPurity = flask.getOrCreateTag().getInt("purified_water");
        
        // Always allow pure water (0)
        if (waterPurity == 0) {
            return true;
        }
        
        // Allow impure/dirty water only if setting is enabled
        return allowDirtyWater;
    }

    @Override
    public boolean hasTagSelector() {
        return false; // We don't need tag filtering
    }

    @Override
    public AutoDrinkFilterSettings createFilterSettings(UpgradeManager manager, NonNullList<ItemStack> filter, List<String> filterTags) {
        return new AutoDrinkFilterSettings(manager.getWrapper().getStorage(), filter.stream().limit(getFilterSlotCount()).filter(stack -> !stack.isEmpty()).toList(), getFilter());
    }

    @Override
    protected FilterHandler createFilter(NonNullList<ItemStack> stacks, int size) {
        return new FilterHandler(stacks, size) {
            @Override
            protected void onContentsChanged(int slot) {
                updateDataHolderUnchecked(com.tiviacz.travelersbackpack.init.ModDataHelper.BACKPACK_CONTAINER, filter);
                getFilterSettings().updateFilter(com.tiviacz.travelersbackpack.util.NbtHelper.get(getDataHolderStack(), com.tiviacz.travelersbackpack.init.ModDataHelper.BACKPACK_CONTAINER));
            }
        };
    }


    // Filter system compatibility: Use 3-element structure for FilterUpgradeBase
    private static final int DIRTY_WATER_INDEX = 0; // Our setting at index 0
    private static final int ALLOW_DIRTY_VALUE = 1; // 1 = allow dirty water
    private static final int PURE_ONLY_VALUE = 0; // 0 = pure water only
    
    private static final java.util.List<Integer> DEFAULT_FILTER = java.util.List.of(
        PURE_ONLY_VALUE, // Index 0: Start with pure water only
        0,               // Index 1: Unused (required for compatibility)
        0                // Index 2: Unused (required for compatibility)
    );
    
    public java.util.List<Integer> getFilter() {
        return com.tiviacz.travelersbackpack.util.NbtHelper.getOrDefault(
            getDataHolderStack(), 
            com.tiviacz.travelersbackpack.init.ModDataHelper.FILTER_SETTINGS, 
            DEFAULT_FILTER
        );
    }
    
    public boolean getAllowDirtyWater() {
        return getFilter().get(DIRTY_WATER_INDEX) == ALLOW_DIRTY_VALUE;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public WidgetBase<BackpackScreen> createWidget(BackpackScreen screen, int x, int y) {
        return new AutoDrinkWidget(screen, this, new Point(screen.getGuiLeft() + x, screen.getGuiTop() + y));
    }

    @Override
    public List<Slot> getUpgradeSlots(BackpackBaseMenu menu, BackpackWrapper wrapper, int x, int y) {
        // No slots needed - just the toggle button
        return new ArrayList<>();
    }

    @Override
    public void remove() {
        // No special cleanup needed
    }
}