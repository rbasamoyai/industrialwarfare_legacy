package rbasamoyai.industrialwarfare.common.containers.whistle;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.WhistleScreenMessages.SWhistleScreenSync;

public class WhistleContainer extends Container {

	private static final String TAG_CURRENT_MODE = WhistleItem.TAG_CURRENT_MODE;
	private static final String TAG_FORMATION_TYPE = WhistleItem.TAG_FORMATION_TYPE;
	private static final String TAG_DIRTY = WhistleItem.TAG_DIRTY;
	
	public static WhistleContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		WhistleContainer ct = new WhistleContainer(ContainerInit.WHISTLE.get(), windowId, Optional.empty());
		ct.setMode(CombatMode.fromId(buf.readVarInt()));
		ct.setFormation(buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES));
		return ct;
	}
	
	public static IContainerProvider getServerContainerProvider(ItemStack stack) {
		return (windowId, playerInv, player) -> new WhistleContainer(ContainerInit.WHISTLE.get(), windowId, Optional.of(stack));
	}
	
	private Optional<ItemStack> whistle;
	private CombatMode mode;
	private UnitFormationType<?> type;
	
	protected WhistleContainer(ContainerType<?> type, int windowId, Optional<ItemStack> whistle) {
		super(type, windowId);
		this.whistle = whistle;
		if (this.whistle.isPresent()) {
			ItemStack stack = this.whistle.get();
			CompoundNBT nbt = stack.getOrCreateTag();
			
			this.mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
			
			ResourceLocation typeLoc = nbt.contains(TAG_FORMATION_TYPE)
					? new ResourceLocation(nbt.getString(TAG_FORMATION_TYPE))
					: IWModRegistries.UNIT_FORMATION_TYPES.getDefaultKey(); 
			this.type = IWModRegistries.UNIT_FORMATION_TYPES.getValue(typeLoc);
		}
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}
	
	public void setMode(CombatMode mode) { this.mode = mode; }
	public CombatMode getMode() { return this.mode; }	
	
	public void setFormation(UnitFormationType<?> type) { this.type = type; }
	public UnitFormationType<?> getFormation() { return this.type; }
	
	public void updateItem(PlayerEntity player) {
		if (!this.whistle.isPresent() || player.level.isClientSide) return;
		ItemStack stack = this.whistle.get();
		Item item = stack.getItem();
		if (!(item instanceof WhistleItem)) return;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.putInt(TAG_CURRENT_MODE, this.mode.getId());
		nbt.putString(TAG_FORMATION_TYPE, this.type.getRegistryName().toString());
		nbt.putBoolean(TAG_DIRTY, true);
		
		((WhistleItem) item).updateStance((ServerWorld) player.level, stack, player);
	}
	
	public void updateServer() {
		SWhistleScreenSync msg = new SWhistleScreenSync(this.mode, this.type);
		IWNetwork.CHANNEL.sendToServer(msg);
	}
	
	public void stopWhistle(PlayerEntity player) {
		if (!this.whistle.isPresent() || player.level.isClientSide) return;
		ItemStack stack = this.whistle.get();
		Item item = stack.getItem();
		((WhistleItem) item).stopUnits((ServerWorld) player.level, stack, player);
	}
	
}
