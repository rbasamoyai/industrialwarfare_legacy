package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollDataProvider;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class TaskScrollItem extends Item {

	private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".task_scroll.title");
	private static final IFormattableTextComponent TOOLTIP_LABEL = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".task_scroll.label");
	
	private static final Supplier<List<TaskScrollCommand>> VALID_COMMANDS = () -> {
		return Arrays.asList(
				TaskScrollCommandInit.MOVE_TO.get(),
				TaskScrollCommandInit.TAKE_FROM.get(),
				TaskScrollCommandInit.DEPOSIT_AT.get(),
				TaskScrollCommandInit.WAIT_FOR.get(),
				TaskScrollCommandInit.JUMP_TO.get(),
				TaskScrollCommandInit.WORK_AT.get(),
				TaskScrollCommandInit.SWITCH_ORDER.get()
				);
	};
	
	public TaskScrollItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_GENERAL).stacksTo(1));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		TaskScrollDataProvider provider = new TaskScrollDataProvider();
		provider.deserializeNBT(nbt == null ? this.defaultNBT(new CompoundNBT()) : nbt);
		return provider;
	}
	
	public CompoundNBT defaultNBT(CompoundNBT nbt) {
		nbt.put(TaskScrollDataCapability.TAG_ORDER_LIST, new ListNBT());
		nbt.put(TaskScrollDataCapability.TAG_LABEL_ITEM, ItemStack.EMPTY.serializeNBT());
		return nbt;
	}
	
	public static LazyOptional<ITaskScrollDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			nbt.put("item_cap", TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY.writeNBT(h, null));
		});
		return nbt;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundNBT nbt) {
		super.readShareTag(stack, nbt);
		
		if (nbt != null) {
			getDataHandler(stack).ifPresent(h -> {
				TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY.readNBT(h, null, nbt.getCompound("item_cap"));
			});
		}
	}
	
	public List<TaskScrollCommand> getValidCommands() {
		return VALID_COMMANDS.get();
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
			
			IContainerProvider provider = TaskScrollContainer.getServerContainerProvider(handItem, this.getValidCommands(), stackIndex, hand);
			INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(provider, TITLE);
			
			NetworkHooks.openGui((ServerPlayerEntity) player, namedProvider, buf -> {
				buf
						.writeVarInt(stackIndex)
						.writeVarInt(maxOrderCount)
						.writeVarInt(this.getValidCommands().size());
				
				this.getValidCommands().forEach(cmd -> buf.writeResourceLocation(cmd.getRegistryName()));
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
	
}
