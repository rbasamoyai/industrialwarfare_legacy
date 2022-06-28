package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.npccombatskill.NPCCombatSkill;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;

public interface INPCData {
	
	void setFirstOwner(PlayerIDTag firstOwnerUUID);
	PlayerIDTag getFirstOwner();
	
	void setOwner(PlayerIDTag newOwnerUUID);
	PlayerIDTag getOwner();
	
	void setProfession(NPCProfession profession);
	NPCProfession getProfession();
	
	void setCombatSkill(NPCCombatSkill skill);
	NPCCombatSkill getCombatSkill();
	
	void setCanWearEquipment(boolean canWearEquipment);
	boolean canWearEquipment();
	
	void setRecipeItem(ItemStack recipe);
	ItemStack getRecipeItem();
	
	void setSkill(float skill);
	float getSkill();
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
