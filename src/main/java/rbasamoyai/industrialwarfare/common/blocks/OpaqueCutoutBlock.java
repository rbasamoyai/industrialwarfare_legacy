package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class OpaqueCutoutBlock extends Block {

	public OpaqueCutoutBlock(AbstractBlock.Properties properties) {
		super(properties);
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
}
