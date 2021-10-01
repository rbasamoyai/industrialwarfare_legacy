package rbasamoyai.industrialwarfare.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;

public class ScheduleUtils {

	public static List<Pair<Integer, Integer>> normalize(List<Pair<Integer, Integer>> schedule) {
		return schedule
				.stream()
				.map(shift -> shift.getFirst() > shift.getSecond() ? Pair.of(shift.getSecond(), shift.getFirst()) : shift )
				.collect(Collectors.toList());
	}
	
	public static int getScheduleMinutes(List<Pair<Integer, Integer>> schedule) {
		return schedule.stream()
				.map(shift -> shift.getSecond() - shift.getFirst())
				.reduce(Integer::sum)
				.orElse(0);
	}
	
	public static ListNBT toNBT(List<Pair<Integer, Integer>> schedule) {
		ListNBT tag = new ListNBT();
		schedule.forEach(shift -> tag.add(new IntArrayNBT(new int[] {shift.getFirst(), shift.getSecond()})));
		return tag;
	}
	
	public static List<Pair<Integer, Integer>> fromNBT(ListNBT tag) {
		List<Pair<Integer, Integer>> schedule = new ArrayList<>(tag.size());
		for (int i = 0; i < tag.size(); i++) {
			int[] shift = tag.getIntArray(i);
			if (shift.length != 2)
				throw new IllegalArgumentException("ListNBT passed has invalid tag at index " + i + ", should be an IntArrayNBT of length 2");
			schedule.add(Pair.of(shift[0], shift[1]));
		}
		return schedule;
	}
	
}
