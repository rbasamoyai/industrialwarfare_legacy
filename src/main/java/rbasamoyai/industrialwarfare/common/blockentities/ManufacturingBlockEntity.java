package rbasamoyai.industrialwarfare.common.blockentities;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCData;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.containers.workstations.RecipeItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.recipes.ManufactureRecipe;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipeGetter;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipeWrapper;
import rbasamoyai.industrialwarfare.core.config.IWConfig;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;

/*
 * Base workstation tile entity class
 */

public class ManufacturingBlockEntity extends BlockEntity {
	
	public static final int UPDATE_TICKS = IWConfig.workstation_update.get() > 0 ? IWConfig.workstation_update.get() : 10;
	
	protected final ItemStackHandler inputItemHandler = new UpdateBlockEntityItemHandler(5, this);
	protected final ItemStackHandler outputItemHandler = new UpdateBlockEntityItemHandler(1, this);
	protected final ItemStackHandler recipeItemHandler = new RecipeItemHandler(1, this);
	
	protected final LazyOptional<IItemHandler> inputOptional = LazyOptional.of(this::getInputItemHandler);
	protected final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(this::getOutputItemHandler);
	
	protected final int baseWorkTicks;
	
	private int clockTicks = 0;
	protected boolean forceUpdate = false;
	protected int workingTicks = 0;
	private LivingEntity worker = null;
	protected ManufactureRecipe workingRecipe = null;
	protected boolean isWorking = false;
	
	public ManufacturingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int baseWorkTicks) {
		super(type, pos, state);
		this.baseWorkTicks = baseWorkTicks;
	}
	
	public IItemHandler getInputItemHandler() { return this.inputItemHandler; }
	public IItemHandler getOutputItemHandler() { return this.outputItemHandler; }
	public IItemHandler getRecipeItemHandler() { return this.recipeItemHandler; }
	
	public int getBaseWorkTicks() { return this.baseWorkTicks; }
	public int getWorkingTicks() { return this.workingTicks; }	
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		this.inputOptional.invalidate();
		this.outputOptional.invalidate();
	}
	
	public void setChangedAndForceUpdate() {
		this.setChanged();
		this.forceUpdate = true;
	}
	
	@Override
	public void setChanged() {
		this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
		super.setChanged();
	}
	
	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("input", this.inputItemHandler.serializeNBT());
		tag.put("output", this.outputItemHandler.serializeNBT());
		tag.put("recipe", this.recipeItemHandler.serializeNBT());
		if (this.worker != null) {
			tag.putUUID("currentWorker", this.worker.getUUID());
		}
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.inputItemHandler.deserializeNBT(tag.getCompound("input"));
		this.outputItemHandler.deserializeNBT(tag.getCompound("output"));
		this.recipeItemHandler.deserializeNBT(tag.getCompound("recipe"));
		if (this.level instanceof ServerLevel && tag.contains("currentWorker", Tag.TAG_INT_ARRAY)) {
			Entity entity = ((ServerLevel) this.level).getEntity(tag.getUUID("currentWorker"));
			tag.remove("currentWorker");
			if (entity instanceof LivingEntity) this.worker = (LivingEntity) entity;
		}
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return side == Direction.DOWN ? this.outputOptional.cast() : this.inputOptional.cast();
		}
		return super.getCapability(cap, side);
	}
	
	public void attemptCraft(LivingEntity entity) {
		if (this.level.isClientSide) return;
		if (!ItemStack.matches(this.outputItemHandler.getStackInSlot(0), ItemStack.EMPTY)) return;
		
		AABB box = new AABB(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(2, 3, 2));
		if (!this.level.getEntities(null, box).contains(entity)) {
			this.worker = null;
			return;
		}
		ItemStack recipeItem = entity.getCapability(NPCDataCapability.INSTANCE)
				.map(INPCData::getRecipeItem)
				.orElseGet(() -> this.recipeItemHandler.getStackInSlot(0));
		
		Block block = this.getBlockState().getBlock();
		NormalWorkstationRecipeWrapper wrapper = new NormalWorkstationRecipeWrapper(this.inputItemHandler, recipeItem, block, Optional.empty());
		List<ManufactureRecipe> recipesForBlock = NormalWorkstationRecipeGetter.INSTANCE.getRecipes(this.level.getRecipeManager(), block);
		
		for (ManufactureRecipe recipe : recipesForBlock) {
			if (!recipe.matches(wrapper, this.level)) continue;
			
			this.workingRecipe = recipe;
			this.setRecipe(recipeItem, !(entity instanceof NPCEntity));
			this.worker = entity;
			this.isWorking = true;
			return;
		}
	}
	
	public void setRecipe(ItemStack stack, boolean dropItem) {
		ItemStack previousRecipe = this.recipeItemHandler.getStackInSlot(0);
		if (ItemStack.matches(stack, previousRecipe)) return;
		if (!previousRecipe.isEmpty() && dropItem) {
			ItemStack previousRecipeDrop = this.recipeItemHandler.extractItem(0, this.recipeItemHandler.getSlotLimit(0), false);
			Containers.dropItemStack(this.level, (double) this.worldPosition.getX(), this.worldPosition.above().getY(), this.worldPosition.getZ(), previousRecipeDrop);
		}
		this.recipeItemHandler.setStackInSlot(0, stack);
	}
	
	public void haltCrafting() {
		this.worker = null;
		this.workingRecipe = null;
		this.workingTicks = 0;
		this.setChanged();
		this.forceUpdate = true;
	}

	public void onPlayerCloseScreen(Player player) {
		if (this.level.isClientSide || !player.is(this.worker)) return;
		this.haltCrafting();
	}
	
	public boolean isCurrentWorkerInArea() {
		if (this.worker == null) return false;
		AABB box = new AABB(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(2, 3, 2));
		return this.level.getEntities(null, box).contains(this.worker);
	}
	
	public boolean hasWorker() { return this.worker != null; }
	public void setWorker(LivingEntity worker) { this.worker = worker; }
	public boolean isSameWorker(LivingEntity entity) { return entity.is(this.worker); }
	
	private void innerTick() {
		if (this.level.isClientSide) return;
		
		if (this.isWorking && !this.isCurrentWorkerInArea()) {
			this.worker = null;
			this.isWorking = false;
			this.workingTicks = 0;
			this.setChanged();
			this.forceUpdate = true;
			return;
		}
		this.isWorking = true;
		
		if (this.workingRecipe == null) {
			this.attemptCraft(this.worker);
			return;
		}
		
		ItemStack recipeItem = this.worker.getCapability(NPCDataCapability.INSTANCE).map(INPCData::getRecipeItem).orElseGet(() -> this.recipeItemHandler.getStackInSlot(0));
		NormalWorkstationRecipeWrapper wrapper = new NormalWorkstationRecipeWrapper(this.inputItemHandler, recipeItem, this.getBlockState().getBlock(), Optional.of(this.worker));
		
		if (!this.workingRecipe.matches(wrapper, this.level)) {
			this.haltCrafting();
			return;
		}
		
		if (this.workingTicks < this.baseWorkTicks) return;
		
		ItemStack result = this.workingRecipe.assemble(wrapper);
		this.outputItemHandler.insertItem(0, result, false);
		this.workingRecipe = null;
		this.workingTicks = 0;
		this.isWorking = false;
		this.setChanged();
		this.forceUpdate = true;
	}
	
	public static void serverTick(Level level, BlockPos pos, BlockState state, ManufacturingBlockEntity be) {
		++be.clockTicks;
		if (be.clockTicks >= UPDATE_TICKS || be.forceUpdate) {
			be.clockTicks = 0;
			be.forceUpdate = false;
		}
		if (!be.level.isClientSide) {	
			be.innerTick();
			
			if (be.isWorking && be.worker != null && be.workingRecipe != null && be.workingTicks < be.baseWorkTicks) {
				++be.workingTicks;
			}
		}
		be.setChanged();
	}
	
	public static ManufacturingBlockEntity assembler(BlockPos pos, BlockState state) {
		return new ManufacturingBlockEntity(BlockEntityTypeInit.ASSEMBLER_WORKSTATION.get(), pos, state, 100);
	}
	
}
