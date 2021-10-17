package rbasamoyai.industrialwarfare.utils;

import net.minecraft.world.World;

public class TimeUtils {

	public static final float WEEK_MINUTES = 140.0f;
	public static final long DAY_MINUTES = 20L;
	public static final long MINUTE_TICKS = 1200L;
	public static final long WEEK_TICKS = (long) WEEK_MINUTES * MINUTE_TICKS;
	
	/**
	 * Needed as day time at 0 ticks is about 05:00 (20 hour day, 1 minute = 1 hour) / 06:00 (24 hour day, 50 seconds = 1 hour) 
	 */
	public static final long TIME_OFFSET = 6000L;
	
	public static final int getMinuteOfTheWeek(World world) {
		long time = world.getDayTime() + TIME_OFFSET;
		return (int)(time % WEEK_TICKS / MINUTE_TICKS);
	}
	
}
