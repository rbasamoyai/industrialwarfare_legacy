package rbasamoyai.industrialwarfare.common.blocks;

import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
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
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.workstations.NormalWorkstationContainer;
import rbasamoyai.industrialwarfare.common.tileentities.NormalWorkstationTileEntity;
import rbasamoyai.industrialwarfare.utils.IWInventoryUtils;

public class NormalWorkstationBlock extends WorkstationBlock {
	
	private static final ITextComponent TITLE = new TranslationTextComponent("tile." + IndustrialWarfare.MOD_ID + ".task_scroll_shelf");
	
	protected final Supplier<? extends NormalWorkstationTileEntity> teSupplier;
	
	public NormalWorkstationBlock(AbstractBlock.Properties properties, Supplier<? extends NormalWorkstationTileEntity> teSupplier) {
		super(properties);
		this.teSupplier = teSupplier;
	}
	
	public static NormalWorkstationBlock assemblerWorkstation() { return new NormalWorkstationBlock(WORKSTATION_WOOD, NormalWorkstationTileEntity::assemblerTE); }
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) { 
		return teSupplier.get();
	}
	
	@Override
	public void onRemove(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasTileEntity() && (!oldState.is(newState.getBlock()) || !newState.hasTileEntity())) {
			TileEntity te = world.getBlockEntity(pos);
			
			double x = (double) pos.getX();
			double y = (double) pos.getY();
			double z = (double) pos.getZ();
			
			if (te instanceof NormalWorkstationTileEntity) {
				NormalWorkstationTileEntity workstationTE = (NormalWorkstationTileEntity) te;
				IWInventoryUtils.dropHandlerItems(workstationTE.getRecipeItemHandler(), x, y, z, world);
			}
		}
		super.onRemove(oldState, world, pos, newState, bool);
	}
	
	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (world.isClientSide) return ActionResultType.SUCCESS;
		if (!(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;
		
		TileEntity te = world.getBlockEntity(pos);
		if (te == null) return ActionResultType.FAIL;
		if (!(te instanceof NormalWorkstationTileEntity)) return ActionResultType.FAIL;
		
		IContainerProvider provider = NormalWorkstationContainer.getServerContainerProvider((NormalWorkstationTileEntity) te, pos);
		INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(provider, TITLE);
		NetworkHooks.openGui((ServerPlayerEntity) player, namedProvider, pos);
		return ActionResultType.CONSUME;
	}
	
}
