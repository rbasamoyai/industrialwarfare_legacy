package rbasamoyai.industrialwarfare.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.PacketDistributor;
import rbasamoyai.industrialwarfare.client.rendering.NothingLayer;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.CQueueEntityAnimMessage;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class AnimUtils {

	/**
	 * Call on server thread
	 */
	public static <S extends Item & ISyncable> void syncItemStackAnim(ItemStack stack, LivingEntity entity, S syncableItem, int animId) {
		final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) entity.level);
		final PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
		GeckoLibNetwork.syncAnimation(target, syncableItem, id, animId);
	}
	
	/**
	 * Call on server thread
	 */
	public static <S extends Item & ISyncable> void syncItemStackAnimToSelf(ItemStack stack, LivingEntity entity, S syncableItem, int animId) {
		if (!(entity instanceof ServerPlayerEntity)) return;
		final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) entity.level);
		final PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity);
		GeckoLibNetwork.syncAnimation(target, syncableItem, id, animId);
	}
	
	
	/**
	 * Call on server thread
	 */
	public static void broadcastThirdPersonAnim(ItemStack stack, LivingEntity entity, String controller, List<Tuple<String, Boolean>> anim, float speed) {
		final CQueueEntityAnimMessage msg = new CQueueEntityAnimMessage(entity.getId(), controller, anim, speed);
		final PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
		IWNetwork.CHANNEL.send(target, msg);
	}
	
	/**
	 * Single animation method.
	 * Call on server thread
	 */
	public static void broadcastThirdPersonAnim(ItemStack stack, LivingEntity entity, String controller, String animName, boolean shouldLoop, float speed) {
		List<Tuple<String, Boolean>> anim = new ArrayList<>();
		anim.add(new Tuple<>(animName, shouldLoop));
		broadcastThirdPersonAnim(stack, entity, controller, anim, speed);
	}
	
	public static void renderPartOverBone(ModelRenderer model, GeoBone bone, MatrixStack stack, IVertexBuilder buffer,
			int packedLightIn, float alpha, int packedOverlayIn) {
		model.setPos(bone.rotationPointX, bone.rotationPointY, bone.rotationPointZ);
		model.xRot = 0.0f;
		model.yRot = 0.0f;
		model.zRot = 0.0f;
		model.render(stack, buffer, packedLightIn, packedOverlayIn, 1.0f, 1.0f, 1.0f, alpha);
	}

	public static void hideLayers(Class<?> clazz, LivingRenderer<?, ?> renderer) {
		// I want to throw up after this
		Field layerField = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
		try {
			@SuppressWarnings("unchecked")
			List<LayerRenderer<?, ?>> layers = (List<LayerRenderer<?, ?>>) layerField.get(renderer);
			for (int i = 0; i < layers.size(); ++i) {
				LayerRenderer<?, ?> layer = layers.get(i);
				Class<?> layerClass = layer.getClass();
				if (layerClass.equals(clazz)) layers.set(i, new NothingLayer<>(renderer, layer));
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong");
		}		
	}
	
	public static void restoreLayers(LivingRenderer<?, ?> renderer) {
		// I also want to throw up after this
		Field layerField = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
		try {
			@SuppressWarnings("unchecked")
			List<LayerRenderer<?, ?>> layers = (List<LayerRenderer<?, ?>>) layerField.get(renderer);
			for (int i = 0; i < layers.size(); ++i) {
				LayerRenderer<?, ?> layer = layers.get(i);
				if (layer instanceof NothingLayer) {
					layers.set(i, ((NothingLayer<?, ?>) layer).getReplacedLayer());  
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong");
		}
	}
	
}
