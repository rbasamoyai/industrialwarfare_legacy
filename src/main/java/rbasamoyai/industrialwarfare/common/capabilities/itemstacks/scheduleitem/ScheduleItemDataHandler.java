package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import rbasamoyai.industrialwarfare.utils.ScheduleUtils;

public class ScheduleItemDataHandler implements IScheduleItemDataHandler {

	private static final long WEEK_TICKS = 168000L;
	private static final long MINUTE_TICKS = 1200L;
	
	private List<Pair<Integer, Integer>> schedule = new ArrayList<>(7);
	private int maxShifts = 6;
	private int maxMinutes = 140;
	
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
		final int minuteOfTheWeek = (int)(gameTime % WEEK_TICKS / MINUTE_TICKS);
		return schedule.stream()
				.filter(shift -> shift.getFirst() <= minuteOfTheWeek && minuteOfTheWeek < shift.getSecond())
				.findAny()
				.isPresent();
	}

}
