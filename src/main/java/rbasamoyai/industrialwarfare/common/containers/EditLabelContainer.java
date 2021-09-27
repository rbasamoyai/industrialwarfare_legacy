package rbasamoyai.industrialwarfare.common.containers;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;

public class EditLabelContainer extends Container {
	
	private final Hand hand;
	private UUID labelUUID;
	private byte labelNum;
	private ITextComponent labelCachedName;
	
	public static EditLabelContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		UUID labelUUID = buf.readUUID();
		ITextComponent labelCachedName = ITextComponent.Serializer.fromJson(buf.readUtf());
		byte labelNum = buf.readByte();
		return new EditLabelContainer(windowId, hand, labelUUID, labelNum, labelCachedName);
	}
	
	public static IContainerProvider getServerContainerProvider(Hand hand, UUID labelUUID, byte labelNum, ITextComponent labelCachedName) {
		return (windowId, playerInv, data) -> new EditLabelContainer(windowId, hand, labelUUID, labelNum, labelCachedName);
	}
	
	private EditLabelContainer(int windowId, Hand hand, UUID labelUUID, byte labelNum, ITextComponent labelCachedName) {
		super(ContainerInit.EDIT_LABEL, windowId);
		this.hand = hand;
		this.labelUUID = labelUUID;
		this.labelNum = labelNum;
		this.labelCachedName = labelCachedName;
	}
	
	public Hand getHand() {
		return this.hand;
	}
	
	public UUID getUUID() {
		return this.labelUUID;
	}
	
	public byte getNum() {
		return this.labelNum;
	}
	
	public ITextComponent getCachedName() {
		return this.labelCachedName;
	}
	
	public void setNum(byte b) {
		this.labelNum = b;
	}
	
	public void setUUID(UUID uuid) {
		this.labelUUID = uuid;
	}
	
	
	public void setCachedName(IFormattableTextComponent tc) {
		this.labelCachedName = tc;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}
	
}
