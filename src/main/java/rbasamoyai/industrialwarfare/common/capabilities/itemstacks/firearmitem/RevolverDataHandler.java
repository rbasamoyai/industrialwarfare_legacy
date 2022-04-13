package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class RevolverDataHandler extends FirearmItemDataHandler {
	
	private int currentPosition;
	private NonNullList<ItemStack> cylinder;
	
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

	private static final String TAG_CYLINDER = "cylinder";
	private static final String TAG_CURRENT_POSITION = "currentPosition";
	private static final String TAG_SLOT = "slot";
	private static final String TAG_ITEM = "item";
	
	@Override
	public CompoundNBT serializeAmmo() {
		CompoundNBT nbt = new CompoundNBT();
		
		ListNBT cylinderTag = new ListNBT();
		for (int i = 0; i < this.cylinder.size(); ++i) {
			ItemStack currentStack = this.cylinder.get(i);
			if (currentStack.isEmpty()) continue;
			CompoundNBT slotTag = new CompoundNBT();
			slotTag.putInt(TAG_SLOT, i);
			slotTag.put(TAG_ITEM, currentStack.serializeNBT());
			cylinderTag.add(slotTag);
		}
		nbt.put(TAG_CYLINDER, cylinderTag);
		
		nbt.putInt(TAG_CURRENT_POSITION, this.currentPosition);
		
		return nbt;
	}

	@Override
	public void deserializeAmmo(CompoundNBT nbt) {
		ListNBT cylinderTag = nbt.getList(TAG_CYLINDER, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < cylinderTag.size(); ++i) {
			CompoundNBT slotTag = cylinderTag.getCompound(i);
			int slot = slotTag.getInt(TAG_SLOT);
			ItemStack item = ItemStack.of(slotTag.getCompound(TAG_ITEM));
			this.cylinder.set(slot, item);
		}
		this.currentPosition = nbt.getInt(TAG_CURRENT_POSITION);
	}

	

}
