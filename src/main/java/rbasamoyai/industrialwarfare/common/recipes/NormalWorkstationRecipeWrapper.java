package rbasamoyai.industrialwarfare.common.recipes;

import java.util.Optional;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class NormalWorkstationRecipeWrapper extends RecipeWrapper {

	protected final ItemStack recipeItem;
	protected final Block workstation;
	protected final Optional<? extends LivingEntity> crafterOptional;

	public NormalWorkstationRecipeWrapper(IItemHandlerModifiable inv, ItemStack recipeItem, Block workstation, Optional<? extends LivingEntity> crafterOptional) {
		super(inv);
		this.recipeItem = recipeItem;
		this.workstation = workstation;
		this.crafterOptional = crafterOptional;
	}
	
	public ItemStack getRecipeItem() {
		return this.recipeItem;
	}
	
	public Block getWorkstation() {
		return this.workstation;
	}
	
	public Optional<? extends LivingEntity> getCrafterOptional() {
		return this.crafterOptional;
	}

}
