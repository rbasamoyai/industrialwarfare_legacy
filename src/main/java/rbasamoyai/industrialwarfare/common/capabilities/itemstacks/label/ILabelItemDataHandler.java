package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label;

import java.util.UUID;

import net.minecraft.util.text.ITextComponent;

public interface ILabelItemDataHandler {

	public void setUUID(UUID uuid);
	public UUID getUUID();
	
	public void setNumber(byte b);
	public byte getNumber();
	
	public void setFlags(byte b);
	public byte getFlags();
	
	public void cacheName(ITextComponent name);
	public ITextComponent getCachedName();
	
}
