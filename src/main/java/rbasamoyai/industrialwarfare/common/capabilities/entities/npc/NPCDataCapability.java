package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class NPCDataCapability {
	
	private static final String TAG_FIRST_OWNER = "firstOwnerUUID";
	private static final String TAG_CURRENT_OWNER = "currentOwnerUUID";
	private static final String TAG_OCCUPATION = "occupation";
	private static final String TAG_CAN_WEAR_EQUIPMENT = "canWearEquipment";
	
	@CapabilityInject(INPCDataHandler.class)
	public static Capability<INPCDataHandler> NPC_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(INPCDataHandler.class, new Storage(), NPCDataHandler::new);
	}
	
	public static class Storage implements IStorage<INPCDataHandler> {

		@Nullable
		@Override
		public INBT writeNBT(Capability<INPCDataHandler> capability, INPCDataHandler instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putBoolean(TAG_CAN_WEAR_EQUIPMENT, instance.getCanWearEquipment());
			tag.putString(TAG_OCCUPATION, instance.getOccupation());
			tag.putUUID(TAG_FIRST_OWNER, instance.getFirstOwnerUUID());
			tag.putUUID(TAG_CURRENT_OWNER, instance.getOwnerUUID());
			return tag;
		}

		@Override
		public void readNBT(Capability<INPCDataHandler> capability, INPCDataHandler instance, Direction side,
				INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setCanWearEquipment(tag.getBoolean(TAG_CAN_WEAR_EQUIPMENT));
			instance.setOccupation(tag.getString(TAG_OCCUPATION));
			instance.setFirstOwnerUUID(tag.getUUID(TAG_FIRST_OWNER));
			instance.setOwnerUUID(tag.getUUID(TAG_CURRENT_OWNER));
		}
		
	}
	
}
