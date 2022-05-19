package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;

public class SingleShotDataHandler extends FirearmItemDataHandler {

	private ItemStack shot = ItemStack.EMPTY;
	
	public SingleShotDataHandler(ItemStackHandler handler) {
		super(handler);
	}
	
	@Override public void setMagazineSize(int size) {}
	@Override public int getMagazineSize() { return 1; }

	@Override
	public ItemStack insertAmmo(ItemStack ammo) {
		if (!this.shot.isEmpty()) return ammo;
		this.shot = ammo.split(1);
		return ItemStack.EMPTY;
	}

	@Override public ItemStack extractAmmo() { return this.shot.split(1); }
	@Override public ItemStack peekAmmo(int position) { return this.shot; }
	@Override public int getAmmoPosition() { return 0; }

	@Override public boolean hasAmmo() { return !this.shot.isEmpty(); }
	@Override public boolean isFull() { return !this.shot.isEmpty(); }

	@Override public CompoundNBT serializeAmmo() { return this.shot.serializeNBT(); }
	@Override public void deserializeAmmo(CompoundNBT nbt) { this.shot = ItemStack.of(nbt); }

}
