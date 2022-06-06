package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.ResourceStationContainer;
import rbasamoyai.industrialwarfare.common.tileentities.QuarryTileEntity;
import rbasamoyai.industrialwarfare.common.tileentities.ResourceStationTileEntity;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class QuarryBlock extends ResourceStationBlock {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".quarry");
	
	public QuarryBlock() {
		super(WorkstationBlock.WORKSTATION_WOOD);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.setValue(FACING, (mirrorIn.getRotation(state.getValue(FACING)).rotate(state.getValue(FACING))));
	}

	@Override
	public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
		return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader reader) {
		return new QuarryTileEntity();
	}
	
	@Override
	public void setPlacedBy(World level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack item) {
		TileEntity te = level.getBlockEntity(pos);
		if (!(te instanceof QuarryTileEntity)) return;
		((QuarryTileEntity) te).setYLevel(pos.getY() + 1);
	}
	
	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
		if (level.isClientSide) return ActionResultType.SUCCESS;
		if (!(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;
		
		TileEntity te = level.getBlockEntity(pos);
		if (te == null) return ActionResultType.FAIL;
		if (!(te instanceof ResourceStationTileEntity)) return ActionResultType.FAIL;
		
		IContainerProvider containerProvider = ResourceStationContainer.getServerContainerProvider((ResourceStationTileEntity) te, pos);
		INamedContainerProvider namedContainerProvider = new SimpleNamedContainerProvider(containerProvider, TITLE);
		NetworkHooks.openGui((ServerPlayerEntity) player, namedContainerProvider, buf -> {
			buf.writeBlockPos(pos);
			buf.writeItem(new ItemStack(ItemInit.QUARRY.get()));
		});
		return ActionResultType.CONSUME;
	}
	
}
