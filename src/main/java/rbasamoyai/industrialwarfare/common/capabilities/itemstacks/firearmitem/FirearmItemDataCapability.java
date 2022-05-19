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
	public static final String TAG_ATTACHMENTS = "attachments";
	public static final String TAG_MAGAZINE_SIZE = "magazineSize";
	public static final String TAG_MAGAZINE = "magazine";
	public static final String TAG_MELEEING = "meleeing";
	public static final String TAG_STATE = "state";
	
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
			tag.put(TAG_ATTACHMENTS, instance.serializeAttachments());
			tag.putInt(TAG_MAGAZINE_SIZE, instance.getMagazineSize());
			tag.put(TAG_MAGAZINE, instance.serializeAmmo());
			tag.putBoolean(TAG_MELEEING, instance.isMeleeing());
			tag.putInt(TAG_STATE, instance.getState());
			return tag;
		}
		
		@Override
		public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
			super.readNBT(capability, instance, side, nbt);
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setAction(ActionType.fromId(tag.getInt(TAG_ACTION)), tag.getInt(TAG_ACTION_TIME));
			instance.deserializeAttachments(tag.getCompound(TAG_ATTACHMENTS));
			instance.setMagazineSize(tag.getInt(TAG_MAGAZINE_SIZE));
			instance.deserializeAmmo(tag.getCompound(TAG_MAGAZINE));
			instance.setMelee(tag.getBoolean(TAG_MELEEING));
			if (instance.getAction() == ActionType.NOTHING && instance.isFinishedAction()) {
				instance.setAction(ActionType.NOTHING, 1);
			}
			instance.setState(tag.getInt(TAG_STATE));
		}
	}
	
}
