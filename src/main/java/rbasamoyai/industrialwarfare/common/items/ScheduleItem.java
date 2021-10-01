package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.IScheduleItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemDataProvider;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class ScheduleItem extends Item {

	public ScheduleItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).stacksTo(1));
		
		this.setRegistryName(IndustrialWarfare.MOD_ID, "schedule");
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		ScheduleItemDataProvider provider = new ScheduleItemDataProvider();
		CompoundNBT tag = nbt;
		if (nbt == null) tag = defaultNBT();
		else if (nbt.contains("Parent")) tag = nbt.getCompound("Parent");
		provider.deserializeNBT(tag);
		return provider;
	}

	public static CompoundNBT defaultNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt(ScheduleItemDataCapability.TAG_MAX_MINUTES, 140);
		tag.putInt(ScheduleItemDataCapability.TAG_MAX_SHIFTS, 6);
		tag.put(ScheduleItemDataCapability.TAG_SCHEDULE, new ListNBT());
		return tag;
	}
	
	public static LazyOptional<IScheduleItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER);
	}
	
	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack handItem = player.getItemInHand(hand);
		if (!world.isClientSide && player instanceof ServerPlayerEntity) {
			
		}
		return ActionResult.sidedSuccess(handItem, world.isClientSide);
	}
	
}
