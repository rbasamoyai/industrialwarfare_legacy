package rbasamoyai.industrialwarfare.core.init;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.common.entityai.navigation.PrecisePosCodec;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class MemoryModuleTypeInit {

	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<MemoryModuleType<ActivityStatus>> ACTIVITY_STATUS = MEMORY_MODULE_TYPES.register("activity_status", () -> new MemoryModuleType<>(Optional.of(ActivityStatus.CODEC))); 
	public static final RegistryObject<MemoryModuleType<GlobalPos>> CACHED_POS = MEMORY_MODULE_TYPES.register("cached_pos", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Boolean>> CAN_ATTACK = MEMORY_MODULE_TYPES.register("can_attack", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<CombatMode>> COMBAT_MODE = MEMORY_MODULE_TYPES.register("combat_mode", () -> new MemoryModuleType<>(Optional.of(CombatMode.CODEC)));
	public static final RegistryObject<MemoryModuleType<NPCComplaint>> COMPLAINT = MEMORY_MODULE_TYPES.register("complaint", () -> new MemoryModuleType<>(Optional.of(NPCComplaint.CODEC)));
	public static final RegistryObject<MemoryModuleType<TaskScrollOrder>> CURRENT_ORDER = MEMORY_MODULE_TYPES.register("current_order", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Integer>> CURRENT_ORDER_INDEX = MEMORY_MODULE_TYPES.register("current_order_index", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
	public static final RegistryObject<MemoryModuleType<Boolean>> DEFENDING_SELF = MEMORY_MODULE_TYPES.register("defending_self", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> ENGAGING_COMPLETED = MEMORY_MODULE_TYPES.register("engaging_target", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> EXECUTING_INSTRUCTION = MEMORY_MODULE_TYPES.register("executing_instruction", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> FINISHED_ATTACKING = MEMORY_MODULE_TYPES.register("finished_attacking", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<UUID>> IN_COMMAND_GROUP = MEMORY_MODULE_TYPES.register("in_command_group", () -> new MemoryModuleType<>(Optional.of(UUIDCodec.CODEC)));
	public static final RegistryObject<MemoryModuleType<FormationLeaderEntity>> IN_FORMATION = MEMORY_MODULE_TYPES.register("in_formation", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Integer>> JUMP_TO = MEMORY_MODULE_TYPES.register("jump_to", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Integer>> ON_PATROL = MEMORY_MODULE_TYPES.register("on_patrol", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
	public static final RegistryObject<MemoryModuleType<Vector3d>> PRECISE_POS = MEMORY_MODULE_TYPES.register("precise_pos", () -> new MemoryModuleType<>(Optional.of(PrecisePosCodec.CODEC)));
	public static final RegistryObject<MemoryModuleType<Boolean>> SHOULD_PREPARE_ATTACK = MEMORY_MODULE_TYPES.register("should_prepare_attack", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> STOP_EXECUTION = MEMORY_MODULE_TYPES.register("stop_execution", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<IPosition>> SHOOTING_POS = MEMORY_MODULE_TYPES.register("shooting_pos", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Long>> WAIT_FOR = MEMORY_MODULE_TYPES.register("wait_for", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));
	
}
