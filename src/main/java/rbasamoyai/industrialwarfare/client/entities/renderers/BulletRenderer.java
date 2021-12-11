package rbasamoyai.industrialwarfare.client.entities.renderers;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.BulletEntity;

public class BulletRenderer extends EntityRenderer<BulletEntity> {

	// Not to point to an actual texture file, the entity will be invisible
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/entity/bullet.png");
	
	public BulletRenderer(EntityRendererManager manager) {
		super(manager);
	}
	
	@Override
	public ResourceLocation getTextureLocation(BulletEntity entity) {
		return TEXTURE_LOCATION;
	}
	
	@Override
	public boolean shouldRender(BulletEntity entity, ClippingHelper helper, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
		return false;
	}
	
}
