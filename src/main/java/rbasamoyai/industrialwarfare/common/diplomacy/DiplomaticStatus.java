package rbasamoyai.industrialwarfare.common.diplomacy;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Taken from Age of Empires.
 * 
 * @author rbasamoyai
 */
public enum DiplomaticStatus {

	UNKNOWN((byte) 0), // Not from AoE
	ALLY((byte) 1),
	NEUTRAL((byte) 2),
	ENEMY((byte) 3);
	
	private static final DiplomaticStatus[] BY_ID = Arrays.stream(values())
			.sorted(Comparator.comparingInt(DiplomaticStatus::getValue))
			.toArray(sz -> new DiplomaticStatus[sz]);
	
	private final byte value;
	
	private DiplomaticStatus(byte value) {
		this.value = value;
	}
	
	public byte getValue() { return this.value; }
	public static DiplomaticStatus fromValue(byte value) { return BY_ID[value]; }
	
}
