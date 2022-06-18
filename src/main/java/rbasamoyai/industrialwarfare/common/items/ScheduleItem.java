package rbasamoyai.industrialwarfare.common.items;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.IScheduleItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemDataProvider;
import rbasamoyai.industrialwarfare.common.containers.schedule.EditScheduleContainer;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.ScheduleUtils;

public class ScheduleItem extends Item {

	private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".edit_schedule.title");
	
	public ScheduleItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).stacksTo(1));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		ScheduleItemDataProvider provider = new ScheduleItemDataProvider();
		if (nbt == null) {
			provider.getCapability(ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER).ifPresent(h -> {
				h.setMaxMinutes(70);
				h.setMaxShifts(6);
				h.setSchedule(new ArrayList<>(7));
			});
		} else {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		return provider;
	}
	
	public static LazyOptional<IScheduleItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			nbt.put("item_cap", ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER.writeNBT(h, null));
		});
		return nbt;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundNBT nbt) {
		super.readShareTag(stack, nbt);
		
		if (nbt == null) return;
		
		if (nbt.contains("creativeData", Constants.NBT.TAG_COMPOUND)) {
			readCreativeData(stack, nbt.getCompound("creativeData"));
			nbt.remove("creativeData");
			return;
		}
		
		getDataHandler(stack).ifPresent(h -> {
			ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER.readNBT(h, null, nbt.getCompound("item_cap"));
		});
	}
	
	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack handItem = player.getItemInHand(hand);
		if (!world.isClientSide && player instanceof ServerPlayerEntity) {
			LazyOptional<IScheduleItemDataHandler> optional = getDataHandler(handItem);
			
			List<Pair<Integer, Integer>> schedule = optional.map(IScheduleItemDataHandler::getSchedule).orElseGet(() -> new ArrayList<>(7));
			int maxMinutes = optional.map(IScheduleItemDataHandler::getMaxMinutes).orElse(0);
			int maxShifts = optional.map(IScheduleItemDataHandler::getMaxShifts).orElse(0);
			
			IContainerProvider containerProvider = EditScheduleContainer.getServerContainerProvider(schedule, maxMinutes, maxShifts, hand);
			INamedContainerProvider namedContainerProvider = new SimpleNamedContainerProvider(containerProvider, TITLE);
			NetworkHooks.openGui((ServerPlayerEntity) player, namedContainerProvider, buf -> {
				buf.writeBoolean(hand == Hand.MAIN_HAND);
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
		return ActionResult.sidedSuccess(handItem, world.isClientSide);
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
	
	public static CompoundNBT getCreativeData(ItemStack stack) {
		CompoundNBT nbt = new CompoundNBT();
		getDataHandler(stack).ifPresent(h -> {
			nbt.putInt(ScheduleItemDataCapability.TAG_MAX_MINUTES, h.getMaxMinutes());
			nbt.putInt(ScheduleItemDataCapability.TAG_MAX_SHIFTS, h.getMaxShifts());
			nbt.put(ScheduleItemDataCapability.TAG_SCHEDULE, ScheduleUtils.toNBT(h.getSchedule()));
		});
		return nbt;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundNBT nbt) {
		getDataHandler(stack).ifPresent(h -> {
			h.setMaxMinutes(nbt.getInt(ScheduleItemDataCapability.TAG_MAX_MINUTES));
			h.setMaxShifts(nbt.getInt(ScheduleItemDataCapability.TAG_MAX_SHIFTS));
			h.setSchedule(ScheduleUtils.fromNBT(nbt.getList(ScheduleItemDataCapability.TAG_SCHEDULE, Constants.NBT.TAG_INT_ARRAY)));
		});
	}
	
}
