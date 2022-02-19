package rbasamoyai.industrialwarfare.common.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.ISpecialThirdPersonRender;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ThirdPersonItemAnimEntity implements IAnimatable {

	private UUID uuid;
	private Hand hand;
	private Map<String, AnimationBuilder> queuedAnims = new HashMap<>();
	private float animSpeed = 1.0f;
	
	public ThirdPersonItemAnimEntity() {
		this(Util.NIL_UUID, Hand.MAIN_HAND);
	}
	
	public ThirdPersonItemAnimEntity(UUID uuid, Hand hand) {
		this.uuid = uuid;
		this.hand = hand;
	}
	
	protected AnimationFactory factory = new AnimationFactory(this);
	
	@Override
	public void registerControllers(AnimationData data) {
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(this.uuid);
		if (entity == null) return;
		
		ItemStack stack = entity.getItemInHand(this.hand);
		Item item = stack.getItem();
		if (!(item instanceof ISpecialThirdPersonRender)) return;
		ISpecialThirdPersonRender stpr = (ISpecialThirdPersonRender) item;

		stpr.getAnimationControlllers(stack, entity).forEach(data::addAnimationController);
	}

	@Override public AnimationFactory getFactory() { return this.factory; }
	
	public UUID getUUID() { return this.uuid; }
	public Hand getHand() { return this.hand; }
	
	public void queueAnim(String controller, AnimationBuilder builder) { this.queuedAnims.put(controller, builder); }
	public AnimationBuilder popAndGetAnim(String controller) { return this.queuedAnims.remove(controller); }
	
	public AnimationController<?> getController(String name) {
		ThirdPersonItemAnimRenderer renderer = RenderEvents.RENDERER_CACHE.get(this.uuid);
		if (renderer == null) return null;
		
		@SuppressWarnings("unchecked")
		int id = renderer.getUniqueID(this);
		
		@SuppressWarnings("rawtypes")
		HashMap<String, AnimationController> controllers = this.factory.getOrCreateAnimationData(id).getAnimationControllers();
		
		return controllers.get(name);
	}
	
	public void setSpeed(float speed) { this.animSpeed = speed; }
	public float getSpeed() { return this.animSpeed; }
	
}
