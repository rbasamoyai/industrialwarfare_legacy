package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class RevolverDataHandler extends FirearmItemDataHandler {
	
	private int currentPosition;
	private NonNullList<ItemStack> cylinder;
	
	public RevolverDataHandler(IItemHandlerModifiable handler) {
		super(handler);
	}
	
	@Override public void setMagazineSize(int size) { this.cylinder = NonNullList.withSize(size, ItemStack.EMPTY); }
	@Override public int getMagazineSize() { return this.cylinder.size(); }
	
	@Override public void setAmmoPosition(int position) { this.currentPosition = position; }
	@Override public int getAmmoPosition() { return this.currentPosition; }

	private boolean validateCurrentPosition() { return 0 <= this.currentPosition && this.currentPosition < this.cylinder.size(); }
	private boolean isEmptyOrFired(ItemStack stack) { return stack.isEmpty() || stack.getItem() == ItemInit.CARTRIDGE_CASE.get(); }
	
	@Override
	public ItemStack insertAmmo(ItemStack ammo) {
		if (ammo.isEmpty()) return ItemStack.EMPTY;
		if (this.cylinder.size() <= 0) return ammo;
		if (!this.validateCurrentPosition()) this.currentPosition = 0;
		ItemStack currentStack = this.cylinder.get(this.currentPosition);
		if (this.isEmptyOrFired(currentStack)) {
			this.cylinder.set(this.currentPosition, ammo.split(1));
		}
		return ammo.isEmpty() ? ItemStack.EMPTY : ammo;
	}

	@Override
	public ItemStack extractAmmo() {
		if (this.cylinder.size() <= 0) return ItemStack.EMPTY;
		if (!this.validateCurrentPosition()) this.currentPosition = 0;
		ItemStack currentStack = this.cylinder.get(this.currentPosition);
		if (this.isEmptyOrFired(currentStack)) {
			return ItemStack.EMPTY;
		}
		this.cylinder.set(this.currentPosition, new ItemStack(ItemInit.CARTRIDGE_CASE.get()));
		return currentStack;
	}

	@Override
	public ItemStack peekAmmo(int position) {
		return 0 <= position && position < this.cylinder.size() ? this.cylinder.get(position) : ItemStack.EMPTY;
	}

	@Override
	public boolean hasAmmo() {
		return this.cylinder.stream().anyMatch(s -> !this.isEmptyOrFired(s));
	}

	@Override
	public boolean isFull() {
		return !this.cylinder.stream().anyMatch(this::isEmptyOrFired);
	}

	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		super.writeTag(tag);
		this.serializeAmmo(tag);
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		super.readTag(tag);
		this.deserializeAmmo(tag);
	}
	
	public CompoundTag serializeAmmo(CompoundTag tag) {	
		ListTag cylinderTag = new ListTag();
		for (int i = 0; i < this.cylinder.size(); ++i) {
			ItemStack stack = this.cylinder.get(i);
			if (stack.isEmpty()) continue;
			CompoundTag itemTag = stack.serializeNBT();
			itemTag.putByte("Slot", (byte) i);
			cylinderTag.add(itemTag);
		}
		tag.put("cylinder", cylinderTag);
		
		tag.putByte("currentPosition", (byte) this.currentPosition);
		return tag;
	}
	
	public void deserializeAmmo(CompoundTag tag) {
		ListTag cylinderTag = tag.getList("cylinder", Tag.TAG_COMPOUND);
		for (int i = 0; i < cylinderTag.size(); ++i) {
			CompoundTag slotTag = cylinderTag.getCompound(i);
			int slot = slotTag.getInt("Slot");
			ItemStack item = ItemStack.of(slotTag);
			this.cylinder.set(slot, item);
		}
		this.currentPosition = tag.getByte("currentPosition");
	}

	

}
