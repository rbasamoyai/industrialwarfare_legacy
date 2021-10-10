package rbasamoyai.industrialwarfare.core.init;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
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
	public static final MemoryModuleType<Integer> CURRENT_INSTRUCTION_INDEX = null;
	public static final MemoryModuleType<Boolean> EXECUTING_INSTRUCTION = null;
	public static final MemoryModuleType<Boolean> STOP_EXECUTION = null;
	public static final MemoryModuleType<Long> WAIT_FOR = null;
	public static final MemoryModuleType<Boolean> WORKING = null;
	
	@SubscribeEvent
	public static void registerMemoryModuleTypes(RegistryEvent.Register<MemoryModuleType<?>> event) {
		event.getRegistry().registerAll(new MemoryModuleType<?>[] {
			new MemoryModuleType<>(Optional.of(Codec.BOOL)).setRegistryName(IndustrialWarfare.MOD_ID, "cant_interface"),
			new MemoryModuleType<>(Optional.of(Codec.INT)).setRegistryName(IndustrialWarfare.MOD_ID, "current_instruction_index"),
			new MemoryModuleType<>(Optional.of(Codec.BOOL)).setRegistryName(IndustrialWarfare.MOD_ID, "executing_instruction"),
			new MemoryModuleType<>(Optional.of(Codec.BOOL)).setRegistryName(IndustrialWarfare.MOD_ID, "stop_execution"),
			new MemoryModuleType<>(Optional.of(Codec.LONG)).setRegistryName(IndustrialWarfare.MOD_ID, "wait_for"),
			new MemoryModuleType<>(Optional.of(Codec.BOOL)).setRegistryName(IndustrialWarfare.MOD_ID, "working")
		});
	}
	
}
