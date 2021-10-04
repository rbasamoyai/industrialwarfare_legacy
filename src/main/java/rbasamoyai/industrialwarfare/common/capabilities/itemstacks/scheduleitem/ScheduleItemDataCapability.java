package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.utils.ScheduleUtils;

public class ScheduleItemDataCapability {

	public static final String TAG_MAX_MINUTES = "maxMinutes";
	public static final String TAG_MAX_SHIFTS = "maxShifts";
	public static final String TAG_SCHEDULE = "schedule";
	
	@CapabilityInject(IScheduleItemDataHandler.class)
	public static Capability<IScheduleItemDataHandler> SCHEDULE_ITEM_DATA_HANDLER = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IScheduleItemDataHandler.class, new Storage(), ScheduleItemDataHandler::new);
	}
	
	public static class Storage implements IStorage<IScheduleItemDataHandler> {

		@Override
		public INBT writeNBT(Capability<IScheduleItemDataHandler> capability, IScheduleItemDataHandler instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putInt(TAG_MAX_MINUTES, instance.getMaxMinutes());
			tag.putInt(TAG_MAX_SHIFTS, instance.getMaxShifts());
			tag.put(TAG_SCHEDULE, ScheduleUtils.toNBT(instance.getSchedule()));
			return tag;
		}

		@Override
		public void readNBT(Capability<IScheduleItemDataHandler> capability, IScheduleItemDataHandler instance, Direction side, INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setMaxMinutes(tag.getInt(TAG_MAX_MINUTES));
			instance.setMaxShifts(tag.getInt(TAG_MAX_SHIFTS));
			instance.setSchedule(ScheduleUtils.fromNBT(tag.getList(TAG_SCHEDULE, Constants.NBT.TAG_INT_ARRAY)));
		}
		
	}
	
}
