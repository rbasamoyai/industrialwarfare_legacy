package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollDataProvider;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class TaskScrollItem extends Item {

	private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".task_scroll.title");
	private static final IFormattableTextComponent TOOLTIP_LABEL = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".task_scroll.label");
	
	private static List<TaskScrollCommand> VALID_COMMANDS;
	
	public static void initValidCommands() {
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
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		TaskScrollDataProvider provider = new TaskScrollDataProvider();
		if (nbt == null) {
			provider.getCapability(TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY).ifPresent(h -> {
				h.setList(new ArrayList<>());
				h.setLabel(ItemStack.EMPTY);
			});
		} else {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		return provider;
	}
	
	public static LazyOptional<ITaskScrollDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			if (TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY != null)
				nbt.put("item_cap", TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY.writeNBT(h, null));
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
			if (TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY != null)
				TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY.readNBT(h, null, nbt.getCompound("item_cap"));
		});
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack handItem = player.getItemInHand(hand);
		if (!world.isClientSide && player instanceof ServerPlayerEntity) {
			LazyOptional<ITaskScrollDataHandler> optional = getDataHandler(handItem);
			
			int stackIndex = player.inventory.selected;
			int maxOrderCount = optional.map(ITaskScrollDataHandler::getMaxListSize).orElse(0);
			
			List<TaskScrollOrder> orderList = optional.map(ITaskScrollDataHandler::getList).orElse(new ArrayList<>());
			
			ItemStack labelItem = optional.map(ITaskScrollDataHandler::getLabel).orElse(ItemStack.EMPTY);
			
			IContainerProvider provider = TaskScrollContainer.getServerContainerProvider(handItem, VALID_COMMANDS, stackIndex, hand);
			INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(provider, TITLE);
			
			NetworkHooks.openGui((ServerPlayerEntity) player, namedProvider, buf -> {
				buf
						.writeVarInt(stackIndex)
						.writeVarInt(maxOrderCount)
						.writeVarInt(VALID_COMMANDS.size());
				
				VALID_COMMANDS.forEach(cmd -> buf.writeResourceLocation(cmd.getRegistryName()));
				buf.writeBoolean(hand == Hand.MAIN_HAND);
				
				buf.writeVarInt(orderList.size());
				orderList.forEach(o -> o.toNetwork(buf));
				
				buf.writeItem(labelItem);
			});
		}
		return ActionResult.sidedSuccess(handItem, world.isClientSide);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_LABEL,
				getDataHandler(stack)
					.map(h -> {
						ItemStack label = h.getLabel();
						return label.isEmpty() ? TooltipUtils.NOT_AVAILABLE : (IFormattableTextComponent) h.getLabel().getHoverName();
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
	
	public static CompoundNBT getCreativeData(ItemStack stack) {
		CompoundNBT nbt = new CompoundNBT();
		getDataHandler(stack).ifPresent(h -> {
			ListNBT orderList = new ListNBT();
			h.getList().forEach(order -> orderList.add(order.serializeNBT()));
			
			nbt.put(TaskScrollDataCapability.TAG_ORDER_LIST, orderList);
			nbt.put(TaskScrollDataCapability.TAG_LABEL_ITEM, h.getLabel().serializeNBT());
		});
		return nbt;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundNBT nbt) {
		getDataHandler(stack).ifPresent(h -> {
			ListNBT orderTags = nbt.getList(TaskScrollDataCapability.TAG_ORDER_LIST, Constants.NBT.TAG_COMPOUND);
			List<TaskScrollOrder> orderList = orderTags.stream()
					.map(ot -> {
						TaskScrollOrder order = TaskScrollOrder.empty(TaskScrollCommandInit.MOVE_TO.get());
						order.deserializeNBT((CompoundNBT) ot);
						return order;
					}).collect(Collectors.toList());
			h.setList(orderList);
			h.setLabel(ItemStack.of(nbt.getCompound(TaskScrollDataCapability.TAG_LABEL_ITEM)));
		});
	}
	
}
