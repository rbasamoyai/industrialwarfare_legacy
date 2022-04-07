package rbasamoyai.industrialwarfare.client.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.IRendersPlayerArms;
import rbasamoyai.industrialwarfare.client.items.renderers.ISpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import rbasamoyai.industrialwarfare.common.items.IFirstPersonTransform;
import rbasamoyai.industrialwarfare.common.items.IFovModifier;
import rbasamoyai.industrialwarfare.common.items.IHideCrosshair;
import rbasamoyai.industrialwarfare.common.items.IHighlighterItem;
import rbasamoyai.industrialwarfare.common.items.IRenderOverlay;
import software.bernie.geckolib3.renderers.geo.GeoReplacedEntityRenderer;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class RenderEvents {

	public static Map<UUID, LivingEntity> ENTITY_CACHE = new HashMap<>();
	public static Map<UUID, ThirdPersonItemAnimEntity> ANIM_ENTITY_CACHE = new HashMap<>();
	public static Map<UUID, ThirdPersonItemAnimRenderer> RENDERER_CACHE = new HashMap<>();

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
	public static void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		ElementType type = event.getType();
		
		ItemStack weaponStack = mc.player.getMainHandItem();
		Item item = weaponStack.getItem();
		if (event.isCancelable()) {
			if (type == ElementType.CROSSHAIRS
				&& item instanceof IHideCrosshair
				&& ((IHideCrosshair) item).shouldHideCrosshair(weaponStack)) {
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		ElementType type = event.getType();
		
		ItemStack itemStack = mc.player.getMainHandItem();
		Item item = itemStack.getItem();
		
		if (type == ElementType.HOTBAR) {
			if (item instanceof IRenderOverlay && mc.screen == null && !mc.options.hideGui) {
				((IRenderOverlay) item).renderOverlay(event.getMatrixStack(), event.getPartialTicks());
			}
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
	
	@SubscribeEvent
	public static void onRenderLivingPre(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
		Minecraft mc = Minecraft.getInstance();
		LivingEntity entity = event.getEntity();
		
		LivingRenderer<?, ?> renderer = event.getRenderer();
		EntityModel<?> model = renderer.getModel();
		
		float partialTick = event.getPartialRenderTick();
		MatrixStack matrixStack = event.getMatrixStack();
		IRenderTypeBuffer buffers = event.getBuffers();
		int packedLight = event.getLight();
		
		if (entity instanceof PlayerEntity && model instanceof PlayerModel) {
			Pose forced = ((PlayerEntity) entity).getForcedPose();
			if (forced != null && forced != Pose.CROUCHING && entity.isCrouching()) {
				((PlayerModel<?>) model).crouching = false;
				matrixStack.translate(0.0f, 0.125f, 0.0f);
			}
		}
		
		ItemStack mainhand = entity.getMainHandItem();
		Item mainhandItem = mainhand.getItem();
		if (mainhandItem instanceof ISpecialThirdPersonRender && (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isSpectator())) {
			ISpecialThirdPersonRender stpr = (ISpecialThirdPersonRender) mainhandItem;
			if (!stpr.shouldSpecialRender(mainhand, entity)) return;
			
			UUID uuid = entity.getUUID();
			
			if (!ENTITY_CACHE.containsKey(uuid)) {
				ENTITY_CACHE.put(uuid, entity);
			}
			
			if (!ANIM_ENTITY_CACHE.containsKey(uuid)) {
				ANIM_ENTITY_CACHE.put(uuid, new ThirdPersonItemAnimEntity(uuid, Hand.MAIN_HAND));
			}
			ThirdPersonItemAnimEntity animEntity = ANIM_ENTITY_CACHE.get(uuid);
			
			if (!RENDERER_CACHE.containsKey(uuid)) {
				RENDERER_CACHE.put(uuid, new ThirdPersonItemAnimRenderer(mc.getEntityRenderDispatcher(), animEntity));
			}
			ThirdPersonItemAnimRenderer animRenderer = RENDERER_CACHE.get(uuid);
			
			if (GeoReplacedEntityRenderer.getRenderer(animEntity.getClass()) == null) {
				GeoReplacedEntityRenderer.registerReplacedEntity(animEntity.getClass(), animRenderer);
			}
			
			float headYaw = MathHelper.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
			float bodyYaw = MathHelper.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
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
		
		IRenderTypeBuffer buf = event.getBuffers();
		float partialTick = event.getPartialRenderTick();
		MatrixStack matrixStack = event.getMatrixStack();
		IRenderTypeBuffer buffers = event.getBuffers();
		int packedLight = event.getLight();
		
		if (mc.player != null) {
			ItemStack stack = mc.player.getItemInHand(Hand.MAIN_HAND);
			
			if (stack.getItem() instanceof IHighlighterItem && ((IHighlighterItem) stack.getItem()).shouldHighlightEntity(stack, entity)) {
				((IHighlighterItem) stack.getItem()).renderHighlight(entity, stack, event.getMatrixStack(), buf);
			}
		}
		
		ItemStack mainhand = entity.getMainHandItem();
		Item mainhandItem = mainhand.getItem();
		if (mainhandItem instanceof ISpecialThirdPersonRender) {
			ISpecialThirdPersonRender stpr = (ISpecialThirdPersonRender) mainhandItem;
			UUID uuid = entity.getUUID();
			
			if (!ENTITY_CACHE.containsKey(uuid)) {
				ENTITY_CACHE.put(uuid, entity);
			}

			if (!ANIM_ENTITY_CACHE.containsKey(uuid)) {
				ANIM_ENTITY_CACHE.put(uuid, new ThirdPersonItemAnimEntity(uuid, Hand.MAIN_HAND));
			}
			ThirdPersonItemAnimEntity animEntity = ANIM_ENTITY_CACHE.get(uuid);
			
			if (!RENDERER_CACHE.containsKey(uuid)) {
				RENDERER_CACHE.put(uuid, new ThirdPersonItemAnimRenderer(mc.getEntityRenderDispatcher(), animEntity));
			}
			ThirdPersonItemAnimRenderer animRenderer = RENDERER_CACHE.get(uuid);
			
			float lerpYaw = MathHelper.lerp(partialTick, entity.yRotO, entity.yRot);
			stpr.onPostRender(entity, animEntity, lerpYaw, partialTick, matrixStack, buffers, packedLight, animRenderer);
		}
	}
	
	@SubscribeEvent
	public static void onLogout(LoggedOutEvent event) {
		ENTITY_CACHE.clear();
		ANIM_ENTITY_CACHE.clear();
		RENDERER_CACHE.clear();
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event) {
		List<UUID> toRemove = new ArrayList<>();
		ENTITY_CACHE.forEach((uuid, entity) -> {
			if (entity == null || entity.removed) toRemove.add(uuid);
		});
		for (UUID uuid : toRemove) {
			ENTITY_CACHE.remove(uuid);
			ANIM_ENTITY_CACHE.remove(uuid);
			RENDERER_CACHE.remove(uuid);
		}
	}
	
}
