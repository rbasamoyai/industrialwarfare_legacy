package rbasamoyai.industrialwarfare.common.containers.attachmentitems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemData;
import rbasamoyai.industrialwarfare.common.containers.ImmovableGuiItemSlot;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.core.init.MenuInit;

public class AttachmentsRifleMenu extends AbstractContainerMenu {
	
	private static final int SLOT_SPACING = 18;
	private static final int INVENTORY_ROWS = 3;
	private static final int INVENTORY_COLUMNS = 9;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 118;
	private static final int HOTBAR_Y = INVENTORY_START_Y + SLOT_SPACING * 3 + 4;
	private static final int HOTBAR_COUNT = INVENTORY_COLUMNS;
	
	private static final int MELEE_SLOT_X = 0;
	private static final int MELEE_SLOT_Y = 0;
	
	private static final int OPTIC_SLOT_X = 18;
	private static final int OPTIC_SLOT_Y = 0;
	
	private final Inventory playerInv;
	
	public static MenuConstructor getServerContainerProvider(ItemStack stack) {
		IItemHandler attachments = FirearmItem.getDataHandler(stack).map(IFirearmItemData::getAttachmentsHandler).orElseGet(() -> new ItemStackHandler(2));
		return (windowId, playerInv, player) -> new AttachmentsRifleMenu(MenuInit.ATTACHMENTS_RIFLE.get(), windowId, playerInv, attachments);
	}
	
	public static AttachmentsRifleMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		return new AttachmentsRifleMenu(MenuInit.ATTACHMENTS_RIFLE.get(), windowId, playerInv, new ItemStackHandler(2));
	}
	
	protected AttachmentsRifleMenu(MenuType<?> type, int windowId, Inventory playerInv, IItemHandler attachments) {
		super(type, windowId);
		
		this.playerInv = playerInv;
		
		for (int i = 0; i < INVENTORY_ROWS; ++i) {
			for (int j = 0; j < INVENTORY_COLUMNS; ++j) {
				int x = INVENTORY_START_X + SLOT_SPACING * j;
				int y = INVENTORY_START_Y + SLOT_SPACING * i;
				int index = INVENTORY_COLUMNS * i + j + HOTBAR_COUNT;
				this.addSlot(new Slot(playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < HOTBAR_COUNT; ++i) {
			int x = INVENTORY_START_X + SLOT_SPACING * i;
			if (i == playerInv.selected) this.addSlot(new ImmovableGuiItemSlot(playerInv, i, x, HOTBAR_Y));
			else this.addSlot(new Slot(playerInv, i, x, HOTBAR_Y));
		}
		
		this.addSlot(new SlotItemHandler(attachments, 0, MELEE_SLOT_X, MELEE_SLOT_Y));
		this.addSlot(new SlotItemHandler(attachments, 1, OPTIC_SLOT_X, OPTIC_SLOT_Y));
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
	public ItemStack getTargetStack() {
		return this.playerInv.getSelected();
	}

}
