package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label;

import java.util.UUID;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class LabelItemDataHandler implements ILabelItemDataHandler {
	
	private UUID npcUUID = new UUID(0L, 0L);
	private byte labelNum = 0;
	private byte flags = 0;
	private ITextComponent cachedName = StringTextComponent.EMPTY;
	
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
	public void cacheName(ITextComponent name) {
		this.cachedName = name;
		this.flags = (byte)(TooltipUtils.charLength(this.cachedName) == 0 ? this.flags & 0b11111101 : this.flags | 0b00000010);
	}

	@Override
	public ITextComponent getCachedName() {
		return this.cachedName;
	}

}
