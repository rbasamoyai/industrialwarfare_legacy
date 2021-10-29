package rbasamoyai.industrialwarfare.common.diplomacy;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Taken from Age of Empires.
 * 
 * @author rbasamoyai
 */
public enum DiplomaticStatus {

	UNKNOWN((byte) 0, "unknown"), // Not from AoE
	ALLY((byte) 1, "ally"),
	NEUTRAL((byte) 2, "neutral"),
	ENEMY((byte) 3, "enemy");
	
	private static final DiplomaticStatus[] BY_ID = Arrays.stream(values())
			.sorted(Comparator.comparingInt(DiplomaticStatus::getValue))
			.toArray(sz -> new DiplomaticStatus[sz]);
	
	private final byte value;
	private final String name;
	
	private DiplomaticStatus(byte value, String name) {
		this.value = value;
		this.name = name;
	}
	
	public byte getValue() { return this.value; }
	public String getName() { return this.name; }
	public static DiplomaticStatus fromValue(byte value) { return BY_ID[value]; }
	
}
