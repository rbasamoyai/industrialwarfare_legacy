package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;
import rbasamoyai.industrialwarfare.common.containers.workstations.RecipeItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipe;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipeGetter;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipeWrapper;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;
import rbasamoyai.industrialwarfare.utils.IWUUIDUtils;

/**
 * Workstations that have five input slots, one output slot, and only require that a worker be present.
 */

public class NormalWorkstationTileEntity extends WorkstationTileEntity {

	public static final String TAG_RECIPE = "recipe";
	
	private static final int INPUT_SLOTS = 5;
	
	protected final RecipeItemHandler recipeItemHandler = new RecipeItemHandler(this, 1);
	
	protected NormalWorkstationRecipe workingRecipe;
	
	public NormalWorkstationTileEntity(TileEntityType<?> tileEntityTypeIn, Block workstation, int baseWorkTicks) {
		super(tileEntityTypeIn, workstation, baseWorkTicks);
		
		this.inputItemHandler.setSize(INPUT_SLOTS);
		
		this.workingRecipe = null;
	}
	
	public static NormalWorkstationTileEntity assemblerTE() {
		return new NormalWorkstationTileEntity(TileEntityTypeInit.ASSEMBLER_WORKSTATION.get(), BlockInit.ASSEMBLER_WORKSTATION.get(), 100);
	}
	
	public ItemStackHandler getRecipeItemHandler() {
		return this.recipeItemHandler;
	}
	
	public NormalWorkstationRecipe getRecipe() {
		return this.workingRecipe;
	}
	
	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag.put(TAG_RECIPE, this.recipeItemHandler.serializeNBT());
		return super.save(tag);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT tag) {
		this.recipeItemHandler.deserializeNBT(tag.getCompound(TAG_RECIPE));
		super.load(state, tag);
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return side == Direction.DOWN ? this.outputOptional.cast() : this.inputOptional.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void setRecipe(ItemStack stack, boolean dropItem) {
		ItemStack previousRecipe = this.recipeItemHandler.getStackInSlot(0);
		if (!previousRecipe.isEmpty() && dropItem) {
			ItemStack previousRecipeDrop = this.recipeItemHandler.extractItem(0, this.recipeItemHandler.getSlotLimit(0), false);
			InventoryHelper.dropItemStack(this.level, (double) this.worldPosition.getX(), this.worldPosition.above().getY(), this.worldPosition.getZ(), previousRecipeDrop);
		}
		this.recipeItemHandler.setStackInSlot(0, stack);
	}
	
	@Override
	public void nullRecipe() {
		this.workingRecipe = null;
		this.getDataHandler().ifPresent(h -> {
			h.setWorkingTicks(0);
		});
		this.setChangedAndForceUpdate();
	}
	
	@Override
	public void tick() {
		this.innerTick();
		super.tick();
	}
	
	private void innerTick() {
		if (this.clockTicks > 0) return;
		if (this.level.isClientSide) return;
		
		LazyOptional<IWorkstationDataHandler> teOptional = this.getDataHandler();
		
		LivingEntity workerEntity = this.checkForWorkerEntity();
		teOptional.ifPresent(h -> {
			h.setWorker(workerEntity);
			h.setIsWorking(workerEntity != null);
		});
		if (workerEntity == null) return;
		boolean isNPC = workerEntity instanceof NPCEntity;
		
		ItemStack recipeItem;
		
		if (this.workingRecipe == null) {
			if (isNPC) this.attemptCraft(workerEntity);
			return;
		}
		
		if (isNPC) {
			LazyOptional<INPCDataHandler> npcOptional = ((NPCEntity) workerEntity).getDataHandler();
			recipeItem = npcOptional.map(INPCDataHandler::getRecipeItem).orElse(ItemStack.EMPTY);
		} else {
			recipeItem = this.recipeItemHandler.getStackInSlot(0);
		}
		
		NormalWorkstationRecipeWrapper wrapper = new NormalWorkstationRecipeWrapper(this.inputItemHandler, recipeItem, this.workstation, Optional.of(workerEntity));
		
		if (!this.workingRecipe.matches(wrapper, this.level)) {
			this.haltCrafting();
			return;
		}
		
		if (teOptional.map(IWorkstationDataHandler::getWorkingTicks).orElse(0) < this.baseWorkTicks) return;
		
		ItemStack result = this.workingRecipe.assemble(wrapper);
		this.outputItemHandler.insertResult(0, result, false);
		this.nullRecipe(); // Reset recipe
	}
	
	@Override
	public void attemptCraft(LivingEntity entity) {
		if (this.level.isClientSide) return;
		if (!ItemStack.matches(this.outputItemHandler.getStackInSlot(0), ItemStack.EMPTY)) return;
		
		LazyOptional<IWorkstationDataHandler> optional = this.getDataHandler();
		
		LivingEntity workerEntity = this.checkForEntity(entity);
		if (workerEntity == null) {
			optional.ifPresent(h -> h.setWorker(null));
			return;
		}
		
		boolean isNPC = workerEntity instanceof NPCEntity;
		ItemStack recipeItem = isNPC
				? ((NPCEntity) workerEntity).getDataHandler().map(INPCDataHandler::getRecipeItem).orElse(ItemStack.EMPTY)
				: this.recipeItemHandler.getStackInSlot(0);
		
		NormalWorkstationRecipeWrapper wrapper = new NormalWorkstationRecipeWrapper(this.inputItemHandler, recipeItem, this.workstation, Optional.empty());
		List<NormalWorkstationRecipe> recipesForBlock = NormalWorkstationRecipeGetter.INSTANCE.getRecipes(this.level.getRecipeManager(), this.workstation);
		
		for (NormalWorkstationRecipe recipe : recipesForBlock) {
			if (!recipe.matches(wrapper, this.level)) continue;
			
			this.workingRecipe = recipe;
			this.setRecipe(recipeItem, !isNPC);
			optional.ifPresent(h -> {
				h.setWorker(entity);
				h.setIsWorking(true);
			});
			return;
		}
	}
	
	public LivingEntity checkForWorkerEntity() {
		double x1 = (double) this.worldPosition.getX() - 1;
		double y1 = (double) this.worldPosition.getY();
		double z1 = (double) this.worldPosition.getZ() - 1;
		double x2 = (double) this.worldPosition.getX() + 2;
		double y2 = (double) this.worldPosition.getY() + 1;
		double z2 = (double) this.worldPosition.getZ() + 2;
		
		AxisAlignedBB box = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
		for (LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, box)) {
			if (IWUUIDUtils.equalsFromWorkstationOptional(this.getDataHandler(), entity.getUUID())) return entity;
		}
		return null;
	}
	
	private LivingEntity checkForEntity(LivingEntity entity) {
		this.getDataHandler().ifPresent(h -> h.setWorkerUUIDOnly(entity.getUUID()));
		LivingEntity result = this.checkForWorkerEntity();
		this.getDataHandler().ifPresent(h -> h.setWorkerUUIDOnly(null));
		return result;
	}
	
}
