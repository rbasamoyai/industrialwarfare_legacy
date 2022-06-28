package rbasamoyai.industrialwarfare.client.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.RendersPlayerArms;
import rbasamoyai.industrialwarfare.client.items.renderers.SpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import rbasamoyai.industrialwarfare.common.items.EntityHighlighterItem;
import rbasamoyai.industrialwarfare.common.items.FirstPersonTransform;
import rbasamoyai.industrialwarfare.common.items.FovModifierItem;
import rbasamoyai.industrialwarfare.common.items.HideCrosshair;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.common.items.RenderOverlay;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {

	public static Map<UUID, LivingEntity> ENTITY_CACHE = new HashMap<>();
	public static Map<UUID, ThirdPersonItemAnimEntity> ANIM_ENTITY_CACHE = new HashMap<>();
	public static Map<UUID, ThirdPersonItemAnimRenderer> RENDERER_CACHE = new HashMap<>();

	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		if (event.isCancelable() && !shouldRenderHand(mc.player.getMainHandItem(), mc.player.getOffhandItem(), event.getHand())) {
			event.setCanceled(true);
			return;
		}
		
		BlockEntityWithoutLevelRenderer ister = RenderProperties.get(event.getItemStack()).getItemStackRenderer();
		if (ister instanceof RendersPlayerArms) {
			((RendersPlayerArms) ister).setRenderArms(true);
		}
		
		ItemStack itemstack = event.getItemStack();
		Item item = itemstack.getItem();
		if (item instanceof FirstPersonTransform && ((FirstPersonTransform) item).shouldTransform(itemstack, mc.player)) {
			((FirstPersonTransform) item).transformPoseStack(itemstack, mc.player, event.getPoseStack());
		}
	}
	
	private static boolean shouldRenderHand(ItemStack mainhand, ItemStack offhand, InteractionHand renderHand) {
		BlockEntityWithoutLevelRenderer mhister = RenderProperties.get(mainhand).getItemStackRenderer();
		if (mhister instanceof RendersPlayerArms && !((RendersPlayerArms) mhister).shouldAllowHandRender(mainhand, offhand, renderHand)) {
			return false;
		}
		BlockEntityWithoutLevelRenderer ohister = RenderProperties.get(offhand).getItemStackRenderer();
		return !(ohister instanceof RendersPlayerArms) || ((RendersPlayerArms) ohister).shouldAllowHandRender(mainhand, offhand, renderHand);
	}
	
	@SubscribeEvent
	public static void onRenderOverlayPre(RenderGameOverlayEvent.PreLayer event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		ItemStack weaponStack = mc.player.getMainHandItem();
		Item item = weaponStack.getItem();
		if (event.isCancelable()) {
			if (event.getOverlay() == ForgeIngameGui.CROSSHAIR_ELEMENT
				&& item instanceof HideCrosshair
				&& ((HideCrosshair) item).shouldHideCrosshair(weaponStack)) {
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderOverlayPost(RenderGameOverlayEvent.PostLayer event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		ItemStack itemStack = mc.player.getMainHandItem();
		Item item = itemStack.getItem();
		
		if (event.getOverlay() == ForgeIngameGui.HOTBAR_ELEMENT) {
			if (item instanceof RenderOverlay && mc.screen == null && !mc.options.hideGui) {
				((RenderOverlay) item).renderOverlay(event.getMatrixStack(), event.getPartialTicks());
			}
		}
	}
	
	@SubscribeEvent
	public static void onFOVUpdate(FOVModifierEvent event) {
		Player player = event.getEntity();
		ItemStack stack = player.getMainHandItem();
		Item item = stack.getItem();
		float fovModifier = item instanceof FovModifierItem ? ((FovModifierItem) item).getFovModifier(stack) : 1.0f;
		event.setNewfov(event.getFov() * fovModifier);
	}
	
	@SubscribeEvent
	public static void onRenderLivingPre(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
		Minecraft mc = Minecraft.getInstance();
		LivingEntity entity = event.getEntity();
		
		LivingEntityRenderer<?, ?> renderer = event.getRenderer();
		EntityModel<?> model = renderer.getModel();
		
		float partialTick = event.getPartialTick();
		PoseStack matrixStack = event.getPoseStack();
		MultiBufferSource buffers = event.getMultiBufferSource();
		int packedLight = event.getPackedLight();
		
		if (entity instanceof Player && model instanceof PlayerModel) {
			Pose forced = ((Player) entity).getForcedPose();
			if (forced != null && forced != Pose.CROUCHING && entity.isCrouching()) {
				((PlayerModel<?>) model).crouching = false;
				matrixStack.translate(0.0f, 0.125f, 0.0f);
			}
		}
		
		ItemStack mainhand = entity.getMainHandItem();
		Item mainhandItem = mainhand.getItem();
		if (mainhandItem instanceof SpecialThirdPersonRender && (!(entity instanceof Player) || !((Player) entity).isSpectator())) {
			SpecialThirdPersonRender stpr = (SpecialThirdPersonRender) mainhandItem;
			if (!stpr.shouldSpecialRender(mainhand, entity)) return;
			
			UUID uuid = entity.getUUID();
			
			if (!ENTITY_CACHE.containsKey(uuid)) {
				ENTITY_CACHE.put(uuid, entity);
			}
			
			if (!ANIM_ENTITY_CACHE.containsKey(uuid)) {
				ANIM_ENTITY_CACHE.put(uuid, new ThirdPersonItemAnimEntity(uuid, InteractionHand.MAIN_HAND));
			}
			ThirdPersonItemAnimEntity animEntity = ANIM_ENTITY_CACHE.get(uuid);
			
			if (!RENDERER_CACHE.containsKey(uuid)) {
				EntityRendererProvider.Context context = new EntityRendererProvider.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font);
				RENDERER_CACHE.put(uuid, new ThirdPersonItemAnimRenderer(context, animEntity));
			}
			ThirdPersonItemAnimRenderer animRenderer = RENDERER_CACHE.get(uuid);
			
			float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
			float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
			float dYaw = bodyYaw - headYaw;
			
			matrixStack.pushPose();
			
			stpr.onPreRender(entity, animEntity, dYaw, partialTick, matrixStack, buffers, packedLight, animRenderer);
			animRenderer.render(entity, animEntity, dYaw, partialTick, matrixStack, buffers, packedLight);
			stpr.onJustAfterRender(entity, animEntity, dYaw, partialTick, matrixStack, buffers, packedLight, animRenderer);
			
			matrixStack.popPose();
		}
	}
	
	@SubscribeEvent
	public static void onRenderLivingPost(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
		Minecraft mc = Minecraft.getInstance();
		LivingEntity entity = event.getEntity();
		
		MultiBufferSource buf = event.getMultiBufferSource();
		float partialTick = event.getPartialTick();
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource buffers = event.getMultiBufferSource();
		int packedLight = event.getPackedLight();
		
		if (mc.player != null) {
			ItemStack stack = mc.player.getMainHandItem();
			
			if (stack.getItem() instanceof EntityHighlighterItem && ((EntityHighlighterItem) stack.getItem()).shouldHighlightEntity(stack, entity)) {
				((EntityHighlighterItem) stack.getItem()).renderHighlight(entity, stack, event.getPoseStack(), buf);
			}
		}
		
		ItemStack mainhand = entity.getMainHandItem();
		Item mainhandItem = mainhand.getItem();
		if (mainhandItem instanceof SpecialThirdPersonRender) {
			SpecialThirdPersonRender stpr = (SpecialThirdPersonRender) mainhandItem;
			UUID uuid = entity.getUUID();
			
			if (!ENTITY_CACHE.containsKey(uuid)) {
				ENTITY_CACHE.put(uuid, entity);
			}

			if (!ANIM_ENTITY_CACHE.containsKey(uuid)) {
				ANIM_ENTITY_CACHE.put(uuid, new ThirdPersonItemAnimEntity(uuid, InteractionHand.MAIN_HAND));
			}
			ThirdPersonItemAnimEntity animEntity = ANIM_ENTITY_CACHE.get(uuid);
			
			if (!RENDERER_CACHE.containsKey(uuid)) {
				EntityRendererProvider.Context context = new EntityRendererProvider.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getResourceManager(), mc.getEntityModels(), mc.font);
				RENDERER_CACHE.put(uuid, new ThirdPersonItemAnimRenderer(context, animEntity));
			}
			ThirdPersonItemAnimRenderer animRenderer = RENDERER_CACHE.get(uuid);
			
			float lerpYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
			stpr.onPostRender(entity, animEntity, lerpYaw, partialTick, poseStack, buffers, packedLight, animRenderer);
		}
	}
	
	@SubscribeEvent
	public static void onLogout(LoggedOutEvent event) {
		ENTITY_CACHE.clear();
		ANIM_ENTITY_CACHE.clear();
		RENDERER_CACHE.clear();
	}
	
	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event) {
		List<UUID> toRemove = new ArrayList<>();
		ENTITY_CACHE.forEach((uuid, entity) -> {
			if (entity == null || entity.isRemoved()) toRemove.add(uuid);
		});
		for (UUID uuid : toRemove) {
			ENTITY_CACHE.remove(uuid);
			ANIM_ENTITY_CACHE.remove(uuid);
			RENDERER_CACHE.remove(uuid);
		}
	}
	
	private static final String TAG_LAST_UPDATED_TICK = MatchCordItem.TAG_LAST_UPDATED_TICK;
	
	private static final String MATCH_CORD_EXPIRED_KEY = "tooltip." + IndustrialWarfare.MOD_ID + ".match_cord.expired";
	private static final Component MATCH_CORD_EXPIRED_TEXT = (new TranslatableComponent(MATCH_CORD_EXPIRED_KEY)).withStyle(ChatFormatting.RED);
	private static final Component MATCH_CORD_EXPIRED_TEXT1 = (new TranslatableComponent(MATCH_CORD_EXPIRED_KEY + "1")).withStyle(ChatFormatting.RED);
	
	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		List<Component> tooltip = event.getToolTip();
		ItemStack stack = event.getItemStack();
		CompoundTag nbt = stack.getOrCreateTag();
		Item item = stack.getItem();
		
		if (item == ItemInit.MATCH_CORD.get() && event.getFlags().isAdvanced() && stack.isDamaged() && MatchCordItem.isLit(stack)) {
			int adjustedDamage = Math.min(stack.getMaxDamage(), (int)(event.getPlayer().level.getGameTime() - nbt.getLong(TAG_LAST_UPDATED_TICK) + stack.getDamageValue()));
			
			for (int i = tooltip.size() - 1; i >= 0; --i) {
				Component line = tooltip.get(i);
				if (line instanceof TranslatableComponent && ((TranslatableComponent) line).getKey().equals("item.durability")) {
					tooltip.set(i, new TranslatableComponent("item.durability", stack.getMaxDamage() - adjustedDamage, stack.getMaxDamage()));
				}
			}
			
			if (adjustedDamage >= stack.getMaxDamage()) {
				tooltip.add(1, MATCH_CORD_EXPIRED_TEXT);
				tooltip.add(2, MATCH_CORD_EXPIRED_TEXT1);
			}
		}
	}
	
}
