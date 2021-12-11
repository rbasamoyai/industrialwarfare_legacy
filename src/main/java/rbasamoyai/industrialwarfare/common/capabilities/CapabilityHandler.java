package rbasamoyai.industrialwarfare.common.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataProvider;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.FirearmItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.LabelItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.ScheduleItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.TaskScrollDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.WorkstationDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.WorkstationDataProvider;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class CapabilityHandler {

	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		NPCDataCapability.register();
		
		WorkstationDataCapability.register();
		
		QualityItemDataCapability.register();
		PartItemDataCapability.register();
		FirearmItemDataCapability.register();
		RecipeItemDataCapability.register();
		TaskScrollDataCapability.register();
		LabelItemDataCapability.register();
		ScheduleItemDataCapability.register();
		
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityHandler::attachEntityCapabilities);
		MinecraftForge.EVENT_BUS.addGenericListener(TileEntity.class, CapabilityHandler::attachTileEntityCapabilities);
		MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, CapabilityHandler::attachItemStackCapabilities);
		
		MinecraftForge.EVENT_BUS.addListener(CapabilityHandler::clonePlayerEvent);
	}
	
	public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof NPCEntity) {
			NPCDataProvider provider = new NPCDataProvider();
			event.addCapability(new ResourceLocation(IndustrialWarfare.MOD_ID, "npc_data"), provider);
			event.addListener(provider::invalidate);
		}
	}
	
	public static void attachTileEntityCapabilities(AttachCapabilitiesEvent<TileEntity> event) {
		if (event.getObject() instanceof WorkstationTileEntity) {
			WorkstationDataProvider provider = new WorkstationDataProvider();
			event.addCapability(new ResourceLocation(IndustrialWarfare.MOD_ID, "workstation_data"), provider);
			event.addListener(provider::invalidate);
		}
	}
	
	public static void attachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		
	}
	
	public static void clonePlayerEvent(PlayerEvent.Clone event) {
		
	}
	
}
