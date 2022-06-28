package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

public class OpaqueCutoutBlock extends Block {

	public OpaqueCutoutBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
}
