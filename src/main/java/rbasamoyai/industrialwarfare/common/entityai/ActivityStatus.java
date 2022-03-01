package rbasamoyai.industrialwarfare.common.entityai;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum ActivityStatus {
	
	NO_ACTIVITY("no_activity"),
	WORKING("working"),
	FIGHTING("fighting");
	
	public static final Codec<ActivityStatus> CODEC = Codec.STRING.comapFlatMap(ActivityStatus::read, ActivityStatus::toString).stable();
	
	private static final Map<String, ActivityStatus> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ActivityStatus::toString, npcas -> npcas));
	
	private final String name;
	
	private ActivityStatus(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	private static DataResult<ActivityStatus> read(String name) {
		try {
			return DataResult.success(BY_NAME.get(name));
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			BY_NAME.keySet().forEach(k -> sb.append(", "));
			sb.delete(sb.length() -1, sb.length());
			sb.setCharAt(sb.length() - 1, '}');
			return DataResult.error("Not a valid string " + name + "; valid strings include {" + sb.toString());
		}
	}
	
}
