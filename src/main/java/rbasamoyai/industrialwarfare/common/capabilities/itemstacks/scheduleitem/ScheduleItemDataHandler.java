package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import rbasamoyai.industrialwarfare.utils.ScheduleUtils;

public class ScheduleItemDataHandler implements IScheduleItemData {
	
	private List<Pair<Integer, Integer>> schedule = new ArrayList<>(7);
	private int maxShifts = 6;
	private int maxMinutes = 70;
	
	@Override
	public void setMaxMinutes(int maxMinutes) {
		this.maxMinutes = maxMinutes;
	}

	@Override
	public int getMaxMinutes() {
		return this.maxMinutes;
	}
	
	@Override
	public void setMaxShifts(int maxShifts) {
		this.maxShifts = maxShifts;
	}
	
	@Override
	public int getMaxShifts() {
		return this.maxShifts;
	}

	@Override
	public void setSchedule(List<Pair<Integer, Integer>> schedule) {
		List<Pair<Integer, Integer>> normalized = ScheduleUtils.normalize(schedule);
		if (ScheduleUtils.getScheduleMinutes(normalized) <= this.maxMinutes) this.schedule = normalized;
	}

	@Override
	public List<Pair<Integer, Integer>> getSchedule() {
		return this.schedule;
	}

	@Override
	public boolean shouldWork(int minuteOfTheWeek) {
		return ScheduleUtils.inShift(this.schedule, minuteOfTheWeek);
	}
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.putInt("maxMinutes", this.maxMinutes);
		tag.putInt("maxShifts", this.maxShifts);
		tag.put("schedule", ScheduleUtils.toTag(this.schedule));
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		this.maxMinutes = tag.getInt("maxMinutes");
		this.maxShifts = tag.getInt("maxShifts");
		this.schedule = ScheduleUtils.fromTag(tag.getList("schedule", Tag.TAG_INT_ARRAY));
	}

}
