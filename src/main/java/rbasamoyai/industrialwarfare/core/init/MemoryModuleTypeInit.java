package rbasamoyai.industrialwarfare.core.init;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;

public class MemoryModuleTypeInit {

	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, IndustrialWarfare.MOD_ID);
	public static final RegistryObject<MemoryModuleType<GlobalPos>> CACHED_POS = MEMORY_MODULE_TYPES.register("cached_pos", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<NPCComplaint>> COMPLAINT = MEMORY_MODULE_TYPES.register("complaint", () -> new MemoryModuleType<>(Optional.of(NPCComplaint.CODEC)));
	public static final RegistryObject<MemoryModuleType<Integer>> CURRENT_INSTRUCTION_INDEX = MEMORY_MODULE_TYPES.register("current_instruction_index", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
	public static final RegistryObject<MemoryModuleType<Boolean>> EXECUTING_INSTRUCTION = MEMORY_MODULE_TYPES.register("executing_instruction", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Integer>> JUMP_TO = MEMORY_MODULE_TYPES.register("jump_to", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Boolean>> STOP_EXECUTION = MEMORY_MODULE_TYPES.register("stop_execution", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Long>> WAIT_FOR = MEMORY_MODULE_TYPES.register("wait_for", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));
	public static final RegistryObject<MemoryModuleType<Boolean>> WORKING = MEMORY_MODULE_TYPES.register("working", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	
}
