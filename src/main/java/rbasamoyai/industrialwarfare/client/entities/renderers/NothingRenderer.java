package rbasamoyai.industrialwarfare.client.entities.renderers;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class NothingRenderer<T extends Entity> extends EntityRenderer<T> {

	// Not to point to an actual texture file, the entity will be invisible
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/entity/invalid.png");
	
	public NothingRenderer(EntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return TEXTURE_LOCATION;
	}
	
}
