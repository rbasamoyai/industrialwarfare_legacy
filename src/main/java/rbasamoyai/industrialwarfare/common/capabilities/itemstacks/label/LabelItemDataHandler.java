package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class LabelItemDataHandler implements ILabelItemData {
	
	private UUID npcUUID = new UUID(0L, 0L);
	private byte labelNum = 0;
	private byte flags = 0;
	private Component cachedName = TextComponent.EMPTY;
	
	@Override
	public void setUUID(UUID uuid) {
		this.npcUUID = uuid;
	}

	@Override
	public UUID getUUID() {
		return this.npcUUID;
	}
	
	@Override
	public void setNumber(byte b) {
		this.labelNum = b;
		this.flags = (byte)(this.labelNum == 0 ? this.flags & 0b11111110 : this.flags | 0b00000001);
	}
	
	@Override
	public byte getNumber() {
		return this.labelNum;
	}
	
	@Override
	public void setFlags(byte b) {
		this.flags = b;
	}
	
	@Override
	public byte getFlags() {
		return this.flags;
	}

	@Override
	public void cacheName(Component name) {
		this.cachedName = name;
		this.flags = (byte)(TooltipUtils.charLength(this.cachedName) == 0 ? this.flags & 0b11111101 : this.flags | 0b00000010);
	}

	@Override
	public Component getCachedName() {
		return this.cachedName;
	}
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.putUUID("npcUUID", this.npcUUID);
		tag.putByte("labelNum", this.labelNum);
		if (TooltipUtils.charLength(this.cachedName) > 0) {
			tag.putString("cachedName", Component.Serializer.toJson(this.cachedName));
		}
		tag.putByte("flags", this.flags);
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		this.npcUUID = tag.getUUID("npcUUID");
		this.labelNum = tag.getByte("labelNum");
		if (tag.contains("cachedName", Tag.TAG_STRING)) {
			this.cachedName = Component.Serializer.fromJson(tag.getString("cachedName"));
		}
		this.flags = tag.getByte("flags");
	}

}
