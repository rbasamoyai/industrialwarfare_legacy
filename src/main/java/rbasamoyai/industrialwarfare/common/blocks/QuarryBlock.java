package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import rbasamoyai.industrialwarfare.common.blockentities.QuarryBlockEntity;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;

public class QuarryBlock extends ResourceStationBlock {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public QuarryBlock() {
		super(ManufacturingBlock.WORKSTATION_WOOD);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
	
	@Override public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) { return new QuarryBlockEntity(pPos, pState); }
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack item) {
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof QuarryBlockEntity)) return;
		((QuarryBlockEntity) be).setYLevel(pos.getY() + 1);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, BlockEntityTypeInit.QUARRY.get(), QuarryBlockEntity::serverTicker);
	}
	
}
