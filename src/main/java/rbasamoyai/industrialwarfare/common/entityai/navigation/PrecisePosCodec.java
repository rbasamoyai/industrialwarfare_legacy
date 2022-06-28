package rbasamoyai.industrialwarfare.common.entityai.navigation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.phys.Vec3;

public class PrecisePosCodec {

	public static final Codec<Vec3> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
				Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
				Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z))
				.apply(instance, Vec3::new);
	});
	
}
