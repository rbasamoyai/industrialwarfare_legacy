package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;

public interface INPCDataHandler {
	
	public void setFirstOwner(PlayerIDTag firstOwnerUUID);
	public PlayerIDTag getFirstOwner();
	
	public void setOwner(PlayerIDTag newOwnerUUID);
	public PlayerIDTag getOwner();
	
	public void setProfession(NPCProfession profession);
	public NPCProfession getProfession();
	
	public void setCanWearEquipment(boolean canWearEquipment);
	public boolean canWearEquipment();
	
	public void setRecipeItem(ItemStack recipe);
	public ItemStack getRecipeItem();
	
	public void setSkill(float skill);
	public float getSkill();
	
}
