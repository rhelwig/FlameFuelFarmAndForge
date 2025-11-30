package com.ronhelwig.ffff.blockentity;

import static com.ronhelwig.ffff.FlameFuelFarmAndForge.MOD_ID;

import com.ronhelwig.ffff.menu.LanternMenu;
import com.ronhelwig.ffff.registry.ModBlockEntities;
import com.ronhelwig.ffff.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class LanternBlockEntity extends RandomizableContainerBlockEntity implements Container {
	public static final int SLOT_FUEL = 0;
	private static final int INVENTORY_SIZE = 1;
	private static final int MAX_STACK_SIZE = 16;
	private static final long FUEL_TICKS = 24000L;

	private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	private long lastFuelCheckGameTime = -1L;
	private boolean playerPlaced;
	private boolean initialized;
	private boolean lastLit;
	private BooleanProperty litProperty;

	public LanternBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LANTERN, pos, state);
		litProperty = resolveLitProperty(state);
	}

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public int getMaxStackSize() {
		return MAX_STACK_SIZE;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot != SLOT_FUEL) {
			return false;
		}

		return stack.is(ModItems.ANIMAL_FAT) || stack.is(ModItems.OIL_BOTTLE);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container." + MOD_ID + ".lantern");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
		return new LanternMenu(syncId, playerInventory, this);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		lastFuelCheckGameTime = input.getLongOr("FuelCheckTime", -1L);
		playerPlaced = input.getBooleanOr("PlayerPlaced", false);
		lastLit = input.getBooleanOr("LastLit", hasFuel());
		litProperty = resolveLitProperty(getBlockState());
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items, true);
		output.putLong("FuelCheckTime", lastFuelCheckGameTime);
		output.putBoolean("PlayerPlaced", playerPlaced);
		output.putBoolean("LastLit", isLit());
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot != SLOT_FUEL) {
			return;
		}

		if (!stack.isEmpty() && !(stack.is(ModItems.ANIMAL_FAT) || stack.is(ModItems.OIL_BOTTLE))) {
			return;
		}

		ItemStack clamped = stack.copy();
		if (clamped.getCount() > MAX_STACK_SIZE) {
			clamped.setCount(MAX_STACK_SIZE);
		}

		items.set(slot, clamped);
		if (!clamped.isEmpty() && level != null) {
			lastFuelCheckGameTime = level.getGameTime();
		}
		setChanged();
		triggerLightUpdateIfNeeded();
	}

	public void markPlayerPlaced() {
		if (!playerPlaced) {
			playerPlaced = true;
			setChanged();
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, LanternBlockEntity lantern) {
		if (level.isClientSide()) {
			return;
		}

		lantern.ensureInitialized(level);

		long gameTime = level.getGameTime();
		if (lantern.lastFuelCheckGameTime < 0L) {
			lantern.lastFuelCheckGameTime = gameTime;
		}

		if (!lantern.hasFuel()) {
			lantern.triggerLightUpdateIfNeeded();
			return;
		}

		long elapsed = gameTime - lantern.lastFuelCheckGameTime;
		long unitTicks = lantern.getFuelUnitTicks();
		if (elapsed < unitTicks) {
			return;
		}

		int toConsume = (int) (elapsed / unitTicks);
		lantern.consumeFuel(toConsume);
		lantern.lastFuelCheckGameTime = gameTime - (elapsed % unitTicks);
		lantern.triggerLightUpdateIfNeeded();
	}

	private void ensureInitialized(Level level) {
		if (initialized) {
			return;
		}
		initialized = true;
		if (!playerPlaced && !hasFuel()) {
			items.set(SLOT_FUEL, new ItemStack(ModItems.ANIMAL_FAT, MAX_STACK_SIZE));
			setChanged();
		}
		lastFuelCheckGameTime = level.getGameTime();
		triggerLightUpdateIfNeeded();
	}

	private void consumeFuel(int amount) {
		if (amount <= 0) {
			return;
		}

		ItemStack stack = items.get(SLOT_FUEL);
		if (stack.isEmpty()) {
			return;
		}

		int actualConsume = Math.min(amount, stack.getCount());
		if (stack.is(ModItems.OIL_BOTTLE)) {
			Level level = getLevel();
			if (level != null && !level.isClientSide()) {
				ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, actualConsume);
				double dropX = worldPosition.getX() + 0.5;
				double dropY = worldPosition.getY() + 0.5;
				double dropZ = worldPosition.getZ() + 0.5;
				ItemEntity bottleEntity = new ItemEntity(level, dropX, dropY, dropZ, bottles);
				bottleEntity.setDefaultPickUpDelay();
				bottleEntity.setUnlimitedLifetime();
				level.addFreshEntity(bottleEntity);
			}
		}

		int newCount = Math.max(0, stack.getCount() - actualConsume);
		if (newCount <= 0) {
			items.set(SLOT_FUEL, ItemStack.EMPTY);
		} else {
			stack.setCount(newCount);
			items.set(SLOT_FUEL, stack);
		}
		setChanged();
	}

	public boolean hasFuel() {
		ItemStack stack = items.get(SLOT_FUEL);
		return !stack.isEmpty() && (stack.is(ModItems.ANIMAL_FAT) || stack.is(ModItems.OIL_BOTTLE)) && stack.getCount() > 0;
	}

	private long getFuelUnitTicks() {
		ItemStack stack = items.get(SLOT_FUEL);
		if (!stack.isEmpty() && stack.is(ModItems.OIL_BOTTLE)) {
			return FUEL_TICKS * 2;
		}
		return FUEL_TICKS;
	}

	private boolean isLit() {
		return hasFuel();
	}

	private void triggerLightUpdateIfNeeded() {
		boolean currentLit = isLit();
		if (currentLit == lastLit) {
			return;
		}

		lastLit = currentLit;
		Level level = getLevel();
		if (level != null) {
			BlockState state = getBlockState();
			if (litProperty != null && state.hasProperty(litProperty) && state.getValue(litProperty) != currentLit) {
				BlockState updated = state.setValue(litProperty, currentLit);
				level.setBlock(worldPosition, updated, Block.UPDATE_ALL | Block.UPDATE_INVISIBLE);
			} else {
				level.getChunkSource().getLightEngine().checkBlock(worldPosition);
				level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
			}
		}
	}

	public void dropContents() {
		if (level == null || level.isClientSide()) {
			return;
		}
		Containers.dropContents(level, worldPosition, this);
	}

	private BooleanProperty resolveLitProperty(BlockState state) {
		for (var property : state.getProperties()) {
			if (property instanceof BooleanProperty booleanProperty && "lit".equals(booleanProperty.getName())) {
				return booleanProperty;
			}
		}
		return null;
	}
}
