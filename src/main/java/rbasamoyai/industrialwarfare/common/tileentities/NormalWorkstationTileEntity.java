package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;
import rbasamoyai.industrialwarfare.common.containers.workstations.RecipeItemHandler;
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
		return new NormalWorkstationTileEntity(TileEntityTypeInit.ASSEMBLER_WORKSTATION, BlockInit.ASSEMBLER_WORKSTATION, 100);
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
			if (side == Direction.UP) return this.inputOptional.cast();
			else if (side == Direction.DOWN) return this.outputOptional.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void nullRecipe() {
		this.workingRecipe = null;
		this.getDataHandler().ifPresent(h -> {
			h.setWorker(null);
			h.setWorkingTicks(0);
		});
		this.setChangedAndForceUpdate();
	}
	
	@Override
	public void tick() {
		if (this.clockTicks <= 0) {
			if (!this.level.isClientSide) {
				LazyOptional<IWorkstationDataHandler> optional = this.getDataHandler();
				
				NormalWorkstationRecipeWrapper matchWrapper = new NormalWorkstationRecipeWrapper(this.inputItemHandler, this.recipeItemHandler, this.workstation, Optional.empty());
				
				if (this.workingRecipe != null) {
					if (!this.workingRecipe.matches(matchWrapper, this.level)) this.haltCrafting();
				}
				
				LivingEntity workerEntity = checkForWorkerEntity();
				optional.ifPresent(h -> h.setWorker(workerEntity));
				
				// TODO: Implement code if worker not found
				if (workerEntity == null) {
					
				} else {
					optional.ifPresent(h -> h.setIsWorking(true));
				}
				
				if (optional.map(IWorkstationDataHandler::getWorkingTicks).orElse(0) >= this.baseWorkTicks && workerEntity != null && this.workingRecipe != null) {
					NormalWorkstationRecipeWrapper assemblyWrapper = new NormalWorkstationRecipeWrapper(this.recipeItemHandler, this.recipeItemHandler, this.workstation, Optional.of(workerEntity));
					ItemStack result = this.workingRecipe.assemble(assemblyWrapper);
					this.outputItemHandler.insertResult(0, result, false);
					// Reset recipe and see if we can try again
					this.nullRecipe();
					this.attemptCraft(workerEntity);
				}
			}
		}
		super.tick();
	}
	
	@Override
	public void attemptCraft(LivingEntity entity) {
		if (!ItemStack.matches(this.outputItemHandler.getStackInSlot(0), ItemStack.EMPTY)) return;
		if (!this.level.isClientSide) {
			LazyOptional<IWorkstationDataHandler> optional = this.getDataHandler();
			
			LivingEntity workerEntity = checkForEntity(entity);
			if (workerEntity == null) {
				optional.ifPresent(h -> h.setWorker(null));
				return;
			}
			
			NormalWorkstationRecipeWrapper wrapper = new NormalWorkstationRecipeWrapper(this.inputItemHandler, this.recipeItemHandler, this.workstation, Optional.empty());
			List<NormalWorkstationRecipe> recipesForBlock = NormalWorkstationRecipeGetter.INSTANCE.getRecipes(this.level.getRecipeManager(), this.workstation);
			for (NormalWorkstationRecipe recipe : recipesForBlock) {
				if (recipe.matches(wrapper, this.level)) {
					this.workingRecipe = recipe;
					optional.ifPresent(h -> h.setWorker(entity));
					break;
				}
			}
		}
	}
	
	private LivingEntity checkForWorkerEntity() {
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
