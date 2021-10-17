package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.core.init.ItemInit;

public class NPCDataHandler implements INPCDataHandler {
	
	private UUID firstOwnerUUID;
	private UUID currentOwnerUUID;
	private NPCProfession profession;
	private boolean canWearEquipment;
	private ItemStack recipeItem;
	private float skill;
	
	@Override public void setFirstOwnerUUID(UUID firstOwnerUUID) { this.firstOwnerUUID = firstOwnerUUID; }
	@Override public UUID getFirstOwnerUUID() { return this.firstOwnerUUID; }

	@Override public void setOwnerUUID(UUID newOwnerUUID) { this.currentOwnerUUID = newOwnerUUID; }
	@Override public UUID getOwnerUUID() { return this.currentOwnerUUID; }

	@Override public void setProfession(NPCProfession profession) { this.profession = profession; }
	@Override public NPCProfession getProfession() { return this.profession; }

	@Override public void setCanWearEquipment(boolean canWearEquipment) { this.canWearEquipment = canWearEquipment; }
	@Override public boolean canWearEquipment() { return this.canWearEquipment; }
	
	@Override public void setRecipeItem(ItemStack recipe) { 
		if (recipe.getItem() != ItemInit.RECIPE_MANUAL) return;
		this.recipeItem = recipe;
	}	
	@Override public ItemStack getRecipeItem() { return this.recipeItem == null ? ItemStack.EMPTY : this.recipeItem; }

	@Override public void setSkill(float skill) { this.skill = skill; }
	@Override public float getSkill() { return this.skill; }
	
}
