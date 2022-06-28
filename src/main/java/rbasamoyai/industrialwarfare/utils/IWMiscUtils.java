package rbasamoyai.industrialwarfare.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class IWMiscUtils {

	public static boolean isTopSlabAt(Level level, BlockPos pos) {
		BlockState blockstate = level.getBlockState(pos);
		return blockstate.is(BlockTags.SLABS)
			&& blockstate.getValue(SlabBlock.TYPE) == SlabType.TOP
			&& !blockstate.getValue(SlabBlock.WATERLOGGED);
	}
	
	public static float quadEasingIn(float time) {
		return time * time;
	}
	
	public static float quadEasingOut(float time) {
		float f = time - 1;
		return 1 - f * f;
	}
	
	public static float quadEasingInOut(float time) {
		if (time < 0.5) {
			return 2.0f * quadEasingIn(time);
		}
		return 2.0f * quadEasingOut(time) - 1.0f;
	}

}
