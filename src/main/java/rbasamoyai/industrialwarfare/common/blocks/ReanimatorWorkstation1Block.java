package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReanimatorWorkstation1Block extends RotatableWorkstationBlock {

	public static final EnumProperty<DoubleBlockHalf> DOUBLE_BLOCK_HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public ReanimatorWorkstation1Block() {
		super("reanimator_workstation_1", WorkstationBlock.WORKSTATION_METAL);

		this.registerDefaultState(this.stateDefinition.any().setValue(DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context).setValue(DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
	}

	@Override
	public void onPlace(BlockState oldState, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.isEmptyBlock(pos.above())) {
			worldIn.setBlock(pos.above(),
					this.getStateDefinition().any().setValue(DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 0);
		} else {
			worldIn.removeBlock(pos, false);
			worldIn.removeBlockEntity(pos);
		}
	}

	@Override
	public void onRemove(BlockState oldState, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (oldState.hasTileEntity() && oldState.getBlock() != newState.getBlock()) {

		}
	}
}
