package rbasamoyai.industrialwarfare.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.KeyBindingsInit;
import rbasamoyai.industrialwarfare.common.items.ISimultaneousUseAndAttack;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages;
import rbasamoyai.industrialwarfare.core.network.messages.SOpenItemScreen;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class InputEvents {
	
	@SubscribeEvent
	public static void onMouseInput(InputEvent.MouseInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		
		if (player == null || mc.screen != null) return;
		
		if (player.isUsingItem()) {
			Hand hand = player.getUsedItemHand();
			ItemStack useStack = player.getUseItem();
			if (!useStack.isEmpty()) {
				if (useStack.getItem() instanceof ISimultaneousUseAndAttack) {
					while (mc.options.keyAttack.consumeClick()) {
						player.swing(hand);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		
		if (mc.screen != null) return;
		
		if (KeyBindingsInit.ITEM_SCREEN.isDown()) {
			IWNetwork.CHANNEL.sendToServer(new SOpenItemScreen());
		}
		
		if (KeyBindingsInit.DIPLOMACY_SCREEN.isDown()) {
			IWNetwork.CHANNEL.sendToServer(new DiplomacyScreenMessages.SOpenScreen());
			return;
		}
		
		if (KeyBindingsInit.RELOAD_FIREARM.isDown()) {
			IWNetwork.CHANNEL.sendToServer(new FirearmActionMessages.SReloadingFirearm());
		}
	}
	
	@SubscribeEvent
	public static void onClickInput(InputEvent.ClickInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		Hand hand = event.getHand();
		ItemStack stack = mc.player.getItemInHand(hand);
		if (event.isAttack()) {
			if (stack.getItem() instanceof FirearmItem && !FirearmItem.isMeleeing(stack)) {
				event.setCanceled(true);
			}
		}
		if (event.isUseItem()) {
			if (event.isCancelable()) {
				if (stack.getItem() instanceof FirearmItem
					&& !FirearmItem.isMeleeing(stack)
					&& !FirearmItem.isFinishedAction(stack)) {
					//event.setCanceled(true);
					//event.setSwingHand(false);
				}
				if (stack.getItem() == ItemInit.WHISTLE.get()) {
					event.setSwingHand(false);
				}
			}
		}
	}
	
}
