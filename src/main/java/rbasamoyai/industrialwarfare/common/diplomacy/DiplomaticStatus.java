package rbasamoyai.industrialwarfare.common.diplomacy;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Taken from Age of Empires.
 * 
 * @author rbasamoyai
 */
public enum DiplomaticStatus {

	UNKNOWN((byte) 0, "unknown", Style.EMPTY), // Not from AoE
	ALLY((byte) 1, "ally", Style.EMPTY.withColor(TextColor.fromRgb(0x004db512))),
	NEUTRAL((byte) 2, "neutral", Style.EMPTY.withColor(TextColor.fromRgb(0x00ffd400))),
	ENEMY((byte) 3, "enemy", Style.EMPTY.withColor(TextColor.fromRgb(0x00ff1427)));
	
	private static final DiplomaticStatus[] BY_ID = Arrays.stream(values())
			.sorted(Comparator.comparingInt(DiplomaticStatus::getValue))
			.toArray(sz -> new DiplomaticStatus[sz]);
	
	private final byte value;
	private final String name;
	private final Style style;
	
	private DiplomaticStatus(byte value, String name, Style style) {
		this.value = value;
		this.name = name;
		this.style = style;
	}
	
	public byte getValue() { return this.value; }
	public String getName() { return this.name; }
	public Style getStyle() { return this.style; }
	public static DiplomaticStatus fromValue(byte value) { return BY_ID[value]; }
	
	public static DiplomaticStatus getDefault(boolean isPlayer) {
		return isPlayer ? NEUTRAL : UNKNOWN;
	}
	
}
