package rbasamoyai.industrialwarfare.common.entityai;

import java.util.Arrays;
import java.util.Comparator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum CombatMode {
	ATTACK(0, "attack"),
	DEFEND(1, "defend"),
	STAND_GROUND(2, "stand_ground"),
	DONT_ATTACK(3, "dont_attack");
	
	public static final Codec<CombatMode> CODEC = Codec.INT.comapFlatMap(CombatMode::read, CombatMode::getId).stable();
	
	private static final CombatMode[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(CombatMode::getId)).toArray(sz -> new CombatMode[sz]);
	
	private final int id;
	private final String name;
	
	private CombatMode(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public CombatMode next() {
		return fromId(this.id + 1);
	}
	
	public int getId() { return this.id; }
	public static CombatMode fromId(int id) { return id >= 0 && id < BY_ID.length ? BY_ID[id] : ATTACK; }
	
	@Override public String toString() { return this.name; }
	
	public static DataResult<CombatMode> read(int id) {
		try {
			return DataResult.success(fromId(id));
		} catch (Exception e) {
			return DataResult.error("Something wrong happened reading CombatMode id");
		}
	}
}