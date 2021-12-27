package rbasamoyai.industrialwarfare.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.items.renderers.IRendersPlayerArms;
import rbasamoyai.industrialwarfare.common.items.IFirstPersonTransform;
import rbasamoyai.industrialwarfare.common.items.IFovModifier;
import rbasamoyai.industrialwarfare.common.items.IHideCrosshair;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {

	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		ItemStack itemStack = event.getItemStack();
		Item item = itemStack.getItem();
		ItemStackTileEntityRenderer ister = itemStack.getItem().getItemStackTileEntityRenderer();
		
		if (ister instanceof IRendersPlayerArms) {
			((IRendersPlayerArms) ister).setRenderArms(true);
		}
		
		if (item instanceof IFirstPersonTransform) {
			if (((IFirstPersonTransform) item).shouldTransform(itemStack, mc.player)) {
				((IFirstPersonTransform) item).transformMatrixStack(itemStack, mc.player, event.getMatrixStack());
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		ItemStack weaponStack = mc.player.getMainHandItem();
		Item item = weaponStack.getItem();
		if (event.isCancelable()
			&& event.getType() == ElementType.CROSSHAIRS
			&& item instanceof IHideCrosshair
			&& ((IHideCrosshair) item).shouldHideCrosshair(weaponStack)) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onFOVUpdate(FOVUpdateEvent event) {
		PlayerEntity player = event.getEntity();
		
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();
		float fovModifier = item instanceof IFovModifier ? ((IFovModifier) item).getFovModifier(stack) : 1.0f;
		event.setNewfov(event.getFov() * fovModifier);
	}
	
}
