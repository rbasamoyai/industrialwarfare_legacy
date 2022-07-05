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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.common.blockentities.ResourceStationBlockEntity;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationMenu;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;

public abstract class ResourceStationBlock extends BaseEntityBlock {
	
	public ResourceStationBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void onRemove(BlockState oldState, Level world, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasBlockEntity() && (!oldState.is(newState.getBlock()) || !newState.hasBlockEntity())) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof ResourceStationBlockEntity) {
				((ResourceStationBlockEntity) be).dropItems();
			}
			world.removeBlockEntity(pos);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!(player instanceof ServerPlayer)) return InteractionResult.SUCCESS;
		
		BlockEntity te = level.getBlockEntity(pos);
		if (!(te instanceof ResourceStationBlockEntity)) return InteractionResult.FAIL;
		ResourceStationBlockEntity resourceStation = (ResourceStationBlockEntity) te;
		
		ResourceLocation beReg = this.getRegistryName();
		MenuConstructor constructor = ResourceStationMenu.getServerContainerProvider(resourceStation, pos);
		Component title = new TranslatableComponent("tile." + beReg.getNamespace() + "." + beReg.getPath());
		MenuProvider provider = new SimpleMenuProvider(constructor, title);
		NetworkHooks.openGui((ServerPlayer) player, provider, buf -> {
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
		return InteractionResult.CONSUME;
	}

}
