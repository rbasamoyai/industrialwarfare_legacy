package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataHandler;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem.ActionType;

public abstract class FirearmItemDataHandler extends PartItemDataHandler implements IFirearmItemDataHandler {
	
	protected final ItemStackHandler attachments;
	
	protected FirearmItem.ActionType action;
	protected int time;
	
	protected boolean selected = false;
	protected int state;
	
	public FirearmItemDataHandler(ItemStackHandler attachments) {
		this.attachments = attachments;
	}
	
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
	
	@Override public IItemHandler getAttachmentsHandler() { return this.attachments; }
	@Override public CompoundNBT serializeAttachments() { return this.attachments.serializeNBT(); }
	@Override public void deserializeAttachments(CompoundNBT nbt) { this.attachments.deserializeNBT(nbt); }
	
}
