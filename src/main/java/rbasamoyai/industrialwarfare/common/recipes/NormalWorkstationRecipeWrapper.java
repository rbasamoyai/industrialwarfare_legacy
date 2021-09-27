package rbasamoyai.industrialwarfare.common.recipes;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class NormalWorkstationRecipeWrapper extends RecipeWrapper {

	protected final IItemHandlerModifiable recipeSlot;
	protected final Block workstation;
	protected final Optional<? extends LivingEntity> crafterOptional;

	public NormalWorkstationRecipeWrapper(IItemHandlerModifiable inv, IItemHandlerModifiable recipeSlot, Block workstation, Optional<? extends LivingEntity> crafterOptional) {
		super(inv);
		this.recipeSlot = recipeSlot;
		this.workstation = workstation;
		this.crafterOptional = crafterOptional;
	}

	// A lot of the functions below only apply to the recipeSlot
	// Many are copied from the RecipeWrapper file and modified
	public ItemStack getRecipeItem() {
		return recipeSlot.getStackInSlot(0);
	}

	public ItemStack removeRecipeItem(int count) {
		ItemStack stack = this.getRecipeItem();
		return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
	}
	
	public void setRecipeItem(ItemStack stack) {
		recipeSlot.setStackInSlot(0, stack);
	}
	
	public ItemStack removeRecipeItemNoUpdate() {
		ItemStack s = this.getRecipeItem();
		if (s.isEmpty())
			return ItemStack.EMPTY;
		setRecipeItem(ItemStack.EMPTY);
		return s;
	}

	@Override
	public boolean isEmpty() {
		if (this.getRecipeItem().isEmpty()) return true;
		for (int i = 0; i < inv.getSlots(); i++) {
			if (!inv.getStackInSlot(i).isEmpty())
				return false;
		}
		return true;
	}

	public boolean canPlaceRecipeItem(ItemStack stack) {
		return recipeSlot.isItemValid(0, stack);
	}

	public void clearRecipeItemContent() {
		recipeSlot.setStackInSlot(0, ItemStack.EMPTY);
	}
	
	public Block getWorkstation() {
		return this.workstation;
	}
	
	public Optional<? extends LivingEntity> getCrafterOptional() {
		return this.crafterOptional;
	}

}
