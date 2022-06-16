package rbasamoyai.industrialwarfare.client.entities.models;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.ISpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ThirdPersonItemAnimModel extends AnimatedGeoModel<IAnimatable> {

	public static final ResourceLocation NO_MODEL_ANIM_FILE = new ResourceLocation(IndustrialWarfare.MOD_ID, "animations/third_person/no_model_t.animation.json");
	@Override
	public ResourceLocation getAnimationFileLocation(IAnimatable animatable) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) animatable;
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return NO_MODEL_ANIM_FILE;
		ItemStack stack = entity.getItemInHand(animEntity.getHand());
		Item item = stack.getItem();
		if (!(item instanceof ISpecialThirdPersonRender)) return NO_MODEL_ANIM_FILE;
		return ((ISpecialThirdPersonRender) item).getAnimationFileLocation(stack, entity);
	}

	public static final ResourceLocation NO_MODEL_MODEL = new ResourceLocation(IndustrialWarfare.MOD_ID, "geo/third_person/no_model_t.geo.json");
	@Override
	public ResourceLocation getModelLocation(IAnimatable object) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) object;
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return NO_MODEL_MODEL;
		ItemStack stack = entity.getItemInHand(animEntity.getHand());
		Item item = stack.getItem();
		if (!(item instanceof ISpecialThirdPersonRender)) return NO_MODEL_MODEL;
		return ((ISpecialThirdPersonRender) item).getModelLocation(stack, entity);
	}

	public static final ResourceLocation NO_MODEL_TEXTURE = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/item/no_model.png");
	@Override
	public ResourceLocation getTextureLocation(IAnimatable object) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) object;
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return NO_MODEL_TEXTURE;
		ItemStack stack = entity.getItemInHand(animEntity.getHand());
		Item item = stack.getItem();
		if (!(item instanceof ISpecialThirdPersonRender)) return NO_MODEL_TEXTURE;
		return ((ISpecialThirdPersonRender) item).getTextureLocation(stack, entity);
	}
	
	@Override
	public void setMolangQueries(IAnimatable animatable, double currentTick) {
		/* I HATE NPES ON RESOURCE REFRESHES! I HATE NPES ON RESOURCE REFRESHES!
		 *
		 * - We've got you surrounded! Come read your
		 * java.lang.NullPointerException: Rendering item
		 * 			...
		 */
	}
	
}
