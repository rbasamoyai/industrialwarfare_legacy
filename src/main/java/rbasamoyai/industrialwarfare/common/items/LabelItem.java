package rbasamoyai.industrialwarfare.common.items;

import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.ILabelItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.LabelItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.LabelItemDataProvider;
import rbasamoyai.industrialwarfare.common.containers.EditLabelContainer;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class LabelItem extends Item {

	private static final IFormattableTextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".edit_label.title");
	
	public LabelItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_GENERAL));
		
		this.setRegistryName(IndustrialWarfare.MOD_ID, "label");
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		LabelItemDataProvider provider = new LabelItemDataProvider();
		CompoundNBT tag = nbt;
		if (nbt == null) tag = this.defaultNBT(new CompoundNBT());
		else if (nbt.contains("Parent")) tag = nbt.getCompound("Parent");
		provider.deserializeNBT(tag);
		return provider;
	}
	
	public CompoundNBT defaultNBT(CompoundNBT nbt) {
		nbt.putUUID(LabelItemDataCapability.TAG_NPC_UUID, new UUID(0L, 0L));
		nbt.putString(LabelItemDataCapability.TAG_CACHED_NAME, ITextComponent.Serializer.toJson(StringTextComponent.EMPTY));
		return nbt;
	}
	
	public static LazyOptional<ILabelItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			nbt.put("item_cap", LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY.writeNBT(h, null));
		});
		return nbt;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundNBT nbt) {
		super.readShareTag(stack, nbt);
		
		if (nbt != null) {
			getDataHandler(stack).ifPresent(h -> {
				LabelItemDataCapability.LABEL_ITEM_DATA_CAPABILITY.readNBT(h, null, nbt.getCompound("item_cap"));
			});
		}
	}
	
	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof NPCEntity) {
			if (!player.level.isClientSide && entity.isAlive()) {
				ItemStack label = stack.getCount() > 1 ? stack.split(1) : stack;
				
				getDataHandler(label).ifPresent(h -> {
					h.setUUID(entity.getUUID());
					h.cacheName(entity.getCustomName());
				});
			}
			return ActionResultType.sidedSuccess(player.level.isClientSide);
		}
		return super.interactLivingEntity(stack, player, entity, hand);
	}
	
	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack handItem = player.getItemInHand(hand);
		if (!player.level.isClientSide && player instanceof ServerPlayerEntity) {
			LazyOptional<ILabelItemDataHandler> optional = getDataHandler(handItem);
			
			UUID labelUUID = optional.map(ILabelItemDataHandler::getUUID).orElseGet(() -> new UUID(0L, 0L));
			byte labelNum = optional.map(ILabelItemDataHandler::getNumber).orElse((byte) 0);
			ITextComponent labelCachedName = optional.map(ILabelItemDataHandler::getCachedName).orElse(StringTextComponent.EMPTY);
			
			IContainerProvider containerProvider = EditLabelContainer.getServerContainerProvider(hand, labelUUID, labelNum, labelCachedName);
			INamedContainerProvider namedContainerProvider = new SimpleNamedContainerProvider(containerProvider, TITLE);
			
			NetworkHooks.openGui((ServerPlayerEntity) player, namedContainerProvider, buf -> {
				buf.writeBoolean(hand == Hand.MAIN_HAND);
				buf
						.writeUUID(labelUUID)
						.writeUtf(ITextComponent.Serializer.toJson(labelCachedName))
						.writeByte(labelNum);
			});
			
			return ActionResult.success(handItem);
		}
		return ActionResult.pass(handItem);
	}
	
	@Override
	public ITextComponent getName(ItemStack stack) {
		return getDataHandler(stack)
				.map(h -> {
					byte flag = h.getFlags();
					boolean hasNum = (flag & 0b00000001) > 0;
					boolean hasName = (flag & 0b00000010) > 0;
					
					if (flag == 0) return super.getName(stack);
					
					StringTextComponent tc = new StringTextComponent("");
					if (hasNum) tc.append(Byte.toString(h.getNumber()));
					if (hasName) {
						if (TooltipUtils.charLength(tc) > 0) tc.append(" ");
						tc.append(h.getCachedName());
					}
					
					return (ITextComponent) tc;
				})
				.orElseGet(() -> super.getName(stack));
	}

}
