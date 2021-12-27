package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.npccombatskill.NPCCombatSkill;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class NPCDataHandler implements INPCDataHandler {
	
	private PlayerIDTag firstOwner;
	private PlayerIDTag currentOwner;
	private NPCProfession profession;
	private NPCCombatSkill combatSkill;
	private boolean canWearEquipment;
	private ItemStack recipeItem;
	private float skill;
	
	@Override public void setFirstOwner(PlayerIDTag firstOwner) { this.firstOwner = firstOwner; }
	@Override public PlayerIDTag getFirstOwner() { return this.firstOwner; }

	@Override
	public void setOwner(PlayerIDTag newOwner) {
		if (this.firstOwner == null || this.firstOwner.equals(PlayerIDTag.NO_OWNER)) {
			this.setFirstOwner(newOwner);
		}
		this.currentOwner = newOwner;
	}
	@Override public PlayerIDTag getOwner() { return this.currentOwner; }

	@Override public void setProfession(NPCProfession profession) { this.profession = profession; }
	@Override public NPCProfession getProfession() { return this.profession; }
	
	@Override public void setCombatSkill(NPCCombatSkill skill) { this.combatSkill = skill; }
	@Override public NPCCombatSkill getCombatSkill() { return this.combatSkill; }

	@Override public void setCanWearEquipment(boolean canWearEquipment) { this.canWearEquipment = canWearEquipment; }
	@Override public boolean canWearEquipment() { return this.canWearEquipment; }
	
	@Override public void setRecipeItem(ItemStack recipe) { 
		if (recipe.getItem() != ItemInit.RECIPE_MANUAL.get()) return;
		this.recipeItem = recipe;
	}	
	@Override public ItemStack getRecipeItem() { return this.recipeItem == null ? ItemStack.EMPTY : this.recipeItem; }

	@Override public void setSkill(float skill) { this.skill = skill; }
	@Override public float getSkill() { return this.skill; }
	
}
