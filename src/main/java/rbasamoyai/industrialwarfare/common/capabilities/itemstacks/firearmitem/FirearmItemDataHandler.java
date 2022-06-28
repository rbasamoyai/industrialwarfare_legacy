package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem.ActionType;

public abstract class FirearmItemDataHandler implements IFirearmItemData {
	
	protected final IItemHandlerModifiable attachments;
	
	protected FirearmItem.ActionType action = ActionType.NOTHING;
	protected int time;
	
	protected boolean selected = false;
	protected int state;
	protected int recoilTicks;
	protected float recoilPitch;
	protected float recoilYaw;
	
	public FirearmItemDataHandler(IItemHandlerModifiable attachments) {
		this.attachments = attachments;
	}
	
	@Override public void setRecoilTicks(int ticks) { this.recoilTicks = ticks; }
	@Override public int getRecoilTicks() { return this.recoilTicks; }
	
	@Override
	public void setRecoil(float recoilPitch, float recoilYaw) {
		this.recoilPitch = recoilPitch;
		this.recoilYaw = recoilYaw;
	}
	
	@Override public float getRecoilPitch() { return this.recoilPitch; }
	@Override public float getRecoilYaw() { return this.recoilYaw; }
	
	@Override public void setState(int state) { this.state = state; }
	@Override public int getState() { return this.state; }
	
	@Override public void setSelected(boolean selected) { this.selected = selected; }
	@Override public boolean isSelected() { return this.selected; }
	
	@Override
	public void setAction(ActionType action, int time) {
		this.action = action;
		this.time = time;
	}
	
	@Override public ActionType getAction() { return this.action; }
	@Override public boolean isFinishedAction() { return this.time <= 0; }
	@Override public void countdownAction() { --this.time; }
	@Override public int actionTime() { return this.time; }
	
	@Override public IItemHandlerModifiable getAttachmentsHandler() { return this.attachments; }
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.putInt("action", this.action.getId());
		tag.putInt("actionTime", this.time);
		tag.putInt("recoilTicks", this.recoilTicks);
		tag.putFloat("recoilPitch", this.recoilPitch);
		tag.putFloat("recoilYaw", this.recoilYaw);
		tag.putInt("state", this.state);
		
		ListTag attachmentsList = new ListTag();
		for (int i = 0; i < this.attachments.getSlots(); ++i) {
			ItemStack stack = this.attachments.getStackInSlot(i);
			if (stack == null || stack.isEmpty()) continue;
			CompoundTag itemTag = stack.serializeNBT();
			itemTag.putByte("Slot", (byte) i);
			attachmentsList.add(itemTag);
		}
		tag.put("attachments", attachmentsList);
		
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		this.action = ActionType.fromId(tag.getInt("action"));
		this.time = tag.getInt("actionTime");
		if (this.action == ActionType.NOTHING && this.isFinishedAction()) {
			this.setAction(ActionType.NOTHING, 1);
		}
		this.recoilTicks = tag.getInt("recoilTicks");
		this.recoilPitch = tag.getFloat("recoilPitch");
		this.recoilYaw = tag.getFloat("recoilYaw");
		this.state = tag.getInt("state");
		
		ListTag attachmentsList = tag.getList("attachments", Tag.TAG_COMPOUND);
		for (int i = 0; i < attachmentsList.size(); ++i) {
			CompoundTag itemTag = attachmentsList.getCompound(i);
			int slot = itemTag.getByte("Slot");
			ItemStack stack = ItemStack.of(itemTag);
			if (0 <= slot && slot < this.attachments.getSlots()) {
				this.attachments.setStackInSlot(slot, stack);
			}
		}
	}
	
}
