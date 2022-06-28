package rbasamoyai.industrialwarfare.common.items;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.IScheduleItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemDataProvider;
import rbasamoyai.industrialwarfare.common.containers.schedule.EditScheduleMenu;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.ScheduleUtils;

public class ScheduleItem extends Item {

	private static final Component TITLE = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".edit_schedule.title");
	
	public ScheduleItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).stacksTo(1));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag tag) {
		ScheduleItemDataProvider provider = new ScheduleItemDataProvider();
		if (tag == null) {
			provider.getCapability(ScheduleItemCapability.INSTANCE).ifPresent(h -> {
				h.setMaxMinutes(70);
				h.setMaxShifts(6);
				h.setSchedule(new ArrayList<>(7));
			});
		} else {
			provider.deserializeNBT(tag.contains("Parent") ? tag.getCompound("Parent") : tag);
		}
		return provider;
	}
	
	public static LazyOptional<IScheduleItemData> getDataHandler(ItemStack stack) {
		return stack.getCapability(ScheduleItemCapability.INSTANCE);
	}
	
	@Override
	public CompoundTag getShareTag(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> tag.put("item_cap", h.writeTag(new CompoundTag())));
		return tag;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundTag tag) {
		super.readShareTag(stack, tag);
		
		if (tag == null) return;
		
		if (tag.contains("creativeData", Tag.TAG_COMPOUND)) {
			readCreativeData(stack, tag.getCompound("creativeData"));
			tag.remove("creativeData");
			return;
		}
		
		getDataHandler(stack).ifPresent(h -> h.readTag(tag.getCompound("item_cap")));
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack handItem = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer) {
			LazyOptional<IScheduleItemData> optional = getDataHandler(handItem);
			
			List<Pair<Integer, Integer>> schedule = optional.map(IScheduleItemData::getSchedule).orElseGet(() -> new ArrayList<>(7));
			int maxMinutes = optional.map(IScheduleItemData::getMaxMinutes).orElse(0);
			int maxShifts = optional.map(IScheduleItemData::getMaxShifts).orElse(0);
			
			MenuConstructor constructor = EditScheduleMenu.getServerContainerProvider(schedule, maxMinutes, maxShifts, hand);
			MenuProvider provider = new SimpleMenuProvider(constructor, TITLE);
			NetworkHooks.openGui((ServerPlayer) player, provider, buf -> {
				buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
				buf
						.writeVarInt(maxMinutes)
						.writeVarInt(maxShifts)
						.writeVarInt(schedule.size());
				schedule.forEach(shift -> { // Just to be predictable, writing full-sized ints
					buf.writeInt(shift.getFirst());
					buf.writeInt(shift.getSecond());
				});
			});
		}
		return InteractionResultHolder.sidedSuccess(handItem, level.isClientSide);
	}
	
	public static ItemStack creativeStack() {
		ItemStack stack = new ItemStack(ItemInit.SCHEDULE.get());
		getDataHandler(stack).ifPresent(h -> {
			h.setMaxMinutes(70);
			h.setMaxShifts(6);
			h.setSchedule(new ArrayList<>(7));
		});
		stack.getOrCreateTag().put("creativeData", getCreativeData(stack));
		return stack;
	}
	
	public static CompoundTag getCreativeData(ItemStack stack) {
		CompoundTag tag = new CompoundTag();
		getDataHandler(stack).ifPresent(h -> {
			tag.putInt("maxMinutes", h.getMaxMinutes());
			tag.putInt("maxShifts", h.getMaxShifts());
			tag.put("schedule", ScheduleUtils.toTag(h.getSchedule()));
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundTag tag) {
		getDataHandler(stack).ifPresent(h -> {
			h.setMaxMinutes(tag.getInt("maxMinutes"));
			h.setMaxShifts(tag.getInt("maxShifts"));
			h.setSchedule(ScheduleUtils.fromTag(tag.getList("schedule", Tag.TAG_INT_ARRAY)));
		});
	}
	
}
