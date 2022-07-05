package rbasamoyai.industrialwarfare.common.blocks;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.common.blockentities.LivestockPenBlockEntity;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.LivestockPenMenu;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;

public class LivestockPenBlock extends ResourceStationBlock {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public LivestockPenBlock() {
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
	
	@Override public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) { return new LivestockPenBlockEntity(pPos, pState); }
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, BlockEntityTypeInit.LIVESTOCK_PEN.get(), LivestockPenBlockEntity::serverTicker);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!(player instanceof ServerPlayer)) return InteractionResult.SUCCESS;
		
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof LivestockPenBlockEntity)) return InteractionResult.FAIL;
		LivestockPenBlockEntity pen = (LivestockPenBlockEntity) be;
		
		ResourceLocation beReg = this.getRegistryName();
		MenuConstructor constructor = LivestockPenMenu.getServerContainerProvider(pen, pos);
		Component title = new TranslatableComponent("tile." + beReg.getNamespace() + "." + beReg.getPath());
		MenuProvider provider = new SimpleMenuProvider(constructor, title);
		NetworkHooks.openGui((ServerPlayer) player, provider, buf -> {
			buf.writeBlockPos(pos);
			buf.writeItem(new ItemStack(this.asItem()));
			buf.writeBoolean(pen.isRunning());
			buf.writeVarInt(pen.getMinimumLivestock());
			
			List<SupplyRequestPredicate> requests = pen.getRequests();
			buf.writeVarInt(requests.size());
			requests.forEach(p -> p.toNetwork(buf));
			
			List<SupplyRequestPredicate> extraStock = pen.getExtraStock();
			buf.writeVarInt(extraStock.size());
			extraStock.forEach(p -> p.toNetwork(buf));
		});
		return InteractionResult.CONSUME;
	}

}
