package rbasamoyai.industrialwarfare.common.containers;

import java.util.HashMap;
import java.util.Map;
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
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.FormationCategory;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.Interval;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.WhistleScreenMessages.SWhistleScreenSync;

public class WhistleContainer extends Container {

	private static final String TAG_CURRENT_MODE = WhistleItem.TAG_CURRENT_MODE;
	private static final String TAG_FORMATION_TYPE = WhistleItem.TAG_FORMATION_TYPE;
	private static final String TAG_DIRTY = WhistleItem.TAG_DIRTY;
	private static final String TAG_FORMATION_CATEGORIES = WhistleItem.TAG_FORMATION_CATEGORIES;
	private static final String TAG_INTERVAL = WhistleItem.TAG_INTERVAL;
	private static final String TAG_CATEGORY_TYPE = WhistleItem.TAG_CATEGORY_TYPE;
	private static final String TAG_ATTACK_TYPE = WhistleItem.TAG_ATTACK_TYPE;
	
	public static WhistleContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		WhistleContainer ct = new WhistleContainer(ContainerInit.WHISTLE.get(), windowId, Optional.empty());
		ct.setInterval(Interval.fromId(buf.readVarInt()));
		ct.setMode(CombatMode.fromId(buf.readVarInt()));
		ct.setFormation(buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES));
		ct.setCategory(ct.getFormation().getCategory());
		
		int sz = buf.readVarInt();
		for (int i = 0; i < sz; ++i) {
			FormationCategory cat = FormationCategory.fromId(buf.readVarInt());
			UnitFormationType<?> formType = buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES);
			FormationAttackType attackType = buf.readRegistryIdUnsafe(IWModRegistries.FORMATION_ATTACK_TYPES);
			
			ct.setCategoryType(cat, formType);
			ct.setCategoryAttackType(cat, attackType);
		}
		
		return ct;
	}
	
	public static IContainerProvider getServerContainerProvider(ItemStack stack) {
		return (windowId, playerInv, player) -> new WhistleContainer(ContainerInit.WHISTLE.get(), windowId, Optional.of(stack));
	}
	
	private Optional<ItemStack> whistle;
	private CombatMode mode;
	private FormationCategory category;
	private UnitFormationType<?> type;
	private Interval interval;
	
	private final Map<FormationCategory, UnitFormationType<?>> formationCategories = new HashMap<>();
	private final Map<FormationCategory, FormationAttackType> attackTypes = new HashMap<>();
	
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
	
	public void setInterval(Interval interval) { this.interval = interval; }
	public Interval getInterval() { return this.interval; }
	
	public void setMode(CombatMode mode) { this.mode = mode; }
	public CombatMode getMode() { return this.mode; }	
	
	public void setCategory(FormationCategory category) { this.category = category; }
	public FormationCategory getCategory() { return this.category; }
	
	public void setFormation(UnitFormationType<?> type) { this.type = type; }
	public UnitFormationType<?> getFormation() { return this.type; }
	
	public void setCategoryType(FormationCategory category, UnitFormationType<?> type) {
		this.formationCategories.put(category, type);
	}
	
	public UnitFormationType<?> getSelectedTypes(FormationCategory category) {
		return this.formationCategories.containsKey(category) ? this.formationCategories.get(category) : category.getDefaultType();
	}
	
	public void setCategoryAttackType(FormationCategory category, FormationAttackType type) {
		this.attackTypes.put(category, type);
	}
	
	public FormationAttackType getSelectedAttackType(FormationCategory category) {
		return this.attackTypes.containsKey(category) ? this.attackTypes.get(category) : FormationAttackTypeInit.FIRE_AT_WILL.get();
	}
	
	public void updateItem(PlayerEntity player) {
		if (!this.whistle.isPresent() || player.level.isClientSide) return;
		ItemStack stack = this.whistle.get();
		Item item = stack.getItem();
		if (!(item instanceof WhistleItem)) return;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.putInt(TAG_INTERVAL, this.interval.getId());
		nbt.putInt(TAG_CURRENT_MODE, this.mode.getId());
		nbt.putString(TAG_FORMATION_TYPE, this.type.getRegistryName().toString());
		nbt.putBoolean(TAG_DIRTY, true);
		
		CompoundNBT formationCategories = nbt.getCompound(TAG_FORMATION_CATEGORIES);
		for (FormationCategory cat : FormationCategory.values()) {
			UnitFormationType<?> formType = this.formationCategories.get(cat);
			FormationAttackType attackType = this.attackTypes.get(cat);
			
			CompoundNBT catTag = formationCategories.getCompound(cat.getTag());
			catTag.putString(TAG_CATEGORY_TYPE, formType.getRegistryName().toString());
			catTag.putString(TAG_ATTACK_TYPE, attackType.getRegistryName().toString());
			formationCategories.put(cat.getTag(), catTag);
		}
		nbt.put(TAG_FORMATION_CATEGORIES, formationCategories);
		
		((WhistleItem) item).updateStance((ServerWorld) player.level, stack, player);
	}
	
	public void updateServer() {
		SWhistleScreenSync msg = new SWhistleScreenSync(this.interval, this.mode, this.type, this.formationCategories, this.attackTypes);
		IWNetwork.CHANNEL.sendToServer(msg);
	}
	
	public void stopWhistle(PlayerEntity player) {
		if (!this.whistle.isPresent() || player.level.isClientSide) return;
		ItemStack stack = this.whistle.get();
		Item item = stack.getItem();
		if (!(item instanceof WhistleItem)) return;
		
		((WhistleItem) item).stopUnits((ServerWorld) player.level, stack, player);
	}
	
}
