package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.EditLabelContainer;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCContainer;
import rbasamoyai.industrialwarfare.common.containers.schedule.EditScheduleContainer;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfContainer;
import rbasamoyai.industrialwarfare.common.containers.workstations.NormalWorkstationContainer;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class ContainerInit {


	public static final ContainerType<EditLabelContainer> EDIT_LABEL = null;
	public static final ContainerType<NormalWorkstationContainer> NORMAL_WORKSTATION = null;
	public static final ContainerType<NPCContainer> NPC_BASE = null;
	public static final ContainerType<EditScheduleContainer> SCHEDULE = null;
	public static final ContainerType<TaskScrollContainer> TASK_SCROLL = null;
	public static final ContainerType<TaskScrollShelfContainer> TASK_SCROLL_SHELF = null;
	
	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
		event.getRegistry().registerAll(new ContainerType<?>[] {
			IForgeContainerType.create(EditLabelContainer::getClientContainer).setRegistryName(IndustrialWarfare.MOD_ID, "edit_label"),
			IForgeContainerType.create(NormalWorkstationContainer::getClientContainer).setRegistryName(IndustrialWarfare.MOD_ID, "normal_workstation"),
			IForgeContainerType.create(NPCContainer::getClientContainer).setRegistryName(IndustrialWarfare.MOD_ID, "npc_base"),
			IForgeContainerType.create(EditScheduleContainer::getClientContainer).setRegistryName(IndustrialWarfare.MOD_ID, "schedule"),
			IForgeContainerType.create(TaskScrollContainer::getClientContainer).setRegistryName(IndustrialWarfare.MOD_ID, "task_scroll"),
			IForgeContainerType.create(TaskScrollShelfContainer::getClientContainer).setRegistryName(IndustrialWarfare.MOD_ID, "task_scroll_shelf")
		});
	}
	
}
