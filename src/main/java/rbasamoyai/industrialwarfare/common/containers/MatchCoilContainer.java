package rbasamoyai.industrialwarfare.common.containers;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.common.itemhandlers.MatchCoilOutputItemHandler;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.init.SoundEventInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class MatchCoilContainer extends Container {

	private static final int SLOT_SPACING = 18;
	private static final int OUTPUT_SLOT_X = 134;
	private static final int OUTPUT_SLOT_Y = 36;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 84;
	private static final int INVENTORY_ROWS = 3;
	private static final int INVENTORY_COLUMNS = 9;
	private static final int HOTBAR_SLOT_Y = 142;
	private static final int HOTBAR_COUNT = INVENTORY_COLUMNS;
	
	public static final int OUTPUT_SLOT_INDEX = 0;
	public static final int PLAYER_INVENTORY_START_INDEX = OUTPUT_SLOT_INDEX + 1;
	public static final int PLAYER_INVENTORY_END_INDEX = PLAYER_INVENTORY_START_INDEX + INVENTORY_COLUMNS * INVENTORY_ROWS + HOTBAR_COUNT + 1;
	
	public static final int MINIMUM_CORD_LEFT = 20 * 60 * 10;
	public static final int MAX_CORD_CUT = 20 * 60 * 60;
	public static final int TOTAL_CORD_LENGTH = MAX_CORD_CUT - MINIMUM_CORD_LEFT;
	
	private static final String TAG_MAX_DAMAGE = MatchCordItem.TAG_MAX_DAMAGE;
	
	public static IContainerProvider getServerContainerProvider(ItemStack item) {
		return (windowId, playerInv, player) -> new MatchCoilContainer(ContainerInit.MATCH_COIL.get(), windowId, playerInv, Optional.of(item), MatchCoilOutputItemHandler::new);
	}
	
	public static MatchCoilContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		MatchCoilContainer ct = new MatchCoilContainer(ContainerInit.MATCH_COIL.get(), windowId, playerInv, Optional.empty(), c -> new ItemStackHandler(1));
		ct.setCurrentCoilLength(buf.readVarInt());
		return ct;
	}
	
	private final Optional<ItemStack> itemOptional;
	private final IItemHandler outputSlotHandler;
	private final IIntArray data = new IntArray(2);
	private final PlayerInventory playerInv;
	
	protected MatchCoilContainer(ContainerType<? extends MatchCoilContainer> type, int windowId,
			PlayerInventory playerInv, Optional<ItemStack> itemOptional, Function<MatchCoilContainer, IItemHandler> outputSlotHandler) {
		super(type, windowId);
		this.itemOptional = itemOptional;
		this.outputSlotHandler = outputSlotHandler.apply(this);
		this.playerInv = playerInv;
		
		this.addSlot(new SlotItemHandler(this.outputSlotHandler, 0, OUTPUT_SLOT_X, OUTPUT_SLOT_Y) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
		});
		
		for (int i = 0; i < INVENTORY_ROWS; i++) {
			for (int j = 0; j < INVENTORY_COLUMNS; j++) {
				int x = INVENTORY_START_X + SLOT_SPACING * j;
				int y = INVENTORY_START_Y + SLOT_SPACING * i;
				int index = i * INVENTORY_COLUMNS + j + HOTBAR_COUNT;
				this.addSlot(new Slot(this.playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < HOTBAR_COUNT; i++) {
			int x = INVENTORY_START_X + i * SLOT_SPACING;
			if (i == this.playerInv.selected) this.addSlot(new ImmovableGuiItemSlot(playerInv, i, x, HOTBAR_SLOT_Y));
			else this.addSlot(new Slot(this.playerInv, i, x, HOTBAR_SLOT_Y));
		}
		
		// Offhand slot, used for tracking shears durability
		this.addSlot(new ImmovableGuiItemSlot(this.playerInv, 40, 0, 0) {
			@OnlyIn(Dist.CLIENT)
			@Override
			public boolean isActive() {
				return false;
			}
		});
		
		this.itemOptional.ifPresent(s -> {
			this.setCurrentCoilLength(s.getMaxDamage() - s.getDamageValue());
		});
		
		this.addDataSlots(this.data);
		
		this.updateOutput();
	}
	
	public void setCutLength(int length) { this.data.set(0, length); }
	public int getCutLength() { return this.data.get(0); }
	public float getCutLengthScaled() { return (float)(this.getCutLength() - MINIMUM_CORD_LEFT) / (float) TOTAL_CORD_LENGTH; }
	
	public void setCurrentCoilLength(int length) { this.data.set(1, length); }
	public int getCurrentCoilLength() { return this.data.get(1); }
	
	public ItemStack getShears() { return this.playerInv.offhand.get(0); }
	
	public void updateCoil(ItemStack result) {
		if (this.playerInv.player.level.isClientSide) {
			return;
		}
		
		this.itemOptional.ifPresent(stack -> {
			int damage = result.getMaxDamage() - result.getDamageValue();
			this.setCurrentCoilLength(Math.max(this.getCurrentCoilLength() - damage, 0));
			if (this.getCurrentCoilLength() <= MINIMUM_CORD_LEFT) {
				this.playerInv.setItem(this.playerInv.selected, new ItemStack(ItemInit.SPOOL.get()));
				
				int newCoilLength = this.getCurrentCoilLength();
				if (newCoilLength > 0) {
					ItemStack cord = new ItemStack(ItemInit.MATCH_CORD.get());
					cord.getOrCreateTag().putInt(TAG_MAX_DAMAGE, newCoilLength);
					this.playerInv.add(cord);
				}
			} else {
				this.updateOutput();
			}
			stack.hurt(damage, this.playerInv.player.getRandom(), (ServerPlayerEntity) this.playerInv.player);
			Slot stackSlot = this.getSlot(27 + this.playerInv.selected);
			if (stackSlot != null) {
				stackSlot.setChanged();
			}
			
			ItemStack shears = this.playerInv.offhand.get(0);
			if (shears.getItem() == Items.SHEARS) {
				shears.hurtAndBreak(1, this.playerInv.player, e -> {
					e.broadcastBreakEvent(EquipmentSlotType.OFFHAND);
				});
			}
			
			this.playerInv.player.level.playSound(null, this.playerInv.player.blockPosition(), SoundEventInit.CUT_FROM_COIL.get(), SoundCategory.PLAYERS, 1.0f, 1.0f);
		});
	}
	
	@Override
	public void broadcastChanges() {
		PlayerEntity player = this.playerInv.player;
		if (!player.level.isClientSide && player instanceof ServerPlayerEntity) {
			((ServerPlayerEntity) player).ignoreSlotUpdateHack = false;
			super.broadcastChanges();
		}
	}
	
	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		if (index > 0) return ItemStack.EMPTY;
		ItemStack slotCopy = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotCopy = slotStack.copy();
			
			if (!this.moveItemStackTo(slotStack, 1, 37, true)) return ItemStack.EMPTY;
			slot.set(ItemStack.EMPTY);
			slot.onTake(player, slotStack);
			this.updateCoil(slotCopy);
		}	
		
		return slotCopy;
	}
	
	public void updateOutput() {
		this.setCutLength(MathHelper.clamp(this.getCutLength(), MINIMUM_CORD_LEFT, Math.min(this.getCurrentCoilLength(), MAX_CORD_CUT)));
		ItemStack cord = new ItemStack(ItemInit.MATCH_CORD.get());
		cord.getOrCreateTag().putInt(TAG_MAX_DAMAGE, this.getCutLength());
		this.outputSlotHandler.insertItem(0, cord, false);
	}
	
	@Override
	public boolean stillValid(PlayerEntity player) {
		return this.getCurrentCoilLength() > MINIMUM_CORD_LEFT && player.getOffhandItem().getItem() == Items.SHEARS;
	}

}
