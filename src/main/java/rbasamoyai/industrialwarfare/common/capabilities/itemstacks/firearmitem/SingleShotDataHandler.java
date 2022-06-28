package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SingleShotDataHandler extends FirearmItemDataHandler {

	private ItemStack shot = ItemStack.EMPTY;
	
	public SingleShotDataHandler(IItemHandlerModifiable handler) {
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

	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		super.writeTag(tag);
		tag.put("loadedShot", this.shot.serializeNBT());
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		super.readTag(tag);
		this.shot = ItemStack.of(tag.getCompound("loadedShot"));
	}

}
