package rbasamoyai.industrialwarfare.common.blockentities;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfItemHandler;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfMenu;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;

public class TaskScrollShelfBlockEntity extends BlockEntity {
	
	private static final String TAG_INVENTORY = "inventory";

	private final TaskScrollShelfItemHandler itemHandler = new TaskScrollShelfItemHandler(this, TaskScrollShelfMenu.SHELF_SLOT_COUNT);
	private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> this.itemHandler);
	
	public TaskScrollShelfBlockEntity(BlockPos pos, BlockState state) {
		this(BlockEntityTypeInit.TASK_SCROLL_SHELF.get(), pos, state);
	}
	
	public TaskScrollShelfBlockEntity(BlockEntityType<? extends TaskScrollShelfBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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
		this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
		super.setChanged();
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
		tag.put(TAG_INVENTORY, this.itemHandler.serializeNBT());
	}
	
	@Override
	public void load(CompoundTag tag) {
		this.itemHandler.deserializeNBT(tag.getCompound(TAG_INVENTORY));
		super.load(tag);
	}
	
}
