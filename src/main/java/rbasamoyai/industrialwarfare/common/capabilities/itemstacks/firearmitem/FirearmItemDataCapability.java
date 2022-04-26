	package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataCapability;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem.ActionType;

public class FirearmItemDataCapability {

	public static final String TAG_ACTION = "action";
	public static final String TAG_ACTION_TIME = "actionTime";
	public static final String TAG_AIMING = "aiming";
	public static final String TAG_ATTACHMENTS = "attachments";
	public static final String TAG_CYCLED = "cycled";
	public static final String TAG_FIRED = "fired";
	public static final String TAG_MAGAZINE_SIZE = "magazineSize";
	public static final String TAG_MAGAZINE = "magazine";
	public static final String TAG_MELEEING = "meleeing";
	
	@CapabilityInject(IFirearmItemDataHandler.class)
	public static Capability<IFirearmItemDataHandler> FIREARM_ITEM_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IFirearmItemDataHandler.class, new Storage<>(), () -> new InternalMagazineDataHandler(new ItemStackHandler()));
	}
	
	public static class Storage<T extends IFirearmItemDataHandler> extends PartItemDataCapability.Storage<T> {
		@Override
		public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
			CompoundNBT tag = (CompoundNBT) super.writeNBT(capability, instance, side);
			tag.putInt(TAG_ACTION, instance.getAction().getId());
			tag.putInt(TAG_ACTION_TIME, instance.actionTime());
			tag.putBoolean(TAG_AIMING, instance.isAiming());
			tag.put(TAG_ATTACHMENTS, instance.serializeAttachments());
			tag.putBoolean(TAG_CYCLED, instance.isCycled());
			tag.putBoolean(TAG_FIRED, instance.isFired());
			tag.putInt(TAG_MAGAZINE_SIZE, instance.getMagazineSize());
			tag.put(TAG_MAGAZINE, instance.serializeAmmo());
			tag.putBoolean(TAG_MELEEING, instance.isMeleeing());
			return tag;
		}
		
		@Override
		public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
			super.readNBT(capability, instance, side, nbt);
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setAction(ActionType.fromId(tag.getInt(TAG_ACTION)), tag.getInt(TAG_ACTION_TIME));
			instance.setAiming(tag.getBoolean(TAG_AIMING));
			instance.deserializeAttachments(tag.getCompound(TAG_ATTACHMENTS));
			instance.setCycled(tag.getBoolean(TAG_CYCLED));
			instance.setFired(tag.getBoolean(TAG_FIRED));
			instance.setMagazineSize(tag.getInt(TAG_MAGAZINE_SIZE));
			instance.deserializeAmmo(tag.getCompound(TAG_MAGAZINE));
			instance.setMelee(tag.getBoolean(TAG_MELEEING));
			if (instance.getAction() == ActionType.NOTHING && instance.isFinishedAction()) {
				instance.setAction(ActionType.NOTHING, 1);
			}
		}
	}
	
}
