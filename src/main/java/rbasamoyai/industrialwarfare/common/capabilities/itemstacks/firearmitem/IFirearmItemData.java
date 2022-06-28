package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem.ActionType;

public interface IFirearmItemData {

	public static final int FLAG_CYCLED = 1;
	public static final int FLAG_FIRED = 2;
	public static final int FLAG_MELEEING = 4;
	public static final int FLAG_AIMING = 8;
	public static final int FLAG_SPRINTING = 16;
	
	void setSelected(boolean selected);
	boolean isSelected();
	
	void setRecoilTicks(int ticks);
	int getRecoilTicks();
	default void tickRecoil() { this.setRecoilTicks(Math.min(this.getRecoilTicks() + 1, 10)); }
	
	void setRecoil(float recoilPitch, float recoilYaw);
	float getRecoilPitch();
	float getRecoilYaw();
	
	void setState(int state);
	int getState();
	
	default void setCycled(boolean cycled) { this.setState(cycled ? this.getState() | FLAG_CYCLED : this.getState() & (0xFFFFFFFF ^ FLAG_CYCLED)); }
	default boolean isCycled() { return (this.getState() & FLAG_CYCLED) == FLAG_CYCLED; }
	
	default void setFired(boolean fired) { this.setState(fired ? this.getState() | FLAG_FIRED : this.getState() & (0xFFFFFFFF ^ FLAG_FIRED)); }
	default boolean isFired() { return (this.getState() & FLAG_FIRED) == FLAG_FIRED; }
	
	default void setMelee(boolean melee) { this.setState(melee ? this.getState() | FLAG_MELEEING : this.getState() & (0xFFFFFFFF ^ FLAG_MELEEING)); }
	default boolean isMeleeing() { return (this.getState() & FLAG_MELEEING) == FLAG_MELEEING; }
	
	default void setAiming(boolean aiming) { this.setState(aiming ? this.getState() | FLAG_AIMING : this.getState() & (0xFFFFFFFF ^ FLAG_AIMING)); }
	default boolean isAiming() { return (this.getState() & FLAG_AIMING) == FLAG_AIMING; }
	
	default void setDisplaySprinting(boolean displaySprinting) { this.setState(displaySprinting ? this.getState() | FLAG_SPRINTING : this.getState() & (0xFFFFFFFF ^ FLAG_SPRINTING)); }
	default boolean shouldDisplaySprinting() { return (this.getState() & FLAG_SPRINTING) == FLAG_SPRINTING; }
	
	void setAction(FirearmItem.ActionType action, int time);
	ActionType getAction();
	boolean isFinishedAction();
	void countdownAction();
	int actionTime();
	
	void setMagazineSize(int size);
	int getMagazineSize();
	
	int getAmmoPosition();
	default void setAmmoPosition(int position) {}
	
	ItemStack insertAmmo(ItemStack ammo);
	ItemStack extractAmmo();
	ItemStack peekAmmo(int position);
	
	boolean hasAmmo();
	boolean isFull();
	default boolean isEmpty() { return !this.hasAmmo(); }
	
	IItemHandler getAttachmentsHandler();
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
