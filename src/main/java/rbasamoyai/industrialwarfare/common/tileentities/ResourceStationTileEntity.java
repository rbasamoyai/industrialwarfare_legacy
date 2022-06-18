package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.CSyncExtraStock;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.CSyncRequests;
import rbasamoyai.industrialwarfare.utils.IWInventoryUtils;

public abstract class ResourceStationTileEntity extends TileEntity implements ITickableTileEntity {

	public static final String TAG_BUFFER = "buffer";
	public static final String TAG_SUPPLIES = "supplies";
	public static final String TAG_RUNNING = "running";
	public static final String TAG_EXTRA_STOCK = "extraStock";
	
	protected final ItemStackHandler buffer = new ItemStackHandler(27);
	protected final ItemStackHandler supplies = new ItemStackHandler(27);
	
	protected final LazyOptional<IItemHandler> bufferOptional = LazyOptional.of(() -> this.buffer);
	protected final LazyOptional<IItemHandler> suppliesOptional = LazyOptional.of(() -> this.supplies);
	
	protected final Set<ItemEntity> itemsToPickUp = new HashSet<>();
	
	protected final Map<LivingEntity, List<SupplyRequestPredicate>> requests = new LinkedHashMap<>();
	protected final List<SupplyRequestPredicate> extraStock = new ArrayList<>();
	
	protected int clockTicks;
	protected boolean isRunning = true;
	
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
		
		ListNBT extraStockNBT = new ListNBT();
		this.extraStock.forEach(r -> {
			extraStockNBT.add(r.serializeNBT());
		});
		nbt.put(TAG_EXTRA_STOCK, extraStockNBT);
		
		return super.save(nbt);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		this.buffer.deserializeNBT(nbt.getCompound(TAG_BUFFER));
		this.supplies.deserializeNBT(nbt.getCompound(TAG_SUPPLIES));
		this.isRunning = nbt.getBoolean(TAG_RUNNING);
		
		this.extraStock.clear();
		
		ListNBT extraStockNBT = nbt.getList(TAG_EXTRA_STOCK, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < extraStockNBT.size(); ++i) {
			this.extraStock.add(SupplyRequestPredicate.fromNBT(extraStockNBT.getCompound(i)));
		}
	}
	
	@Override
	public void tick() {
		++this.clockTicks;
		if (this.clockTicks >= 20) {
			this.clockTicks = 0;
			
			this.purgeEntries();
		}
	}
	
	protected void purgeEntries() {}
	
	@Nullable
	public abstract BlockInteraction getInteraction(LivingEntity entity);
	
	@SuppressWarnings("deprecation")
	@Nullable
	public ItemEntity getItemToPickup(LivingEntity entity) {
		if (this.itemsToPickUp.isEmpty()) {
			this.findItemsToPickUp();
		}
		for (Iterator<ItemEntity> iter = this.itemsToPickUp.iterator(); iter.hasNext(); ) {
			ItemEntity item = iter.next();
			iter.remove();
			if (item != null && !item.removed && !item.getItem().isEmpty()) return item;
		}
		return null;
	}
	
	protected abstract void findItemsToPickUp();
	
	public void stopWorking(LivingEntity entity) {}
	
	public void setRunning(boolean running) {
		this.isRunning = running;
		this.setChanged();
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
		
		this.setChanged();
	}
	
	public void addRequest(LivingEntity requester, SupplyRequestPredicate predicate) {
		if (this.requests.containsKey(requester)) {
			this.removeRequest(requester, predicate);
			this.requests.get(requester).add(predicate);
		} else {
			this.requests.put(requester, Util.make(new ArrayList<>(), list -> list.add(predicate)));
		}
		this.updateContainerRequests();
	}
	
	public void removeRequest(LivingEntity requester, SupplyRequestPredicate predicate) {
		if (this.requests.containsKey(requester)) {
			this.requests.get(requester).removeIf(predicate::equals);
		}
		this.updateContainerRequests();
	}
	
	public void clearRequests(LivingEntity requester) {
		this.requests.clear();
		this.updateContainerRequests();
	}
	
	public List<SupplyRequestPredicate> getRequests() { return this.requests.values().stream().flatMap(List::stream).collect(Collectors.toList()); }
	
	public void updateContainerRequests() {
		IWNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(this.worldPosition)), new CSyncRequests(this.getRequests()));
	}
	
	public void setOrAddExtraStock(SupplyRequestPredicate predicate, int index) {
		if (0 <= index && index < this.extraStock.size()) {
			this.extraStock.set(index, predicate);
		} else {
			this.extraStock.add(predicate);
		}
		this.setChanged();
		this.updateContainerExtraStock();
	}
	
	public void removeExtraStock(int index) {
		if (0 <= index && index < this.extraStock.size()) {
			this.extraStock.remove(index);
			this.setChanged();
			this.updateContainerExtraStock();
		}
	}
	
	public List<SupplyRequestPredicate> getExtraStock() { return this.extraStock; }
	
	public void updateContainerExtraStock() {
		IWNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(this.worldPosition)), new CSyncExtraStock(this.extraStock));
	}
	
	public boolean isFinished() { return false; }
	
}
