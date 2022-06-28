package rbasamoyai.industrialwarfare.common.containers;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import rbasamoyai.industrialwarfare.core.init.MenuInit;

public class EditLabelMenu extends AbstractContainerMenu {
	
	private final InteractionHand hand;
	private UUID labelUUID;
	private byte labelNum;
	private Component labelCachedName;
	
	public static EditLabelMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		UUID labelUUID = buf.readUUID();
		Component labelCachedName = Component.Serializer.fromJson(buf.readUtf());
		byte labelNum = buf.readByte();
		return new EditLabelMenu(windowId, hand, labelUUID, labelNum, labelCachedName);
	}
	
	public static MenuConstructor getServerContainerProvider(InteractionHand hand, UUID labelUUID, byte labelNum, Component labelCachedName) {
		return (windowId, playerInv, data) -> new EditLabelMenu(windowId, hand, labelUUID, labelNum, labelCachedName);
	}
	
	private EditLabelMenu(int windowId, InteractionHand hand, UUID labelUUID, byte labelNum, Component labelCachedName) {
		super(MenuInit.EDIT_LABEL.get(), windowId);
		this.hand = hand;
		this.labelUUID = labelUUID;
		this.labelNum = labelNum;
		this.labelCachedName = labelCachedName;
	}
	
	public InteractionHand getHand() {
		return this.hand;
	}
	
	public UUID getUUID() {
		return this.labelUUID;
	}
	
	public byte getNum() {
		return this.labelNum;
	}
	
	public Component getCachedName() {
		return this.labelCachedName;
	}
	
	public void setNum(byte b) {
		this.labelNum = b;
	}
	
	public void setUUID(UUID uuid) {
		this.labelUUID = uuid;
	}
	
	
	public void setCachedName(Component tc) {
		this.labelCachedName = tc;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
}
