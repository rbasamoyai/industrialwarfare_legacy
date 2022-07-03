package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyMenu;
import rbasamoyai.industrialwarfare.common.containers.EditLabelMenu;
import rbasamoyai.industrialwarfare.common.containers.LivestockPenMenu;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollMenu;
import rbasamoyai.industrialwarfare.common.containers.WhistleMenu;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleMenu;
import rbasamoyai.industrialwarfare.common.containers.matchcoil.MatchCoilMenu;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCMenu;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationMenu;
import rbasamoyai.industrialwarfare.common.containers.schedule.EditScheduleMenu;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfMenu;
import rbasamoyai.industrialwarfare.common.containers.workstations.ManufacturingBlockMenu;

public class MenuInit {
	
	public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, IndustrialWarfare.MOD_ID);

	public static final RegistryObject<MenuType<AttachmentsRifleMenu>> ATTACHMENTS_RIFLE = CONTAINER_TYPES.register("attachments_rifle",
			() -> IForgeMenuType.create(AttachmentsRifleMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<DiplomacyMenu>> DIPLOMACY = CONTAINER_TYPES.register("diplomacy",
			() -> IForgeMenuType.create(DiplomacyMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<EditLabelMenu>> EDIT_LABEL = CONTAINER_TYPES.register("edit_label",
			() -> IForgeMenuType.create(EditLabelMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<LivestockPenMenu>> LIVESTOCK_PEN = CONTAINER_TYPES.register("livestock_pen",
			() -> IForgeMenuType.create(LivestockPenMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<MatchCoilMenu>> MATCH_COIL = CONTAINER_TYPES.register("match_coil",
			() -> IForgeMenuType.create(MatchCoilMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<ManufacturingBlockMenu>> NORMAL_WORKSTATION = CONTAINER_TYPES.register("normal_workstation",
			() -> IForgeMenuType.create(ManufacturingBlockMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<NPCMenu>> NPC_BASE = CONTAINER_TYPES.register("npc_base",
			() -> IForgeMenuType.create(NPCMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<ResourceStationMenu>> RESOURCE_STATION = CONTAINER_TYPES.register("resource_station",
			() -> IForgeMenuType.create(ResourceStationMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<EditScheduleMenu>> SCHEDULE = CONTAINER_TYPES.register("schedule",
			() -> IForgeMenuType.create(EditScheduleMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<TaskScrollMenu>> TASK_SCROLL = CONTAINER_TYPES.register("task_scroll",
			() -> IForgeMenuType.create(TaskScrollMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<TaskScrollShelfMenu>> TASK_SCROLL_SHELF = CONTAINER_TYPES.register("task_scroll_shelf",
			() -> IForgeMenuType.create(TaskScrollShelfMenu::getClientContainer));
	
	public static final RegistryObject<MenuType<WhistleMenu>> WHISTLE = CONTAINER_TYPES.register("whistle",
			() -> IForgeMenuType.create(WhistleMenu::getClientContainer));
	
}
