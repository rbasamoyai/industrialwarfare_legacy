package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.npccombatskill.NPCCombatSkill;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class NPCDataHandler implements INPCData {
	
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
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.putBoolean("canWearEquipment", this.canWearEquipment());
		tag.putString("combatSkill", this.getCombatSkill().getRegistryName().toString());
		tag.putString("profession", this.getProfession().getRegistryName().toString());
		tag.put("firstOwner", this.getFirstOwner().serializeNBT());
		tag.put("currentOwner", this.getOwner().serializeNBT());
		ItemStack recipeItem = this.getRecipeItem();
		tag.put("knownRecipe", recipeItem != null ? recipeItem.serializeNBT() : new CompoundTag());
		tag.putFloat("currentRecipeSkill", this.getSkill());
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		this.setCanWearEquipment(tag.getBoolean("canWearEquipment"));
		this.setCombatSkill(IWModRegistries.NPC_COMBAT_SKILLS.get().getValue(new ResourceLocation(tag.getString("combatSkill"))));
		this.setProfession(IWModRegistries.NPC_PROFESSIONS.get().getValue(new ResourceLocation(tag.getString("profession"))));
		this.setFirstOwner(PlayerIDTag.fromNBT(tag.getCompound("firstOwner")));
		this.setOwner(PlayerIDTag.fromNBT(tag.getCompound("currentOwner")));
		this.setRecipeItem(ItemStack.of(tag.getCompound("knownRecipe")));
		this.setSkill(tag.getFloat("currentRecipeSkill"));
	}
	
}
