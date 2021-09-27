package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import java.util.UUID;

public interface INPCDataHandler {
	
	public void setFirstOwnerUUID(UUID firstOwnerUUID);
	public UUID getFirstOwnerUUID();
	
	public void setOwnerUUID(UUID newOwnerUUID);
	public UUID getOwnerUUID();
	
	public void setOccupation(String newOccupation);
	public String getOccupation();
	
	public void setCanWearEquipment(boolean canWearEquipment);
	public boolean getCanWearEquipment();
	
}
