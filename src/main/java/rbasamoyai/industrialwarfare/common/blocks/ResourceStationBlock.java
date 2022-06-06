package rbasamoyai.industrialwarfare.common.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.tileentities.ResourceStationTileEntity;

public abstract class ResourceStationBlock extends Block {
	
	public ResourceStationBlock(AbstractBlock.Properties properties) {
		super(properties);
	}
	
	@Override public boolean hasTileEntity(BlockState state) { return true; }
	
	@Override public abstract TileEntity createTileEntity(BlockState state, IBlockReader reader);
	
	@Override
	public void onRemove(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasTileEntity() && (!oldState.is(newState.getBlock()) || !newState.hasTileEntity())) {
			TileEntity te = world.getBlockEntity(pos);
			if (te instanceof ResourceStationTileEntity) {
				((ResourceStationTileEntity) te).dropItems();
			}
			world.removeBlockEntity(pos);
		}
	}

}
