package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;
import rbasamoyai.industrialwarfare.utils.IWInventoryUtils;

/*
 * Base class for workstation blocks, some of the methods here (marked using @Deprecated)
 * should be overridden.
 */

public class WorkstationBlock extends Block {

	public static final AbstractBlock.Properties WORKSTATION_METAL	= AbstractBlock.Properties.of(Material.METAL, MaterialColor.METAL).harvestTool(ToolType.PICKAXE).strength(5f, 6f).harvestLevel(2).sound(SoundType.METAL).requiresCorrectToolForDrops();
	public static final AbstractBlock.Properties WORKSTATION_STONE	= AbstractBlock.Properties.of(Material.STONE, MaterialColor.STONE).harvestTool(ToolType.PICKAXE).strength(3.5f, 3.5f).harvestLevel(1).sound(SoundType.STONE).requiresCorrectToolForDrops();
	public static final AbstractBlock.Properties WORKSTATION_WOOD	= AbstractBlock.Properties.of(Material.WOOD, MaterialColor.WOOD).harvestTool(ToolType.AXE).strength(2.5f, 2.5f).harvestLevel(1).sound(SoundType.WOOD);
	
	public final ResourceLocation blockId;
	public final String blockIdString;
	
	public WorkstationBlock(String blockId, AbstractBlock.Properties properties) {
		super(properties);
		
		this.blockIdString = blockId;
		this.blockId = new ResourceLocation(IndustrialWarfare.MOD_ID, blockId);
		this.setRegistryName(this.blockId);
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
			
			if (te instanceof WorkstationTileEntity) {
				WorkstationTileEntity workstationTE = (WorkstationTileEntity) te;
				IWInventoryUtils.dropHandlerItems(workstationTE.getInputItemHandler(), x, y, z, world);
				IWInventoryUtils.dropHandlerItems(workstationTE.getOutputItemHandler(), x, y, z, world);
			}
			
			world.removeBlockEntity(pos);
		}
	}
	
	
	
	@Deprecated
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return null;
	}
	
	@Deprecated
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide) return ActionResultType.SUCCESS;
		else return ActionResultType.FAIL;
	}
}
