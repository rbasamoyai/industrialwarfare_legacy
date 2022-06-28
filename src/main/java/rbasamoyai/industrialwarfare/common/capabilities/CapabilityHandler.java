package rbasamoyai.industrialwarfare.common.capabilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataProvider;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.FirearmItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.LabelItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollCapability;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class CapabilityHandler {

	@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
	public static class ModEvents {
		@SubscribeEvent
		public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
			NPCDataCapability.register(event);
			
			QualityItemCapability.register(event);
			PartItemCapability.register(event);
			FirearmItemCapability.register(event);
			RecipeItemCapability.register(event);
			TaskScrollCapability.register(event);
			LabelItemCapability.register(event);
			ScheduleItemCapability.register(event);
		}
	}
	
	@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE)
	public static class ForgeEvents {
		@SubscribeEvent
		public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof NPCEntity) {
				NPCDataProvider provider = new NPCDataProvider();
				event.addCapability(new ResourceLocation(IndustrialWarfare.MOD_ID, "npc_data"), provider);
				event.addListener(provider::invalidate);
			}
		}
		
		@SubscribeEvent
		public static void attachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
			
		}
		
		@SubscribeEvent
		public static void clonePlayerEvent(PlayerEvent.Clone event) {
			
		}
	}
	
}
