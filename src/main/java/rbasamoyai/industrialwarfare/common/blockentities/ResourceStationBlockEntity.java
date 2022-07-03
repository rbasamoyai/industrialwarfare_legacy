package rbasamoyai.industrialwarfare.common.blockentities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.CSyncExtraStock;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.CSyncRequests;
import rbasamoyai.industrialwarfare.utils.ModInventoryUtils;

public abstract class ResourceStationBlockEntity extends BlockEntity {

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
	
	public ResourceStationBlockEntity(BlockEntityType<? extends ResourceStationBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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
	public CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		tag.put(TAG_BUFFER, this.buffer.serializeNBT());
		tag.put(TAG_SUPPLIES, this.supplies.serializeNBT());
		tag.putBoolean(TAG_RUNNING, this.isRunning);
		
		ListTag extraStockNBT = new ListTag();
		this.extraStock.forEach(r -> {
			extraStockNBT.add(r.serializeNBT());
		});
		tag.put(TAG_EXTRA_STOCK, extraStockNBT);
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.buffer.deserializeNBT(nbt.getCompound(TAG_BUFFER));
		this.supplies.deserializeNBT(nbt.getCompound(TAG_SUPPLIES));
		this.isRunning = nbt.getBoolean(TAG_RUNNING);
		
		this.extraStock.clear();
		
		ListTag extraStockNBT = nbt.getList(TAG_EXTRA_STOCK, Tag.TAG_COMPOUND);
		for (int i = 0; i < extraStockNBT.size(); ++i) {
			this.extraStock.add(SupplyRequestPredicate.fromNBT(extraStockNBT.getCompound(i)));
		}
	}
	
	@Nullable
	public ItemEntity getItemToPickup(LivingEntity entity) {
		if (this.itemsToPickUp.isEmpty()) {
			this.findItemsToPickUp();
		}
		for (Iterator<ItemEntity> iter = this.itemsToPickUp.iterator(); iter.hasNext(); ) {
			ItemEntity item = iter.next();
			iter.remove();
			if (item != null && !item.isRemoved() && !item.getItem().isEmpty()) return item;
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
		
		ModInventoryUtils.dropHandlerItems(this.buffer, x, y, z, this.level);
		ModInventoryUtils.dropHandlerItems(this.supplies, x, y, z, this.level);
		
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
	
	public List<SupplyRequestPredicate> getRequests() {
		return this.requests.values()
				.stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}
	
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
