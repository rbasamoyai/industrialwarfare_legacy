package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import java.util.List;

import com.mojang.datafixers.util.Pair;

public interface IScheduleItemDataHandler {

	public void setMaxMinutes(int maxMinutes);
	public int getMaxMinutes();
	
	public void setSchedule(List<Pair<Integer, Integer>> schedule);
	public List<Pair<Integer, Integer>> getSchedule();
	public boolean shouldWork(long gameTime);
	
}
