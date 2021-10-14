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
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class MemoryModuleTypeInit {

	public static final MemoryModuleType<NPCComplaint> COMPLAINT = null;
	public static final MemoryModuleType<Integer> CURRENT_INSTRUCTION_INDEX = null;
	public static final MemoryModuleType<Boolean> EXECUTING_INSTRUCTION = null;
	public static final MemoryModuleType<Integer> JUMP_TO = null;
	public static final MemoryModuleType<Boolean> STOP_EXECUTION = null;
	public static final MemoryModuleType<Long> WAIT_FOR = null;
	public static final MemoryModuleType<Boolean> WORKING = null;
	
	@SubscribeEvent
	public static void registerMemoryModuleTypes(RegistryEvent.Register<MemoryModuleType<?>> event) {
		event.getRegistry().registerAll(new MemoryModuleType<?>[] {
			new MemoryModuleType<>(Optional.of(NPCComplaint.CODEC)).setRegistryName(IndustrialWarfare.MOD_ID, "complaint"),
			new MemoryModuleType<>(Optional.of(Codec.INT)).setRegistryName(IndustrialWarfare.MOD_ID, "current_instruction_index"),
			new MemoryModuleType<>(Optional.of(Codec.BOOL)).setRegistryName(IndustrialWarfare.MOD_ID, "executing_instruction"),
			new MemoryModuleType<>(Optional.empty()).setRegistryName(IndustrialWarfare.MOD_ID, "jump_to"),
			new MemoryModuleType<>(Optional.of(Codec.BOOL)).setRegistryName(IndustrialWarfare.MOD_ID, "stop_execution"),
			new MemoryModuleType<>(Optional.of(Codec.LONG)).setRegistryName(IndustrialWarfare.MOD_ID, "wait_for"),
			new MemoryModuleType<>(Optional.of(Codec.BOOL)).setRegistryName(IndustrialWarfare.MOD_ID, "working")
		});
	}
	
}
