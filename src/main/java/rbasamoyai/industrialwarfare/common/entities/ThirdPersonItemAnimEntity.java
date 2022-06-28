package rbasamoyai.industrialwarfare.common.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.SpecialThirdPersonRender;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ThirdPersonItemAnimEntity implements IAnimatable {

	private UUID uuid;
	private InteractionHand hand;
	private Map<String, AnimationBuilder> queuedAnims = new HashMap<>();
	private float animSpeed = 1.0f;
	
	public ThirdPersonItemAnimEntity() {
		this(Util.NIL_UUID, InteractionHand.MAIN_HAND);
	}
	
	public ThirdPersonItemAnimEntity(UUID uuid, InteractionHand hand) {
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
		if (!(item instanceof SpecialThirdPersonRender)) return;
		SpecialThirdPersonRender stpr = (SpecialThirdPersonRender) item;

		stpr.getAnimationControlllers(stack, entity).forEach(data::addAnimationController);
	}

	@Override public AnimationFactory getFactory() { return this.factory; }
	
	public UUID getUUID() { return this.uuid; }
	public InteractionHand getHand() { return this.hand; }
	
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
