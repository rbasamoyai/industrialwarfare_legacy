package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE)
public class PlayerEvents {

	@SubscribeEvent
	public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		Entity e = event.getEntity();
		if (e == null) return;
		if (!(e instanceof PlayerEntity)) return;
		
		World world = event.getWorld();
		if (world == null) return;
		if (!(world instanceof ServerWorld)) return;
		
		PlayerEntity player = (PlayerEntity) e;
		
		DiplomacySaveData diplomacyData = DiplomacySaveData.get(world);
		if (!diplomacyData.hasPlayerIdTag(PlayerIDTag.of(player))) {
			diplomacyData.initPlayerDiplomacyStatuses(player);
			IndustrialWarfare.LOGGER.info("Initialized diplomacy for new player {} ({}) and updated diplomacy data", player.getGameProfile().getName(), player.getUUID());
		}
	}	
	
}
