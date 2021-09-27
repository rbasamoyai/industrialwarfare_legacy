package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import java.util.UUID;

public class NPCDataHandler implements INPCDataHandler {
	
	private UUID firstOwnerUUID;
	private UUID currentOwnerUUID;
	private String occupation;
	private boolean canWearEquipment;
	
	@Override
	public void setFirstOwnerUUID(UUID firstOwnerUUID) {
		this.firstOwnerUUID = firstOwnerUUID;
	}

	@Override
	public UUID getFirstOwnerUUID() {
		return this.firstOwnerUUID;
	}

	@Override
	public void setOwnerUUID(UUID newOwnerUUID) {
		this.currentOwnerUUID = newOwnerUUID;
	}

	@Override
	public UUID getOwnerUUID() {
		return this.currentOwnerUUID;
	}

	@Override
	public void setOccupation(String newOccupation) {
		this.occupation = newOccupation;
	}

	@Override
	public String getOccupation() {
		return this.occupation;
	}

	@Override
	public void setCanWearEquipment(boolean canWearEquipment) {
		this.canWearEquipment = canWearEquipment;
	}

	@Override
	public boolean getCanWearEquipment() {
		return this.canWearEquipment;
	}

}
