package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label;

import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class LabelItemDataCapability {

	public static final String TAG_NPC_UUID = "npcUUID";
	public static final String TAG_LABEL_NUM = "labelNum";
	public static final String TAG_CACHED_NAME = "cachedName";
	public static final String TAG_FLAGS = "flags";
	
	@CapabilityInject(ILabelItemDataHandler.class)
	public static Capability<ILabelItemDataHandler> LABEL_ITEM_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(ILabelItemDataHandler.class, new Storage(), LabelItemDataHandler::new);
	}
	
	public static class Storage implements IStorage<ILabelItemDataHandler> {

		@Override
		public INBT writeNBT(Capability<ILabelItemDataHandler> capability, ILabelItemDataHandler instance, Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putUUID(TAG_NPC_UUID, instance.getUUID());
			tag.putByte(TAG_LABEL_NUM, instance.getNumber());
			if (TooltipUtils.charLength(instance.getCachedName()) > 0) {
				tag.putString(TAG_CACHED_NAME, ITextComponent.Serializer.toJson(instance.getCachedName()));
			}
			tag.putByte(TAG_FLAGS, instance.getFlags());
			return tag;
		}

		@Override
		public void readNBT(Capability<ILabelItemDataHandler> capability, ILabelItemDataHandler instance, Direction side, INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			if (tag.contains(TAG_NPC_UUID)) {
				instance.setUUID(tag.getUUID(TAG_NPC_UUID));
			} else {
				instance.setUUID(new UUID(0L, 0L));
			}
			instance.setNumber(tag.getByte(TAG_LABEL_NUM));
			if (tag.contains(TAG_CACHED_NAME, Constants.NBT.TAG_STRING)) {
				instance.cacheName(ITextComponent.Serializer.fromJson(tag.getString(TAG_CACHED_NAME)));
			} else {
				instance.cacheName(StringTextComponent.EMPTY);
			}
			instance.setFlags(tag.getByte(TAG_FLAGS));
		}
		
	}
	
}
