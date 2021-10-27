package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public enum WaitMode {
	
	DAY_TIME(0, "day_time"),
	RELATIVE_TIME(1, "relative_time"),
	HEARD_BELL(2, "heard_bell");
	
	private final int id;
	private final String name;
	
	private static final WaitMode[] VALUES = values();
	private static final WaitMode[] BY_ID = Arrays.stream(VALUES).sorted(Comparator.comparingInt(WaitMode::getId)).toArray(sz -> new WaitMode[sz]);
	private static final Map<String, WaitMode> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(WaitMode::getName, wm -> wm));
	
	private WaitMode(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() { return this.id; }
	public String getName() { return this.name; }
	
	@Override
	public String toString() {
		return this.id + " " + this.name;
	}
	
	public static WaitMode fromId(int id) { return BY_ID[id]; }
	public static WaitMode fromName(String name) { return BY_NAME.get(name); }
}
