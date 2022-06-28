package rbasamoyai.industrialwarfare.common.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.industrialwarfare.common.blockentities.MatchCoilBlockEntity;
import rbasamoyai.industrialwarfare.common.items.MatchCoilItem;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class MatchCoilBlock extends BaseEntityBlock {

	private static final String TAG_MAX_DAMAGE = MatchCoilItem.TAG_MAX_DAMAGE;
	
	public static final IntegerProperty COIL_AMOUNT = IntegerProperty.create("coil_amount", 0, 4);
	
	public static final VoxelShape COIL_0_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(1, 1, 1, 15, 15, 15), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR)).get();
	
	public static final VoxelShape COIL_1_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(2, 1, 2, 14, 15, 14), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR)).get();
	
	public static final VoxelShape COIL_2_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(3, 1, 3, 13, 15, 13), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR)).get();
	
	public static final VoxelShape COIL_3_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(4, 1, 4, 12, 15, 12), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR)).get();
	
	public static final VoxelShape COIL_4_4 = SpoolBlock.SHAPE;
	
	private static final Map<Integer, VoxelShape> SHAPES = Util.make(new HashMap<>(), map -> {
		map.put(0, COIL_0_4);
		map.put(1, COIL_1_4);
		map.put(2, COIL_2_4);
		map.put(3, COIL_3_4);
		map.put(4, COIL_4_4);
	});
	
	public MatchCoilBlock() {
		super(BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD).strength(1.0f).noOcclusion());
		
		this.registerDefaultState(this.stateDefinition.any().setValue(COIL_AMOUNT, 0));
	}
	
	@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COIL_AMOUNT);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		VoxelShape result = SHAPES.get(state.getValue(COIL_AMOUNT));
		return result == null ? COIL_0_4 : result;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		ItemStack stack = context.getItemInHand();
		int i = (int) Math.ceil((float) stack.getDamageValue() * 4.0f / (float) stack.getMaxDamage());
		return super.getStateForPlacement(context).setValue(COIL_AMOUNT, i);
	}
	
	@Override public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) { return new MatchCoilBlockEntity(pPos, pState); }
	
	@Override
	public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasBlockEntity() && (!oldState.is(newState.getBlock()) || !newState.hasBlockEntity())) {
			BlockEntity te = level.getBlockEntity(pos);
			
			double x = (double) pos.getX();
			double y = (double) pos.getY();
			double z = (double) pos.getZ();
			
			if (te instanceof MatchCoilBlockEntity) {
				MatchCoilBlockEntity coil = (MatchCoilBlockEntity) te;
				if (coil.getMaxDamage() - coil.getCoilDamage() <= 0) {
					Containers.dropItemStack(level, x, y, z, new ItemStack(ItemInit.SPOOL.get()));
				} else {
					ItemStack coilItem = new ItemStack(ItemInit.MATCH_COIL.get());
					coilItem.getOrCreateTag().putInt(TAG_MAX_DAMAGE, coil.getMaxDamage());
					coilItem.setDamageValue(coil.getCoilDamage());
					
					Containers.dropItemStack(level, x, y, z, coilItem);
				}
			}
			
			level.removeBlockEntity(pos);
		}
	}
	
	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState,
			LivingEntity pPlacer, ItemStack pStack) {
		if (!pLevel.isClientSide) {
			BlockEntity te = pLevel.getBlockEntity(pPos);
			if (te instanceof MatchCoilBlockEntity) {
				MatchCoilBlockEntity coil = (MatchCoilBlockEntity) te;
				coil.setMaxDamage(pStack.getMaxDamage());
				coil.setCoilDamage(pStack.getDamageValue());
				te.setChanged();
			}
		}
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
	}
	
}
