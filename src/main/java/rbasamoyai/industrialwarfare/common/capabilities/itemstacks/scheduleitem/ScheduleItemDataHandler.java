package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import rbasamoyai.industrialwarfare.utils.ScheduleUtils;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

public class ScheduleItemDataHandler implements IScheduleItemDataHandler {
	
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
	public boolean shouldWork(long gameTime) {
		final int minuteOfTheWeek = (int)(gameTime % TimeUtils.WEEK_TICKS / TimeUtils.MINUTE_TICKS);
		return ScheduleUtils.inShift(this.schedule, minuteOfTheWeek);
	}

}
