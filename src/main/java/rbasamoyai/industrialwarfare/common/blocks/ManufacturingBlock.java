package rbasamoyai.industrialwarfare.common.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.common.blockentities.ManufacturingBlockEntity;
import rbasamoyai.industrialwarfare.common.containers.workstations.ManufacturingBlockMenu;
import rbasamoyai.industrialwarfare.utils.ModInventoryUtils;

public class ManufacturingBlock extends BaseEntityBlock {

	public static final BlockBehaviour.Properties WORKSTATION_METAL	= BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL).strength(5f, 6f)/*.harvestLevel(2)*/.sound(SoundType.METAL).requiresCorrectToolForDrops();
	public static final BlockBehaviour.Properties WORKSTATION_STONE	= BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE).strength(3.5f, 3.5f)/*.harvestLevel(1)*/.sound(SoundType.STONE).requiresCorrectToolForDrops();
	public static final BlockBehaviour.Properties WORKSTATION_WOOD	= BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5f, 2.5f).sound(SoundType.WOOD);
	
	private final Supplier<BlockEntityType<ManufacturingBlockEntity>> blockEntityType;
	
	public ManufacturingBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<ManufacturingBlockEntity>> blockEntityType) {
		super(properties);
		this.blockEntityType = blockEntityType;
	}
	
	@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

	@Override
	public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean bool) {
		if (oldState.hasBlockEntity() && (!oldState.is(newState.getBlock()) || !newState.hasBlockEntity())) {
			BlockEntity te = level.getBlockEntity(pos);
			
			double x = (double) pos.getX();
			double y = (double) pos.getY();
			double z = (double) pos.getZ();
			
			if (te instanceof ManufacturingBlockEntity) {
				ManufacturingBlockEntity workstationTE = (ManufacturingBlockEntity) te;
				ModInventoryUtils.dropHandlerItems(workstationTE.getInputItemHandler(), x, y, z, level);
				ModInventoryUtils.dropHandlerItems(workstationTE.getOutputItemHandler(), x, y, z, level);
			}
			
			level.removeBlockEntity(pos);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!(player instanceof ServerPlayer)) return InteractionResult.SUCCESS;
		
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof ManufacturingBlockEntity)) return InteractionResult.FAIL;
		
		ResourceLocation beReg = be.getType().getRegistryName();
		MenuConstructor provider = ManufacturingBlockMenu.getServerContainerProvider((ManufacturingBlockEntity) be, pos);
		MenuProvider namedProvider = new SimpleMenuProvider(provider, new TranslatableComponent("tile." + beReg.getNamespace() + "." + beReg.getPath()));
		NetworkHooks.openGui((ServerPlayer) player, namedProvider, pos);
		return InteractionResult.CONSUME;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return this.blockEntityType.get().create(pPos, pState);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, this.blockEntityType.get(), ManufacturingBlockEntity::serverTick);
	}
	
}
