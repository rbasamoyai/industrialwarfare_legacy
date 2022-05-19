package rbasamoyai.industrialwarfare.common.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import rbasamoyai.industrialwarfare.common.items.MatchCoilItem;
import rbasamoyai.industrialwarfare.common.tileentities.MatchCoilTileEntity;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class MatchCoilBlock extends Block {

	private static final String TAG_MAX_DAMAGE = MatchCoilItem.TAG_MAX_DAMAGE;
	
	public static final IntegerProperty COIL_AMOUNT = IntegerProperty.create("coil_amount", 0, 4);
	
	public static final VoxelShape COIL_0_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(1, 1, 1, 15, 15, 15), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> VoxelShapes.joinUnoptimized(a, b, IBooleanFunction.OR)).get();
	
	public static final VoxelShape COIL_1_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(2, 1, 2, 14, 15, 14), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> VoxelShapes.joinUnoptimized(a, b, IBooleanFunction.OR)).get();
	
	public static final VoxelShape COIL_2_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(3, 1, 3, 13, 15, 13), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> VoxelShapes.joinUnoptimized(a, b, IBooleanFunction.OR)).get();
	
	public static final VoxelShape COIL_3_4 = Stream.of(Block.box(0, 0, 0, 16, 1, 16), Block.box(4, 1, 4, 12, 15, 12), Block.box(0, 15, 0, 16, 16, 16))
			.reduce((a, b) -> VoxelShapes.joinUnoptimized(a, b, IBooleanFunction.OR)).get();
	
	public static final VoxelShape COIL_4_4 = SpoolBlock.SHAPE;
	
	private static final Map<Integer, VoxelShape> SHAPES = Util.make(new HashMap<>(), map -> {
		map.put(0, COIL_0_4);
		map.put(1, COIL_1_4);
		map.put(2, COIL_2_4);
		map.put(3, COIL_3_4);
		map.put(4, COIL_4_4);
	});
	
	public MatchCoilBlock() {
		super(AbstractBlock.Properties.of(Material.WOOL, MaterialColor.WOOD).strength(1.0f).harvestTool(ToolType.AXE).noOcclusion());
		
		this.registerDefaultState(this.stateDefinition.any().setValue(COIL_AMOUNT, 0));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(COIL_AMOUNT);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		VoxelShape result = SHAPES.get(state.getValue(COIL_AMOUNT));
		return result == null ? COIL_0_4 : result;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		ItemStack stack = context.getItemInHand();
		int i = (int) Math.ceil(stack.getItem().getDurabilityForDisplay(stack) * 4.0f);
		return super.getStateForPlacement(context).setValue(COIL_AMOUNT, i);
	}
	
	@Override public boolean hasTileEntity(BlockState state) { return true; }
	
	@Override public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new MatchCoilTileEntity(); }
	
	@Override
	public void onRemove(BlockState oldState, World level, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasTileEntity() && (!oldState.is(newState.getBlock()) || !newState.hasTileEntity())) {
			TileEntity te = level.getBlockEntity(pos);
			
			double x = (double) pos.getX();
			double y = (double) pos.getY();
			double z = (double) pos.getZ();
			
			if (te instanceof MatchCoilTileEntity) {
				MatchCoilTileEntity coil = (MatchCoilTileEntity) te;
				if (coil.getMaxDamage() - coil.getCoilDamage() <= 0) {
					InventoryHelper.dropItemStack(level, x, y, z, new ItemStack(ItemInit.SPOOL.get()));
				} else {
					ItemStack coilItem = new ItemStack(ItemInit.MATCH_COIL.get());
					coilItem.getOrCreateTag().putInt(TAG_MAX_DAMAGE, coil.getMaxDamage());
					coilItem.setDamageValue(coil.getCoilDamage());
					
					InventoryHelper.dropItemStack(level, x, y, z, coilItem);
				}
			}
			
			level.removeBlockEntity(pos);
		}
	}
	
}
