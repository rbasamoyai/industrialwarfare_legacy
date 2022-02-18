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

}
