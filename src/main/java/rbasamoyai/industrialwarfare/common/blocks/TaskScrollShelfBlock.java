package rbasamoyai.industrialwarfare.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blockentities.TaskScrollShelfBlockEntity;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfMenu;
import rbasamoyai.industrialwarfare.utils.ModInventoryUtils;

public class TaskScrollShelfBlock extends BaseEntityBlock {
	
	private static final Component TITLE = new TranslatableComponent("tile." + IndustrialWarfare.MOD_ID + ".task_scroll_shelf");
	
	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 8, 16, 16, 16);
	private static final VoxelShape EAST_SHAPE = Block.box(0, 0, 0, 8, 16, 16);
	private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 0, 16, 16, 8);
	private static final VoxelShape WEST_SHAPE = Block.box(8, 0, 0, 16, 16, 16);
	
	private static final Map<Direction, VoxelShape> SHAPES = initShapes();
	
	private static Map<Direction, VoxelShape> initShapes() {
		Map<Direction, VoxelShape> shapes = new HashMap<>();
		shapes.put(Direction.EAST, EAST_SHAPE);
		shapes.put(Direction.WEST, WEST_SHAPE);
		shapes.put(Direction.NORTH, NORTH_SHAPE);
		shapes.put(Direction.SOUTH, SOUTH_SHAPE);
		return shapes;
	}
	
	public TaskScrollShelfBlock() {
		super(BlockBehaviour.Properties.of(Material.WOOD).noOcclusion().strength(2.5f, 2.5f).sound(SoundType.WOOD));
		
		this.registerDefaultState(
				this.stateDefinition.any()
						.setValue(HORIZONTAL_FACING, Direction.NORTH)
				);
	}
	
	@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
	}
	
	@Override public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) { return new TaskScrollShelfBlockEntity(pPos, pState); }
	
	@Override
	public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasBlockEntity() && (!oldState.is(newState.getBlock()) || !newState.hasBlockEntity())) {
			BlockEntity te = level.getBlockEntity(pos);
			
			double x = (double) pos.getX();
			double y = (double) pos.getY();
			double z = (double) pos.getZ();
			
			if (te instanceof TaskScrollShelfBlockEntity) {
				TaskScrollShelfBlockEntity shelfTE = (TaskScrollShelfBlockEntity) te;
				ModInventoryUtils.dropHandlerItems(shelfTE.getItemHandler(), x, y, z, level);
			}
			
			level.removeBlockEntity(pos);
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
		VoxelShape result = SHAPES.get(state.getValue(HORIZONTAL_FACING));
		return result == null ? NORTH_SHAPE : result;
	}
	
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.setValue(HORIZONTAL_FACING, (mirrorIn.getRotation(state.getValue(HORIZONTAL_FACING)).rotate(state.getValue(HORIZONTAL_FACING))));
	}

	@Override
	public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
		return state.setValue(HORIZONTAL_FACING, direction.rotate(state.getValue(HORIZONTAL_FACING)));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.stateDefinition.any().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public boolean skipRendering(BlockState state1, BlockState state2, Direction direction) {
		return false;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!(player instanceof ServerPlayer)) return InteractionResult.SUCCESS;
		
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof TaskScrollShelfBlockEntity)) return InteractionResult.FAIL;
		
		MenuConstructor menuConstructor = TaskScrollShelfMenu.getServerContainerProvider((TaskScrollShelfBlockEntity) be, pos);
		MenuProvider menuProvider = new SimpleMenuProvider(menuConstructor, TITLE);
		NetworkHooks.openGui((ServerPlayer) player, menuProvider, buf -> {});
		return InteractionResult.CONSUME;
	}
	
}
