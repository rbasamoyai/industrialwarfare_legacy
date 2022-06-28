package rbasamoyai.industrialwarfare.common.blockentities;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

public class UpdateBlockEntityItemHandler extends ItemStackHandler {

	private final BlockEntity blockEntity;
	
	public UpdateBlockEntityItemHandler(int size, BlockEntity blockEntity) {
		super(size);
		this.blockEntity = blockEntity;
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.blockEntity.setChanged();
	}
	
}
