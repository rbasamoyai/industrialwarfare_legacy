package rbasamoyai.industrialwarfare.common.tileentities;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfContainer;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfItemHandler;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;

public class TaskScrollShelfTileEntity extends TileEntity {
	
	private static final String TAG_INVENTORY = "inventory";

	private final TaskScrollShelfItemHandler itemHandler = new TaskScrollShelfItemHandler(this, TaskScrollShelfContainer.SHELF_SLOT_COUNT);
	private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> this.itemHandler);
	
	public TaskScrollShelfTileEntity() {
		super(TileEntityTypeInit.TASK_SCROLL_SHELF.get());
	}
	
	public TaskScrollShelfItemHandler getItemHandler() {
		return this.itemHandler;
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return this.itemOptional.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		this.itemOptional.invalidate();
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
		this.load(state, tag);
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
		tag.put(TAG_INVENTORY, this.itemHandler.serializeNBT());
		return super.save(tag);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT tag) {
		this.itemHandler.deserializeNBT(tag.getCompound(TAG_INVENTORY));
		super.load(state, tag);
	}
	
}
