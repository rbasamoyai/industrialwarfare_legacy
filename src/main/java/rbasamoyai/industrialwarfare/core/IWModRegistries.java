package rbasamoyai.industrialwarfare.core;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.taskscrollcmds.TaskScrollCommand;

/**
 * Time to take notes from {@link net.minecraftforge.registries.ForgeRegistries} and {@link net.minecraftforge.registries.GameData}
 * 
 * @author rbasamoyai
 */

@EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public class IWModRegistries {
	
	public static IForgeRegistry<TaskScrollCommand> TASK_SCROLL_COMMANDS = null;
	
	@SubscribeEvent
	public static void buildModRegistries(RegistryEvent.NewRegistry event) {
		IndustrialWarfare.LOGGER.info("Starting registry building for IndustrialWarfare by rbasamoyai");
		
		RegistryBuilder<TaskScrollCommand> taskScrollCommandRegistryBuilder = new RegistryBuilder<>();
		TASK_SCROLL_COMMANDS = taskScrollCommandRegistryBuilder
				.setName(KEY_TASK_COMMANDS.location())
				.setMaxID(MAX_ID)
				.setType(TaskScrollCommand.class)
				.setDefaultKey(new ResourceLocation("move_to"))
				.allowModification()
				.create();
		
		IndustrialWarfare.LOGGER.info("Finished registry building for Industrial Warfare by rbasamoyai");
	}
	
	private static final RegistryKey<Registry<TaskScrollCommand>> KEY_TASK_COMMANDS = key("task_commands");
	
	private static final int MAX_ID = Integer.MAX_VALUE - 1;
	
	/**
	 * Pretty much copied from {@link net.minecraftforge.registries.ForgeRegistries.Keys#key}
	 */
	private static <T> RegistryKey<Registry<T>> key(String id) {
		return RegistryKey.createRegistryKey(new ResourceLocation(IndustrialWarfare.MOD_ID, id));
	}
}
