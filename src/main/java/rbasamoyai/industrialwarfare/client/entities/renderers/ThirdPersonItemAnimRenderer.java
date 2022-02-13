package rbasamoyai.industrialwarfare.client.entities.renderers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import rbasamoyai.industrialwarfare.client.entities.models.ThirdPersonItemAnimModel;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.ISpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoReplacedEntityRenderer;

public class ThirdPersonItemAnimRenderer extends GeoReplacedEntityRenderer<ThirdPersonItemAnimEntity> {
	
	private LivingEntity entity;
	private ThirdPersonItemAnimEntity currentAnimEntity;
	private IRenderTypeBuffer currentBuffer;
	private RenderType renderType;
	private float partialTicks;
	
	private final Set<String> hiddenBones = new HashSet<>();
	private final Map<String, Vector3f> queuedBoneMovements = new HashMap<>();
	
	private boolean lockedLimbs = false;
	
	public ThirdPersonItemAnimRenderer(EntityRendererManager manager, ThirdPersonItemAnimEntity entity) {
		super(manager, new ThirdPersonItemAnimModel(), entity);
	}
	
	@Override
	public void render(Entity entity, IAnimatable animatable, float entityYaw, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
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
	public void renderRecursively(GeoBone bone, MatrixStack stack, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		stack.pushPose();
		
		ItemStack itemStack = this.entity.getItemInHand(this.currentAnimEntity.getHand());
		Item item = itemStack.getItem();
		
		if (!(item instanceof ISpecialThirdPersonRender)) return;
		ISpecialThirdPersonRender stpr = (ISpecialThirdPersonRender) item;
		stpr.onRenderRecursively(itemStack, this.entity, this.partialTicks, bone, stack, this.currentBuffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		String name = bone.getName();
		bone.setHidden(this.hiddenBones.contains(name));
		
		float boneAlpha = stpr.getBoneAlpha(itemStack, this.entity, bone, alpha);
		if (this.queuedBoneMovements.containsKey(name)) {
			Vector3f pos = this.queuedBoneMovements.get(name);
			bone.setPositionX(pos.x());
			bone.setPositionY(pos.y());
			bone.setPositionZ(pos.z());
		}
		
		super.renderRecursively(bone, stack, this.currentBuffer.getBuffer(this.renderType), packedLightIn, packedOverlayIn, red, green, blue, boneAlpha);
		stack.popPose();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public RenderType getRenderType(Object animatable, float partialTicks, MatrixStack stack,
			IRenderTypeBuffer renderTypeBuffer, IVertexBuilder vertexBuilder, int packedLightIn,
			ResourceLocation textureLocation) {
		this.renderType = super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn,
				textureLocation);
		return this.renderType;
	}
	
	public void setBoneVisibility(String name, boolean isVisible) {
		if (isVisible) {
			this.hiddenBones.add(name);
		} else {
			this.hiddenBones.remove(name);
		}
	}
	
	public void lockLimbs(boolean lock) { this.lockedLimbs = lock; }
	public boolean areLimbsLocked() { return this.lockedLimbs; }
	
	public void moveBone(String name, float x, float y, float z) {
		this.queuedBoneMovements.put(name, new Vector3f(x, y, z));
	}
	
	public static <E extends IAnimatable> void parse(CustomInstructionKeyframeEvent<E> event) {
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
		
		for (List<String> tokens : instructionTokens) {
			String firstTok = tokens.get(0);
			if (firstTok.equals("set_hidden")) {
				String boneName = tokens.get(1);
				boolean hidden = Boolean.valueOf(tokens.get(2));
				renderer.setBoneVisibility(boneName, hidden);
			} else if (firstTok.equals("lock_limbs")) {
				boolean lock = Boolean.valueOf(tokens.get(1));
				renderer.lockLimbs(lock);
			} else if (firstTok.equals("move")) {
				String boneName = tokens.get(1);
				float x = Float.valueOf(tokens.get(2));
				float y = Float.valueOf(tokens.get(3));
				float z = Float.valueOf(tokens.get(4));
				renderer.moveBone(boneName, x, y, z);
			}
		}
	}
	
	@Override public boolean shouldShowName(Entity entity) { return false; }
	
}
