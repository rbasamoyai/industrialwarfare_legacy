package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.IPartItemDataHandler;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem.ActionType;

public interface IFirearmItemDataHandler extends IPartItemDataHandler {

	void setCycled(boolean cycled);
	boolean isCycled();
	
	void setFired(boolean fired);
	boolean isFired();
	
	void setMelee(boolean melee);
	boolean isMeleeing();
	
	void setAiming(boolean aiming);
	boolean isAiming();
	
	void setAction(FirearmItem.ActionType action, int time);
	ActionType getAction();
	boolean isFinishedAction();
	void countdownAction();
	int actionTime();
	
	void setMagazineSize(int size);
	int getMagazineSize();
	ItemStack insertAmmo(ItemStack ammo);
	ItemStack extractAmmo();
	boolean hasAmmo();
	boolean isFull();
	default boolean isEmpty() { return !this.hasAmmo(); }
	CompoundNBT serializeAmmo();
	void deserializeAmmo(CompoundNBT nbt);
	
	IItemHandler getAttachmentsHandler();
	CompoundNBT serializeAttachments();
	void deserializeAttachments(CompoundNBT nbt);
		
}
