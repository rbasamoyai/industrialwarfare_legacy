package rbasamoyai.industrialwarfare.client.entities.renderers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.client.entities.models.ThirdPersonItemAnimModel;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.SpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoReplacedEntityRenderer;

public class ThirdPersonItemAnimRenderer extends GeoReplacedEntityRenderer<ThirdPersonItemAnimEntity> {
	
	private LivingEntity entity;
	private ThirdPersonItemAnimEntity currentAnimEntity;
	private MultiBufferSource currentBuffer;
	private RenderType renderType;
	private float partialTicks;
	
	private final Set<String> hiddenBones = new HashSet<>();
	private final Set<String> suppressedBones = new HashSet<>();
	private final Map<String, Vector3f> queuedBoneSetMovements = new HashMap<>();
	private final Map<String, Vector3f> queuedBoneSetRotations = new HashMap<>();
	private final Map<String, Vector3f> queuedBoneAddRotations = new HashMap<>();
	
	private boolean lockedLimbs = false;
	
	public ThirdPersonItemAnimRenderer(EntityRendererProvider.Context context, ThirdPersonItemAnimEntity entity) {
		super(context, new ThirdPersonItemAnimModel(), entity);
	}
	
	@Override
	public void render(Entity entity, IAnimatable animatable, float entityYaw, float partialTicks, PoseStack stack,
			MultiBufferSource bufferIn, int packedLightIn) {
		if (!(entity instanceof LivingEntity)) { // Check stolen from GeoReplacedEntityRenderer
			throw (new RuntimeException("Replaced renderer was not an instanceof LivingEntity"));
		}
		
		this.entity = (LivingEntity) entity;
		this.currentAnimEntity = (ThirdPersonItemAnimEntity) animatable;
		this.currentBuffer = bufferIn;
		this.partialTicks = partialTicks;
		
		super.render(entity, animatable, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
	}
	
	@Override
	public void renderRecursively(GeoBone bone, PoseStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		stack.pushPose();
		
		ItemStack itemStack = this.entity.getItemInHand(this.currentAnimEntity.getHand());
		Item item = itemStack.getItem();
		
		if (!(item instanceof SpecialThirdPersonRender)) return;
		SpecialThirdPersonRender stpr = (SpecialThirdPersonRender) item;
		stpr.onRenderRecursively(itemStack, this.entity, this.partialTicks, bone, stack, this.currentBuffer, packedLightIn, packedOverlayIn, red, green, blue, alpha, this);
		
		String name = bone.getName();
		bone.setHidden(this.hiddenBones.contains(name));
		
		if (!this.suppressedBones.contains(name)) {
			if (this.queuedBoneSetMovements.containsKey(name)) {
				Vector3f pos = this.queuedBoneSetMovements.get(name);
				bone.setPositionX(pos.x());
				bone.setPositionY(pos.y());
				bone.setPositionZ(pos.z());
			}
			
			if (this.queuedBoneSetRotations.containsKey(name)) {
				Vector3f rot = this.queuedBoneSetRotations.get(name);
				bone.setRotationX(rot.x());
				bone.setRotationY(rot.y());
				bone.setRotationZ(rot.z());
			}
			
			if (this.queuedBoneAddRotations.containsKey(name)) {
				Vector3f rotAdd = this.queuedBoneAddRotations.get(name);
				bone.setRotationX(bone.getRotationX() + rotAdd.x());
				bone.setRotationY(bone.getRotationY() + rotAdd.y());
				bone.setRotationZ(bone.getRotationZ() + rotAdd.z());
			}
		}
		
		bone.setCubesHidden(stpr.shouldHideCubes(itemStack, this.entity, bone, alpha));
		super.renderRecursively(bone, stack, this.currentBuffer.getBuffer(this.renderType), packedLightIn, packedOverlayIn, red, green, blue, alpha);
		stack.popPose();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public RenderType getRenderType(Object animatable, float partialTicks, PoseStack stack,
			MultiBufferSource bufSrc, VertexConsumer vertexCons, int packedLightIn,
			ResourceLocation textureLocation) {
		this.renderType = super.getRenderType(animatable, partialTicks, stack, bufSrc, vertexCons, packedLightIn,
				textureLocation);
		return this.renderType;
	}
	
	public void hideBone(String name, boolean hide) {
		if (hide) {
			this.hiddenBones.add(name);
		} else {
			this.hiddenBones.remove(name);
		}
	}
	
	public void lockLimbs(boolean lock) { this.lockedLimbs = lock; }
	public boolean areLimbsLocked() { return this.lockedLimbs; }
	
	public void setBonePosition(String name, float x, float y, float z) {
		this.queuedBoneSetMovements.put(name, new Vector3f(x, y, z));
	}
	
	public void setBoneRotation(String name, float x, float y, float z) {
		this.queuedBoneSetRotations.put(name, new Vector3f(x, y, z));
	}
	
	public void addToBoneRotation(String name, float x, float y, float z) {
		this.queuedBoneAddRotations.put(name, new Vector3f(x, y, z));
	}
	
	public void suppressModification(String name) { this.suppressedBones.add(name); }
	public void allowModification(String name) { this.suppressedBones.remove(name); }
	
	public static <E extends IAnimatable> void parse(CustomInstructionKeyframeEvent<E> event) {
		if (!(event.getEntity() instanceof ThirdPersonItemAnimEntity)) return;
		
		List<String> instructions = Arrays.stream(event.instructions.split(";")).filter(s -> s.length() > 0).collect(Collectors.toList());
		List<List<String>> instructionTokens =
				instructions
				.stream()
				.map(s -> Arrays.asList(s.split(" ")).stream().filter(tk -> tk.length() > 0).collect(Collectors.toList()))
				.filter(tks -> !tks.isEmpty())
				.collect(Collectors.toList());
		
		if (instructionTokens.isEmpty()) return;
		
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) event.getEntity();
		ThirdPersonItemAnimRenderer renderer = RenderEvents.RENDERER_CACHE.get(animEntity.getUUID());
		if (renderer == null) return;
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return;
		
		ItemStack stack = entity.getItemInHand(animEntity.getHand());
		if (stack.isEmpty()) return;
		Item item = stack.getItem();
		if (!(item instanceof SpecialThirdPersonRender)) return;
		SpecialThirdPersonRender stpr = (SpecialThirdPersonRender) item;
		
		instructionTokens
		.stream()
		.filter(tks -> !tks.isEmpty())
		.forEach(tks -> stpr.interpretThirdPersonInstructions(tks, stack, renderer));
	}
	
	@Override public boolean shouldShowName(Entity entity) { return false; }
	
}
