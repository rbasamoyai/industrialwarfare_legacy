package rbasamoyai.industrialwarfare.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.blockentities.renderers.TaskScrollShelfRenderer;
import rbasamoyai.industrialwarfare.client.items.models.AmericanKepiModel;
import rbasamoyai.industrialwarfare.client.items.models.DragoonHelmetModel;
import rbasamoyai.industrialwarfare.client.items.models.PickelhaubeHighModel;
import rbasamoyai.industrialwarfare.client.items.models.PithHelmetModel;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ModModelLayers {

	public static final ModelLayerLocation SCROLL = create("scroll");
	
	public static final ModelLayerLocation AMERICAN_KEPI = create("american_kepi");
	public static final ModelLayerLocation DRAGOON_HELMET = create("dragoon_helmet");
	public static final ModelLayerLocation PICKELHAUBE_HIGH = create("pickelhaube_high");
	public static final ModelLayerLocation PITH_HELMET = create("pith_helmet");
	
	public static ModelLayerLocation create(String name) {
		return create(name, "main");
	}
			
	public static ModelLayerLocation create(String name, String layer) {
		return new ModelLayerLocation(new ResourceLocation(IndustrialWarfare.MOD_ID, name), layer);
	}
	
	@SubscribeEvent
	public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(SCROLL, TaskScrollShelfRenderer::createScrollLayer);
		event.registerLayerDefinition(AMERICAN_KEPI, AmericanKepiModel::createLayer);
		event.registerLayerDefinition(DRAGOON_HELMET, DragoonHelmetModel::createLayer);
		event.registerLayerDefinition(PICKELHAUBE_HIGH, PickelhaubeHighModel::createLayer);
		event.registerLayerDefinition(PITH_HELMET, PithHelmetModel::createLayer);
	}
	
}
