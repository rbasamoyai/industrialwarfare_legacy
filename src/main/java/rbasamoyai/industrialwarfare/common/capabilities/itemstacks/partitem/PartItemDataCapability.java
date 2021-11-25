package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataCapability;

public class PartItemDataCapability {

	public static final String TAG_PART_COUNT = "partCount"; // Just a general part count, not as important as the part weight
	public static final String TAG_WEIGHT = "weight"; // Refers to how much the quality of an item will impact part quality aspect the of the crafted product quality
	
	@CapabilityInject(IPartItemDataHandler.class)
	public static Capability<IPartItemDataHandler> PART_ITEM_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IPartItemDataHandler.class, new Storage<>(), PartItemDataHandler::new);
	}
	
	public static class Storage<T extends IPartItemDataHandler> extends QualityItemDataCapability.Storage<T> {
		
		@Override
		public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
			CompoundNBT tag = (CompoundNBT) super.writeNBT(capability, instance, side);
			tag.putInt(TAG_PART_COUNT, instance.getPartCount());
			tag.putFloat(TAG_WEIGHT, instance.getWeight());
			return tag;
		}
		
		@Override
		public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
			super.readNBT(capability, instance, side, nbt);
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setPartCount(tag.getInt(TAG_PART_COUNT));
			instance.setWeight(tag.getFloat(TAG_WEIGHT));
		}
		
	}
	
}
