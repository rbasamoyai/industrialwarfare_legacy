package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import java.util.Stack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class InternalMagazineDataHandler extends FirearmItemDataHandler {
	
	private int magazineSize;
	private Stack<ItemStack> magazine = new Stack<>();
	
	public InternalMagazineDataHandler(IItemHandlerModifiable handler) {
		super(handler);
	}
	
	@Override public void setMagazineSize(int size) { this.magazineSize = size; }
	@Override public int getMagazineSize() { return this.magazineSize; }
	
	@Override
	public ItemStack insertAmmo(ItemStack ammo) {
		if (this.magazine.size() >= this.magazineSize) return ammo;
		if (ammo.isEmpty()) return ItemStack.EMPTY;
		
		ItemStack splitAmmo = ammo.split(1);
		
		this.magazine.push(splitAmmo);
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractAmmo() {
		return this.magazine.size() > 0 ? this.magazine.pop() : ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack peekAmmo(int position) {
		return 0 <= position && position < this.magazine.size() ? this.magazine.elementAt(position) : ItemStack.EMPTY;
	}
	
	@Override
	public int getAmmoPosition() {
		return this.magazine.size() <= 0 ? 0 : this.magazine.size() - 1;
	}
	
	@Override
	public boolean hasAmmo() {
		return this.magazine.size() > 0;
	}
	
	@Override
	public boolean isFull() {
		return this.magazine.size() >= this.magazineSize;
	}
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		super.writeTag(tag);
		ListTag ammo = new ListTag();
		for (int i = 0; i < this.magazine.size(); ++i) {
			ItemStack stack = this.magazine.get(i);
			if (stack == null || stack.isEmpty()) continue;
			ammo.add(stack.serializeNBT());
		}
		tag.put("internalMagazine", ammo);
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		super.readTag(tag);
		this.magazine.clear();
		ListTag ammo = tag.getList("internalMagazine", Tag.TAG_COMPOUND);
		for (int i = 0; i < ammo.size(); ++i) {
			this.magazine.push(ItemStack.of(ammo.getCompound(i)));
		}
	}

}
