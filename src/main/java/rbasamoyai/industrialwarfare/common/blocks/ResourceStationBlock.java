package rbasamoyai.industrialwarfare.common.blocks;

import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationContainer;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
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
	
	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
		if (level.isClientSide) return ActionResultType.SUCCESS;
		if (!(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;
		
		TileEntity te = level.getBlockEntity(pos);
		if (te == null) return ActionResultType.FAIL;
		if (!(te instanceof ResourceStationTileEntity)) return ActionResultType.FAIL;
		ResourceStationTileEntity resourceStation = (ResourceStationTileEntity) te;
		
		IContainerProvider containerProvider = ResourceStationContainer.getServerContainerProvider(resourceStation, pos);
		ITextComponent title = new TranslationTextComponent("gui." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath());
		INamedContainerProvider namedContainerProvider = new SimpleNamedContainerProvider(containerProvider, title);
		NetworkHooks.openGui((ServerPlayerEntity) player, namedContainerProvider, buf -> {
			buf.writeBlockPos(pos);
			buf.writeItem(new ItemStack(this.asItem()));
			buf.writeBoolean(resourceStation.isRunning());
			
			List<SupplyRequestPredicate> requests = resourceStation.getRequests();
			buf.writeVarInt(requests.size());
			requests.forEach(p -> p.toNetwork(buf));
			
			List<SupplyRequestPredicate> extraStock = resourceStation.getExtraStock();
			buf.writeVarInt(extraStock.size());
			extraStock.forEach(p -> p.toNetwork(buf));
		});
		return ActionResultType.CONSUME;
	}

}
