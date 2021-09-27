package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class QualityItemDataCapability {
	
	public static final String TAG_QUALITY = "quality";
	
	@CapabilityInject(IQualityItemDataHandler.class)
	public static Capability<IQualityItemDataHandler> QUALITY_ITEM_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IQualityItemDataHandler.class, new Storage<IQualityItemDataHandler>(), QualityItemDataHandler::new);
	}
	
	public static class Storage<T extends IQualityItemDataHandler> implements IStorage<T> {

		@Override
		public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putFloat(TAG_QUALITY, instance.getQuality());
			return tag;
		}

		@Override
		public void readNBT(Capability<T> capability, T instance, Direction side,
				INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setQuality(tag.getFloat(TAG_QUALITY));
		}
		
	}
	
}
