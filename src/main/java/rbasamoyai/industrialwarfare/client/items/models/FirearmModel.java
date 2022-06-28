package rbasamoyai.industrialwarfare.client.items.models;

import net.minecraft.resources.ResourceLocation;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class FirearmModel extends AnimatedGeoModel<FirearmItem> {

	@Override
	public ResourceLocation getModelLocation(FirearmItem object) {
		ResourceLocation loc = object.getRegistryName();
		return new ResourceLocation(loc.getNamespace(), "geo/" + loc.getPath() + ".geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(FirearmItem object) {
		ResourceLocation loc = object.getRegistryName();
		return new ResourceLocation(loc.getNamespace(), "textures/item/" + loc.getPath() + ".png");
	}

	@Override
	public ResourceLocation getAnimationFileLocation(FirearmItem animatable) {
		ResourceLocation loc = animatable.getRegistryName();
		return new ResourceLocation(loc.getNamespace(), "animations/" + loc.getPath() + ".animation.json");
	}
	
	@Override public void setMolangQueries(IAnimatable animatable, double currentTick) {}
	
}
