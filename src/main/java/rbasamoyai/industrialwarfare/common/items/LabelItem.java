package rbasamoyai.industrialwarfare.common.items;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.ILabelItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.LabelItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.LabelItemDataProvider;
import rbasamoyai.industrialwarfare.common.containers.EditLabelMenu;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class LabelItem extends Item {

	private static final MutableComponent TITLE = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".edit_label.title");
	
	public LabelItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_GENERAL));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		LabelItemDataProvider provider = new LabelItemDataProvider();
		if (nbt == null) {
			provider.getCapability(LabelItemCapability.INSTANCE).ifPresent(h -> {
				h.setUUID(new UUID(0L, 0L));
				h.setNumber((byte) 0);
				h.cacheName(TextComponent.EMPTY);
			});
		} else {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		return provider;
	}
	
	public static LazyOptional<ILabelItemData> getDataHandler(ItemStack stack) {
		return stack.getCapability(LabelItemCapability.INSTANCE);
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
		
		if (nbt != null) {
			getDataHandler(stack).ifPresent(h -> h.readTag(nbt.getCompound("item_cap")));
		}
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		if (entity instanceof NPCEntity) {
			if (!player.level.isClientSide && entity.isAlive()) {
				boolean split = stack.getCount() > 1;
				ItemStack label = split ? stack.split(1) : stack;
				
				getDataHandler(label).ifPresent(h -> {
					h.setUUID(entity.getUUID());
					h.cacheName(entity.getCustomName());
				});
				
				if (split) {
					if (!player.getInventory().add(label)) player.drop(label, false, true);
				}
			}
			return InteractionResult.sidedSuccess(player.level.isClientSide);
		}
		return super.interactLivingEntity(stack, player, entity, hand);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack handItem = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer) {
			LazyOptional<ILabelItemData> optional = getDataHandler(handItem);
			
			UUID labelUUID = optional.map(ILabelItemData::getUUID).orElseGet(() -> new UUID(0L, 0L));
			byte labelNum = optional.map(ILabelItemData::getNumber).orElse((byte) 0);
			Component labelCachedName = optional.map(ILabelItemData::getCachedName).orElse(TextComponent.EMPTY);
			
			MenuConstructor containerProvider = EditLabelMenu.getServerContainerProvider(hand, labelUUID, labelNum, labelCachedName);
			MenuProvider namedContainerProvider = new SimpleMenuProvider(containerProvider, TITLE);
			
			NetworkHooks.openGui((ServerPlayer) player, namedContainerProvider, buf -> {
				buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
				buf
						.writeUUID(labelUUID)
						.writeUtf(Component.Serializer.toJson(labelCachedName))
						.writeByte(labelNum);
			});
		}
		return InteractionResultHolder.sidedSuccess(handItem, level.isClientSide);
	}
	
	@Override
	public Component getName(ItemStack stack) {
		return getDataHandler(stack)
				.map(h -> {
					byte flag = h.getFlags();
					boolean hasNum = (flag & 0b00000001) > 0;
					boolean hasName = (flag & 0b00000010) > 0;
					
					if (flag == 0) return super.getName(stack);
					
					TextComponent tc = new TextComponent("");
					if (hasNum) tc.append(Byte.toString(h.getNumber()));
					if (hasName) {
						if (TooltipUtils.charLength(tc) > 0) tc.append(" ");
						tc.append(h.getCachedName());
					}
					
					return (Component) tc;
				})
				.orElseGet(() -> super.getName(stack));
	}

}
