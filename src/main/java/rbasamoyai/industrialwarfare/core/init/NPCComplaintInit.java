package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class NPCComplaintInit {
	
	public static final NPCComplaint CANT_ACCESS = null;
	public static final NPCComplaint CANT_DEPOSIT_ITEM = null;
	public static final NPCComplaint CANT_GET_ITEM = null;
	public static final NPCComplaint CANT_OPEN = null;
	public static final NPCComplaint CLEAR = null;
	public static final NPCComplaint INVALID_ORDER = null;
	public static final NPCComplaint NOTHING_HERE = null;
	public static final NPCComplaint TIME_STOPPED = null;
	public static final NPCComplaint TOO_FAR = null;
	
	@SubscribeEvent
	public static void registerNPCComplaints(RegistryEvent.Register<NPCComplaint> event) {
		event.getRegistry().registerAll(new NPCComplaint[] {
				new NPCComplaint("cant_access"),
				new NPCComplaint("cant_deposit_item"),
				new NPCComplaint("cant_get_item"),
				new NPCComplaint("cant_open"),
				new NPCComplaint("clear"),
				new NPCComplaint("invalid_order"),
				new NPCComplaint("nothing_here"),
				new NPCComplaint("time_stopped"),
				new NPCComplaint("too_far")
		});
	}
	
}
