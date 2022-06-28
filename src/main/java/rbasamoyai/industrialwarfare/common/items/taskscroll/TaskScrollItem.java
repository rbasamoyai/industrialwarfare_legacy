package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollDataProvider;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollMenu;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class TaskScrollItem extends Item {

	private static final Component TITLE = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".task_scroll.title");
	private static final MutableComponent TOOLTIP_LABEL = new TranslatableComponent("tooltip." + IndustrialWarfare.MOD_ID + ".task_scroll.label");
	
	private static List<TaskScrollCommand> VALID_COMMANDS = null;
	
	private static void initValidCommands() {
		VALID_COMMANDS = Arrays.asList(
				TaskScrollCommandInit.MOVE_TO.get(),
				TaskScrollCommandInit.TAKE_FROM.get(),
				TaskScrollCommandInit.DEPOSIT_AT.get(),
				TaskScrollCommandInit.WAIT_FOR.get(),
				TaskScrollCommandInit.JUMP_TO.get(),
				TaskScrollCommandInit.WORK_AT.get(),
				TaskScrollCommandInit.SWITCH_ORDER.get(),
				TaskScrollCommandInit.EQUIP.get(),
				TaskScrollCommandInit.UNEQUIP.get(),
				TaskScrollCommandInit.PATROL.get()
				);
	}
	
	public TaskScrollItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).stacksTo(1));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		TaskScrollDataProvider provider = new TaskScrollDataProvider();
		if (nbt == null) {
			provider.getCapability(TaskScrollCapability.INSTANCE).ifPresent(h -> {
				h.setList(new ArrayList<>());
				h.setLabel(ItemStack.EMPTY);
			});
		} else {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		return provider;
	}
	
	public static LazyOptional<ITaskScrollData> getDataHandler(ItemStack stack) {
		return stack.getCapability(TaskScrollCapability.INSTANCE);
	}
	
	@Override
	public CompoundTag getShareTag(ItemStack stack) {
		CompoundTag nbt = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> nbt.put("item_cap", h.writeTag(new CompoundTag())));
		return nbt;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundTag nbt) {
		super.readShareTag(stack, nbt);
		
		if (nbt == null) return;
		
		if (nbt.contains("creativeData", Tag.TAG_COMPOUND)) {
			readCreativeData(stack, nbt.getCompound("creativeData"));
			nbt.remove("creativeData");
			return;
		}
		
		getDataHandler(stack).ifPresent(h -> h.readTag(nbt.getCompound("item_cap")));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack handItem = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer) {
			LazyOptional<ITaskScrollData> optional = getDataHandler(handItem);
			
			int stackIndex = player.getInventory().selected;
			int maxOrderCount = optional.map(ITaskScrollData::getMaxListSize).orElse(0);
			
			List<TaskScrollOrder> orderList = optional.map(ITaskScrollData::getList).orElse(new ArrayList<>());
			
			ItemStack labelItem = optional.map(ITaskScrollData::getLabel).orElse(ItemStack.EMPTY);
			
			if (VALID_COMMANDS == null) {
				initValidCommands();
			}
			
			MenuConstructor provider = TaskScrollMenu.getServerContainerProvider(handItem, VALID_COMMANDS, stackIndex, hand);
			MenuProvider namedProvider = new SimpleMenuProvider(provider, TITLE);
			
			NetworkHooks.openGui((ServerPlayer) player, namedProvider, buf -> {
				buf
						.writeVarInt(stackIndex)
						.writeVarInt(maxOrderCount)
						.writeVarInt(VALID_COMMANDS.size());
				
				VALID_COMMANDS.forEach(cmd -> buf.writeResourceLocation(cmd.getRegistryName()));
				buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
				
				buf.writeVarInt(orderList.size());
				orderList.forEach(o -> o.toNetwork(buf));
				
				buf.writeItem(labelItem);
			});
		}
		return InteractionResultHolder.sidedSuccess(handItem, level.isClientSide);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_LABEL,
				getDataHandler(stack)
					.map(h -> {
						ItemStack label = h.getLabel();
						return label.isEmpty() ? TooltipUtils.NOT_AVAILABLE : (MutableComponent) h.getLabel().getHoverName();
					})
					.orElse(TooltipUtils.NOT_AVAILABLE)
				));
	}
	
	public static ItemStack creativeStack() {
		ItemStack stack = new ItemStack(ItemInit.TASK_SCROLL.get());
		getDataHandler(stack).ifPresent(h -> {
			h.setList(new ArrayList<>());
			h.setLabel(ItemStack.EMPTY);
		});
		stack.getOrCreateTag().put("creativeData", getCreativeData(stack));
		return stack;
	}
	
	public static CompoundTag getCreativeData(ItemStack stack) {
		CompoundTag nbt = new CompoundTag();
		getDataHandler(stack).ifPresent(h -> {
			ListTag orderList = new ListTag();
			h.getList()
			.stream()
			.map(TaskScrollOrder::serializeNBT)
			.forEach(orderList::add);
			
			nbt.put("orderList", orderList);
			nbt.put("labelItem", h.getLabel().serializeNBT());
		});
		return nbt;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundTag nbt) {
		getDataHandler(stack).ifPresent(h -> {
			ListTag orderTags = nbt.getList("orderList", Tag.TAG_COMPOUND);
			List<TaskScrollOrder> orderList = orderTags.stream()
					.map(ot -> {
						TaskScrollOrder order = TaskScrollOrder.empty(TaskScrollCommandInit.MOVE_TO.get());
						order.deserializeNBT((CompoundTag) ot);
						return order;
					}).collect(Collectors.toList());
			h.setList(orderList);
			h.setLabel(ItemStack.of(nbt.getCompound("labelItem")));
		});
	}
	
}
