package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyContainer;
import rbasamoyai.industrialwarfare.common.containers.EditLabelContainer;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleContainer;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCContainer;
import rbasamoyai.industrialwarfare.common.containers.schedule.EditScheduleContainer;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfContainer;
import rbasamoyai.industrialwarfare.common.containers.workstations.NormalWorkstationContainer;

public class ContainerInit {
	
	public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, IndustrialWarfare.MOD_ID);

	public static final RegistryObject<ContainerType<AttachmentsRifleContainer>> ATTACHMENTS_RIFLE = CONTAINER_TYPES.register("attachments_rifle",
			() -> IForgeContainerType.create(AttachmentsRifleContainer::getClientContainer));
	
	public static final RegistryObject<ContainerType<DiplomacyContainer>> DIPLOMACY = CONTAINER_TYPES.register("diplomacy",
			() -> IForgeContainerType.create(DiplomacyContainer::getClientContainer));
	
	public static final RegistryObject<ContainerType<EditLabelContainer>> EDIT_LABEL = CONTAINER_TYPES.register("edit_label",
			() -> IForgeContainerType.create(EditLabelContainer::getClientContainer));
	
	public static final RegistryObject<ContainerType<NormalWorkstationContainer>> NORMAL_WORKSTATION = CONTAINER_TYPES.register("normal_workstation",
			() -> IForgeContainerType.create(NormalWorkstationContainer::getClientContainer));
	
	public static final RegistryObject<ContainerType<NPCContainer>> NPC_BASE = CONTAINER_TYPES.register("npc_base",
			() -> IForgeContainerType.create(NPCContainer::getClientContainer));
	
	public static final RegistryObject<ContainerType<EditScheduleContainer>> SCHEDULE = CONTAINER_TYPES.register("schedule",
			() -> IForgeContainerType.create(EditScheduleContainer::getClientContainer));
	
	public static final RegistryObject<ContainerType<TaskScrollContainer>> TASK_SCROLL = CONTAINER_TYPES.register("task_scroll",
			() -> IForgeContainerType.create(TaskScrollContainer::getClientContainer));
	
	public static final RegistryObject<ContainerType<TaskScrollShelfContainer>> TASK_SCROLL_SHELF = CONTAINER_TYPES.register("task_scroll_shelf",
			() -> IForgeContainerType.create(TaskScrollShelfContainer::getClientContainer));
	
}
