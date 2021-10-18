package rbasamoyai.industrialwarfare.common.blocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfContainer;
import rbasamoyai.industrialwarfare.common.tileentities.NormalWorkstationTileEntity;
import rbasamoyai.industrialwarfare.common.tileentities.TaskScrollShelfTileEntity;
import rbasamoyai.industrialwarfare.utils.IWInventoryUtils;

public class TaskScrollShelfBlock extends Block {
	
	private static final ITextComponent TITLE = new TranslationTextComponent("tile." + IndustrialWarfare.MOD_ID + ".assembler_workstation");
	
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
		super(AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WOOD).noOcclusion().harvestTool(ToolType.AXE).strength(2.5f, 2.5f).harvestLevel(1).sound(SoundType.WOOD));
		
		this.registerDefaultState(
				this.stateDefinition.any()
						.setValue(HORIZONTAL_FACING, Direction.NORTH)
				);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TaskScrollShelfTileEntity();
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public void onRemove(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasTileEntity() && (!oldState.is(newState.getBlock()) || !newState.hasTileEntity())) {
			TileEntity te = world.getBlockEntity(pos);
			
			double x = (double) pos.getX();
			double y = (double) pos.getY();
			double z = (double) pos.getZ();
			
			if (te instanceof TaskScrollShelfTileEntity) {
				TaskScrollShelfTileEntity shelfTE = (TaskScrollShelfTileEntity) te;
				IWInventoryUtils.dropHandlerItems(shelfTE.getItemHandler(), x, y, z, world);
			}
			
			world.removeBlockEntity(pos);
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		VoxelShape result = SHAPES.get(state.getValue(HORIZONTAL_FACING));
		return result == null ? NORTH_SHAPE : result;
	}
	
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.setValue(HORIZONTAL_FACING, (mirrorIn.getRotation(state.getValue(HORIZONTAL_FACING)).rotate(state.getValue(HORIZONTAL_FACING))));
	}

	@Override
	public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
		return state.setValue(HORIZONTAL_FACING, direction.rotate(state.getValue(HORIZONTAL_FACING)));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.stateDefinition.any().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public boolean skipRendering(BlockState state1, BlockState state2, Direction direction) {
		return false;
	}
	
	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (!world.isClientSide) return ActionResultType.SUCCESS;
		if (!(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;
		
		TileEntity te = world.getBlockEntity(pos);
		if (te == null) return ActionResultType.FAIL;
		if (!(te instanceof NormalWorkstationTileEntity)) return ActionResultType.FAIL;
		
		IContainerProvider containerProvider = TaskScrollShelfContainer.getServerContainerProvider((TaskScrollShelfTileEntity) te, pos);
		INamedContainerProvider namedContainerProvider = new SimpleNamedContainerProvider(containerProvider, TITLE);
		NetworkHooks.openGui((ServerPlayerEntity) player, namedContainerProvider, buf -> {});
		return ActionResultType.CONSUME;
	}
	
}
