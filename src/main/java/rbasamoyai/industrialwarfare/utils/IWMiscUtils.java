package rbasamoyai.industrialwarfare.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IWMiscUtils {

	public static boolean isTopSlabAt(World level, BlockPos pos) {
		BlockState blockstate = level.getBlockState(pos);
		Block block = blockstate.getBlock();
		return block.getTags().contains(BlockTags.SLABS.getName())
			&& blockstate.getValue(SlabBlock.TYPE) == SlabType.TOP
			&& !blockstate.getValue(SlabBlock.WATERLOGGED);
	}
	
	public static boolean isAirAt(World level, BlockPos pos) {
		BlockState blockstate = level.getBlockState(pos);
		Block block = blockstate.getBlock();
		return block.isAir(blockstate, level, pos);
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
