package rbasamoyai.industrialwarfare.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Direction;

public class ArgUtils {

	public static List<Byte> box(byte[] array) {
		List<Byte> result = new ArrayList<>(array.length);
		for (byte b : array) result.add(Byte.valueOf(b));
		return result;
	}
	
	public static byte[] unbox(List<Byte> list) {
		byte[] result = new byte[list.size()];
		int i = 0;
		for (Byte b : list) result[i++] = b.byteValue();
		return result;
	}
	
	public static Direction getDirection(int b) {
		return b < 0 || b >= Direction.values().length ? Direction.DOWN : Direction.values()[b];
	}
	
}
