package rbasamoyai.industrialwarfare.common.entityai.navigation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.vector.Vector3d;

public class PrecisePosCodec {

	public static final Codec<Vector3d> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.DOUBLE.fieldOf("x").forGetter(Vector3d::x),
				Codec.DOUBLE.fieldOf("y").forGetter(Vector3d::y),
				Codec.DOUBLE.fieldOf("z").forGetter(Vector3d::z))
				.apply(instance, Vector3d::new);
	});
	
}
