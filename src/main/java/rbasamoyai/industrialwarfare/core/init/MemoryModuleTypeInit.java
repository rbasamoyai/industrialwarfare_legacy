package rbasamoyai.industrialwarfare.core.init;

import java.util.Optional;

import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class MemoryModuleTypeInit {

	public static final MemoryModuleType<Boolean> CANT_INTERFACE = null;
	public static final MemoryModuleType<GlobalPos> INTERFACE_TARGET = null;
	public static final MemoryModuleType<Boolean> WORKING = null;
	
	@SubscribeEvent
	public static void registerMemoryModuleTypes(RegistryEvent.Register<MemoryModuleType<?>> event) {
		event.getRegistry().registerAll(new MemoryModuleType<?>[] {
			new MemoryModuleType<>(Optional.empty()).setRegistryName(IndustrialWarfare.MOD_ID, "cant_interface"),
			new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)).setRegistryName(IndustrialWarfare.MOD_ID, "interface_target"),
			new MemoryModuleType<>(Optional.empty()).setRegistryName(IndustrialWarfare.MOD_ID, "working")
		});
	}
	
}
