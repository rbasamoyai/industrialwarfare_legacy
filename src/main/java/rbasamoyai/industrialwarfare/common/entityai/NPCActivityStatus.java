package rbasamoyai.industrialwarfare.common.entityai;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public enum NPCActivityStatus {
	
	NO_ACTIVITY("no_activity"),
	WORKING("working"),
	FIGHTING("fighting");
	
	public static final Codec<NPCActivityStatus> CODEC = Codec.STRING.comapFlatMap(NPCActivityStatus::read, NPCActivityStatus::toString).stable();
	
	private static final Map<String, NPCActivityStatus> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(NPCActivityStatus::toString, npcas -> npcas));
	
	private final String name;
	
	private NPCActivityStatus(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	private static DataResult<NPCActivityStatus> read(String name) {
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
