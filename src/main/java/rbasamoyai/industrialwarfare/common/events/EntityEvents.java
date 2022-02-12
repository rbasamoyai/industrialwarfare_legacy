package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.IProjectilePassThrough;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE)
public class EntityEvents {

	@SubscribeEvent
	public static void onProjectileHit(ProjectileImpactEvent event) {
		if (event.isCancelable()) {
			if (event.getRayTraceResult() instanceof EntityRayTraceResult) {
				if (((EntityRayTraceResult) event.getRayTraceResult()).getEntity() instanceof IProjectilePassThrough) {
					event.setCanceled(true);
				}
			}
		}
	}
	
}
