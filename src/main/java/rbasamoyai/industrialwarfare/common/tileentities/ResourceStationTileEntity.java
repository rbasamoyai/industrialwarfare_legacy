package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.utils.IWInventoryUtils;

public abstract class ResourceStationTileEntity extends TileEntity implements ITickableTileEntity {

	public static final String TAG_BUFFER = "buffer";
	public static final String TAG_SUPPLIES = "supplies";
	public static final String TAG_RUNNING = "running";
	
	protected final ItemStackHandler buffer = new ItemStackHandler(27);
	protected final ItemStackHandler supplies = new ItemStackHandler(27);
	
	protected final LazyOptional<IItemHandler> bufferOptional = LazyOptional.of(() -> this.buffer);
	protected final LazyOptional<IItemHandler> suppliesOptional = LazyOptional.of(() -> this.supplies);
	
	protected final Map<BlockPos, BlockInteraction> posCache = new LinkedHashMap<>();
	protected final BiMap<BlockPos, LivingEntity> currentTasks = HashBiMap.create();
	protected int clockTicks;
	protected boolean isRunning = true;
	protected final List<SupplyRequestPredicate> requests = new ArrayList<>(); 
	
	public ResourceStationTileEntity(TileEntityType<? extends ResourceStationTileEntity> type) {
		super(type);
	}
	
	public ItemStackHandler getBuffer() { return this.buffer; }
	public ItemStackHandler getSupplies() { return this.supplies; }
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		this.bufferOptional.invalidate();
		this.suppliesOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return side.getAxis().isVertical() ? this.bufferOptional.cast() : this.suppliesOptional.cast();
		}
		return super.getCapability(cap, side);
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
		return new SUpdateTileEntityPacket(this.worldPosition, 0, this.save(new CompoundNBT()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		BlockState state = this.level.getBlockState(pkt.getPos());
		this.load(state, pkt.getTag());
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt.put(TAG_BUFFER, this.buffer.serializeNBT());
		nbt.put(TAG_SUPPLIES, this.supplies.serializeNBT());
		nbt.putBoolean(TAG_RUNNING, this.isRunning);
		return super.save(nbt);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		this.buffer.deserializeNBT(nbt.getCompound(TAG_BUFFER));
		this.supplies.deserializeNBT(nbt.getCompound(TAG_SUPPLIES));
		this.isRunning = nbt.getBoolean(TAG_RUNNING);
	}
	
	@Override
	public void tick() {
		++this.clockTicks;
		if (this.clockTicks >= 20) {
			this.clockTicks = 0;
			
			for (Map.Entry<BlockPos, LivingEntity> entry : this.currentTasks.entrySet()) {
				if (entry.getValue().isDeadOrDying()) {
					this.currentTasks.remove(entry.getKey());
				}
			}
		}
	}
	
	@Nullable
	public abstract BlockInteraction getInteraction(LivingEntity entity);
	
	public void stopWorking(LivingEntity entity) {
		this.currentTasks.inverse().remove(entity);
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public void dropItems() {
		double x = (double) this.worldPosition.getX();
		double y = (double) this.worldPosition.getY();
		double z = (double) this.worldPosition.getZ();
		
		IWInventoryUtils.dropHandlerItems(this.buffer, x, y, z, this.level);
		IWInventoryUtils.dropHandlerItems(this.supplies, x, y, z, this.level);
	}
	
}
