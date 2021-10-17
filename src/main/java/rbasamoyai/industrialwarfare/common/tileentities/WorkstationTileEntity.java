package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.WorkstationDataCapability;
import rbasamoyai.industrialwarfare.common.itemhandlers.InputItemHandler;
import rbasamoyai.industrialwarfare.common.itemhandlers.OutputItemHandler;
import rbasamoyai.industrialwarfare.core.config.IWConfig;

/*
 * Base workstation tile entity class
 */

public class WorkstationTileEntity extends TileEntity implements ITickableTileEntity {
	
	// NBT strings
	public static final String TAG_INPUT = "input";
	public static final String TAG_OUTPUT = "output";
	
	public static final int UPDATE_TICKS = IWConfig.workstation_update.get() > 0 ? IWConfig.workstation_update.get() : 10;
	
	protected final InputItemHandler inputItemHandler = new InputItemHandler(this, 1);
	protected final OutputItemHandler outputItemHandler = new OutputItemHandler(this, 1);
	
	protected final LazyOptional<IItemHandler> inputOptional = LazyOptional.of(() -> this.inputItemHandler);
	protected final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(() -> this.outputItemHandler);
	
	protected final Block workstation;
	protected final int baseWorkTicks;
	
	protected int clockTicks; // used so that the entity detection and such doesn't happen all the time
	protected boolean forceUpdate;
	
	public WorkstationTileEntity(TileEntityType<?> tileEntityTypeIn, Block workstation, int baseWorkTicks) {
		super(tileEntityTypeIn);
		
		this.workstation = workstation;
		this.baseWorkTicks = baseWorkTicks;
		
		this.getDataHandler().ifPresent(h -> {
			h.setWorker(null);
			h.setWorkingTicks(0);
		});
		
		this.clockTicks = 0;
		this.forceUpdate = false;
	}
	
	public ItemStackHandler getInputItemHandler() {
		return inputItemHandler;
	}
	
	public ItemStackHandler getOutputItemHandler() {
		return outputItemHandler;
	}
	
	// Shortener method
	public LazyOptional<IWorkstationDataHandler> getDataHandler() {
		return this.getCapability(WorkstationDataCapability.WORKSTATION_DATA_CAPABILITY);
	}
	
	public boolean isClientSide() {
		return this.level.isClientSide;
	}
	
	public Chunk getChunk() {
		return this.level.getChunkAt(this.worldPosition);
	}
	
	public int getBaseWorkTicks() {
		return this.baseWorkTicks;
	}
		
	@Override
	public void setRemoved() {
		super.setRemoved();
		inputOptional.invalidate();
		outputOptional.invalidate();
	}
	
	public void setChangedAndForceUpdate() {
		this.setChanged();
		this.forceUpdate = true;
	}
	
	@Override
	public void setChanged() {
		this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
		super.setChanged();
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.getBlockPos(), 0, this.save(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		BlockState state = this.level.getBlockState(pkt.getPos());
		this.load(state, pkt.getTag());
	}
	
	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag.put(TAG_INPUT, this.inputItemHandler.serializeNBT());
		tag.put(TAG_OUTPUT, this.outputItemHandler.serializeNBT());
		return super.save(tag);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT tag) {
		this.inputItemHandler.deserializeNBT(tag.getCompound(TAG_INPUT));
		this.outputItemHandler.deserializeNBT(tag.getCompound(TAG_OUTPUT));
		super.load(state, tag);
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return super.getCapability(cap, side);
	}

	@Override
	public void tick() {
		++this.clockTicks;
		if (this.clockTicks >= UPDATE_TICKS || this.forceUpdate) {
			this.clockTicks = 0;
			this.forceUpdate = false;
		}
		if (!this.level.isClientSide) {
			LazyOptional<IWorkstationDataHandler> optional = this.getDataHandler();
			if (optional.map(IWorkstationDataHandler::isWorking).orElse(false)) {
				optional.ifPresent(h -> h.setWorkingTicks(MathHelper.clamp(h.getWorkingTicks() + 1, 0, this.baseWorkTicks)));
			}
		}
		this.setChanged();
	}

	public void attemptCraft(LivingEntity entity) {
		IndustrialWarfare.LOGGER.warn("In WorkstationTileEntity#attemptCraft, should not be here! Make sure to override this method in the inheriting class.");
	}
	
	public void setRecipe(ItemStack stack, boolean dropItem) {
		IndustrialWarfare.LOGGER.warn("In WorkstationTileEntity#setRecipe, should not be here! Make sure to override this method in the inheriting class.");
	}

	public void nullRecipe() {
	}
	
	public void haltCrafting() {
		this.getDataHandler().ifPresent(h -> h.setWorker(null));
		this.nullRecipe();
	}

	public void onPlayerCloseScreen(PlayerEntity player) {
		if (this.level.isClientSide) return;
		// Despite this having to do with the workstation GUI, this function is called from WorkstationPlayerActionMessage#handle
		// from a message sent from the client to the server.
		LazyOptional<IWorkstationDataHandler> optional = this.getDataHandler();
		
		UUID workerUUID = optional.map(IWorkstationDataHandler::getWorkerUUID).orElse(null);
		if (player.getUUID().equals(workerUUID)) {
			optional.ifPresent(h -> h.setWorker(null));
			this.nullRecipe();
		}
	}
}
