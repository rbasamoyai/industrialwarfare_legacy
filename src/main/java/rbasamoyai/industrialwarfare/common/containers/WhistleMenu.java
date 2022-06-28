package rbasamoyai.industrialwarfare.common.containers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.FormationCategory;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.Interval;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.init.MenuInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.WhistleScreenMessages.SWhistleScreenSync;

public class WhistleMenu extends AbstractContainerMenu {

	private static final String TAG_CURRENT_MODE = WhistleItem.TAG_CURRENT_MODE;
	private static final String TAG_FORMATION_TYPE = WhistleItem.TAG_FORMATION_TYPE;
	private static final String TAG_DIRTY = WhistleItem.TAG_DIRTY;
	private static final String TAG_FORMATION_CATEGORIES = WhistleItem.TAG_FORMATION_CATEGORIES;
	private static final String TAG_INTERVAL = WhistleItem.TAG_INTERVAL;
	private static final String TAG_CATEGORY_TYPE = WhistleItem.TAG_CATEGORY_TYPE;
	private static final String TAG_ATTACK_TYPE = WhistleItem.TAG_ATTACK_TYPE;
	private static final String TAG_UPDATE_FORMATION = WhistleItem.TAG_UPDATE_FORMATION;
	
	public static WhistleMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		WhistleMenu ct = new WhistleMenu(MenuInit.WHISTLE.get(), windowId, Optional.empty());
		ct.setInterval(Interval.fromId(buf.readVarInt()));
		ct.setMode(CombatMode.fromId(buf.readVarInt()));
		ct.setFormation(buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES.get()));
		ct.setCategory(ct.getFormation().getCategory());
		
		int sz = buf.readVarInt();
		for (int i = 0; i < sz; ++i) {
			FormationCategory cat = FormationCategory.fromId(buf.readVarInt());
			UnitFormationType<?> formType = buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES.get());
			FormationAttackType attackType = buf.readRegistryIdUnsafe(IWModRegistries.FORMATION_ATTACK_TYPES.get());
			
			ct.setCategoryType(cat, formType);
			ct.setCategoryAttackType(cat, attackType);
		}
		
		return ct;
	}
	
	public static MenuConstructor getServerContainerProvider(ItemStack stack) {
		return (windowId, playerInv, player) -> new WhistleMenu(MenuInit.WHISTLE.get(), windowId, Optional.of(stack));
	}
	
	private Optional<ItemStack> whistle;
	private CombatMode mode;
	private FormationCategory category;
	private UnitFormationType<?> type;
	private Interval interval;
	
	private final Map<FormationCategory, UnitFormationType<?>> formationCategories = new HashMap<>();
	private final Map<FormationCategory, FormationAttackType> attackTypes = new HashMap<>();
	
	protected WhistleMenu(MenuType<?> type, int windowId, Optional<ItemStack> whistle) {
		super(type, windowId);
		this.whistle = whistle;
		if (this.whistle.isPresent()) {
			ItemStack stack = this.whistle.get();
			CompoundTag nbt = stack.getOrCreateTag();
			
			this.mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
			this.interval = Interval.fromId(nbt.getInt(TAG_INTERVAL));
			
			if (nbt.contains(TAG_FORMATION_TYPE)) {
				ResourceLocation typeLoc = new ResourceLocation(nbt.getString(TAG_FORMATION_TYPE));
				this.type = IWModRegistries.UNIT_FORMATION_TYPES.get().getValue(typeLoc);
			} else {
				this.type = UnitFormationTypeInit.LINE_10W3D.get(); 
			}
			this.category = this.type.getCategory();
			this.setCategoryType(this.category, this.type);
		}
	}

	@Override
	public boolean stillValid(Player player) {
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
	
	public void updateItem(Player player) {
		if (!this.whistle.isPresent() || player.level.isClientSide) return;
		ItemStack stack = this.whistle.get();
		Item item = stack.getItem();
		if (!(item instanceof WhistleItem)) return;
		
		CompoundTag nbt = stack.getOrCreateTag();
		nbt.putInt(TAG_INTERVAL, this.interval.getId());
		nbt.putInt(TAG_CURRENT_MODE, this.mode.getId());
		
		String typeStr = this.type.getRegistryName().toString();
	if (nbt.contains(TAG_FORMATION_TYPE, Tag.TAG_STRING) && !nbt.getString(TAG_FORMATION_TYPE).equals(typeStr)) {
			nbt.putBoolean(TAG_UPDATE_FORMATION, true);
		}
		
		nbt.putString(TAG_FORMATION_TYPE, typeStr);
		nbt.putBoolean(TAG_DIRTY, true);
		
		CompoundTag formationCategories = nbt.getCompound(TAG_FORMATION_CATEGORIES);
		for (FormationCategory cat : FormationCategory.values()) {
			UnitFormationType<?> formType = this.formationCategories.get(cat);
			FormationAttackType attackType = this.attackTypes.get(cat);
			
			CompoundTag catTag = formationCategories.getCompound(cat.getTag());
			catTag.putString(TAG_CATEGORY_TYPE, formType.getRegistryName().toString());
			catTag.putString(TAG_ATTACK_TYPE, attackType.getRegistryName().toString());
			formationCategories.put(cat.getTag(), catTag);
		}
		nbt.put(TAG_FORMATION_CATEGORIES, formationCategories);
		
		((WhistleItem) item).updateStance((ServerLevel) player.level, stack, player);
	}
	
	public void updateServer() {
		SWhistleScreenSync msg = new SWhistleScreenSync(this.interval, this.mode, this.type, this.formationCategories, this.attackTypes);
		IWNetwork.CHANNEL.sendToServer(msg);
	}
	
	public void stopWhistle(Player player) {
		if (!this.whistle.isPresent() || player.level.isClientSide) return;
		ItemStack stack = this.whistle.get();
		Item item = stack.getItem();
		if (!(item instanceof WhistleItem)) return;
		
		((WhistleItem) item).stopUnits((ServerLevel) player.level, stack, player);
	}
	
}
