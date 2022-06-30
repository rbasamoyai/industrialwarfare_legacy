package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import rbasamoyai.industrialwarfare.common.blockentities.FarmingPlotBlockEntity;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;

public class FarmingPlotBlock extends ResourceStationBlock {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public FarmingPlotBlock() {
		super(ManufacturingBlock.WORKSTATION_WOOD);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.setValue(FACING, (mirrorIn.getRotation(state.getValue(FACING)).rotate(state.getValue(FACING))));
	}

	@Override
	public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
		return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) { return new FarmingPlotBlockEntity(pPos, pState); }
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, BlockEntityTypeInit.FARMING_PLOT.get(), FarmingPlotBlockEntity::serverTicker);
	}

}
