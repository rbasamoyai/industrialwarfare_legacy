package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;

public interface INPCDataHandler {
	
	public void setFirstOwnerUUID(UUID firstOwnerUUID);
	public UUID getFirstOwnerUUID();
	
	public void setOwnerUUID(UUID newOwnerUUID);
	public UUID getOwnerUUID();
	
	public void setProfession(NPCProfession profession);
	public NPCProfession getProfession();
	
	public void setCanWearEquipment(boolean canWearEquipment);
	public boolean canWearEquipment();
	
	public void setRecipeItem(ItemStack recipe);
	public ItemStack getRecipeItem();
	
	public void setSkill(float skill);
	public float getSkill();
	
}
