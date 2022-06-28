package rbasamoyai.industrialwarfare.common.blocks;

import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpoolBlock extends Block {

	public static final VoxelShape SHAPE = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(5, 1, 5, 11, 15, 11), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR)).get();
	
	public SpoolBlock() {
		super(BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD).strength(1.0f).noOcclusion());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
}
