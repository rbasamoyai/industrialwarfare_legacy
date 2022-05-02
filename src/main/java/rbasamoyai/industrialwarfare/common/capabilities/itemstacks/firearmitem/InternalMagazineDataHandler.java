package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import java.util.Stack;
import java.util.stream.IntStream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class InternalMagazineDataHandler extends FirearmItemDataHandler {
	
	private static final String TAG_INTERNAL_MAGAZINE = "internalMagazine";
	
	private int magazineSize;
	private Stack<ItemStack> magazine = new Stack<>();
	
	public InternalMagazineDataHandler(ItemStackHandler handler) {
		super(handler);
	}
	
	@Override public void setMagazineSize(int size) { this.magazineSize = size; }
	@Override public int getMagazineSize() { return this.magazineSize; }
	
	@Override
	public ItemStack insertAmmo(ItemStack ammo) {
		if (this.magazine.size() >= this.magazineSize) return ammo;
		if (ammo.isEmpty()) return ItemStack.EMPTY;
		
		// TODO: bullet item and other ammo
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
	public CompoundNBT serializeAmmo() {
		CompoundNBT tag = new CompoundNBT();
		ListNBT ammo = new ListNBT();
		for (int i = 0; i < this.magazine.size(); ++i) {
			ammo.add(this.magazine.get(i).serializeNBT());
		}
		tag.put(TAG_INTERNAL_MAGAZINE, ammo);
		return tag;
	}
	
	@Override
	public void deserializeAmmo(CompoundNBT nbt) {
		this.magazine.clear();
		ListNBT ammo = nbt.getList(TAG_INTERNAL_MAGAZINE, Constants.NBT.TAG_COMPOUND);
		IntStream.rangeClosed(0, ammo.size() - 1)
				.boxed()
				.map(ammo::getCompound)
				.map(ItemStack::of)
				.forEach(this::insertAmmo);
	}

}
