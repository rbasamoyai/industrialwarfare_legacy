package rbasamoyai.industrialwarfare.common.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataProvider;
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

public class CapabilityHandler {

	public void registerCapabilities() {
		NPCDataCapability.register();
		
		WorkstationDataCapability.register();
		
		QualityItemDataCapability.register();
		PartItemDataCapability.register();
		RecipeItemDataCapability.register();
		TaskScrollDataCapability.register();
		LabelItemDataCapability.register();
		ScheduleItemDataCapability.register();
	}
	
	public void addCapabilityListeners() {
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, this::attachEntityCapabilities);
		MinecraftForge.EVENT_BUS.addGenericListener(TileEntity.class, this::attachTileEntityCapabilities);
		MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, this::attachItemStackCapabilities);
		
		MinecraftForge.EVENT_BUS.addListener(this::clonePlayerEvent);
	}
	
	@SubscribeEvent
	public void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof NPCEntity) {
			NPCDataProvider provider = new NPCDataProvider();
			event.addCapability(new ResourceLocation(IndustrialWarfare.MOD_ID, "npc_data"), provider);
			event.addListener(provider::invalidate);
		}
	}
	
	@SubscribeEvent
	public void attachTileEntityCapabilities(AttachCapabilitiesEvent<TileEntity> event) {
		if (event.getObject() instanceof WorkstationTileEntity) {
			WorkstationDataProvider provider = new WorkstationDataProvider();
			event.addCapability(new ResourceLocation(IndustrialWarfare.MOD_ID, "workstation_data"), provider);
			event.addListener(provider::invalidate);
		}
	}
	
	@SubscribeEvent
	public void attachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
		
	}
	
	@SubscribeEvent
	public void clonePlayerEvent(PlayerEvent.Clone event) {
		
	}
	
}
