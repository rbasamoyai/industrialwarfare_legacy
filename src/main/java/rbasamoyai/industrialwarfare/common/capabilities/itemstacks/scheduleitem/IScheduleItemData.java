package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;

public interface IScheduleItemData {
	
	void setMaxMinutes(int maxMinutes);
	int getMaxMinutes();
	
	void setMaxShifts(int maxShifts);
	int getMaxShifts();
	
	void setSchedule(List<Pair<Integer, Integer>> schedule);
	List<Pair<Integer, Integer>> getSchedule();
	boolean shouldWork(int minuteOfTheWeek);
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
