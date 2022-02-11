package rbasamoyai.industrialwarfare.client.entities.models;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.ISpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ThirdPersonItemAnimModel extends AnimatedGeoModel<IAnimatable> {

	@Override
	public ResourceLocation getAnimationFileLocation(IAnimatable animatable) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) animatable;
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return null;
		ItemStack stack = entity.getItemInHand(animEntity.getHand());
		Item item = stack.getItem();
		if (!(item instanceof ISpecialThirdPersonRender)) return null;
		return ((ISpecialThirdPersonRender) item).getAnimationFileLocation(stack, entity);
	}

	@Override
	public ResourceLocation getModelLocation(IAnimatable object) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) object;
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return null;
		ItemStack stack = entity.getItemInHand(animEntity.getHand());
		Item item = stack.getItem();
		if (!(item instanceof ISpecialThirdPersonRender)) return null;
		return ((ISpecialThirdPersonRender) item).getModelLocation(stack, entity);
	}

	@Override
	public ResourceLocation getTextureLocation(IAnimatable object) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) object;
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return null;
		ItemStack stack = entity.getItemInHand(animEntity.getHand());
		Item item = stack.getItem();
		if (!(item instanceof ISpecialThirdPersonRender)) return null;
		return ((ISpecialThirdPersonRender) item).getTextureLocation(stack, entity);
	}
	
}
