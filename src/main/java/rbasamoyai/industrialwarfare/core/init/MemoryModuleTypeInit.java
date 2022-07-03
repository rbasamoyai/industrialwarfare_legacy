package rbasamoyai.industrialwarfare.core.init;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.Position;
import net.minecraft.core.SerializableUUID;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.MobInteraction;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.navigation.PrecisePosCodec;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class MemoryModuleTypeInit {

	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<MemoryModuleType<ActivityStatus>> ACTIVITY_STATUS = MEMORY_MODULE_TYPES.register("activity_status", () -> new MemoryModuleType<>(Optional.of(ActivityStatus.CODEC)));
	public static final RegistryObject<MemoryModuleType<BlockInteraction>> BLOCK_INTERACTION = MEMORY_MODULE_TYPES.register("block_interaction", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Integer>> BLOCK_INTERACTION_COOLDOWN = MEMORY_MODULE_TYPES.register("block_interaction_cooldown", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
	public static final RegistryObject<MemoryModuleType<GlobalPos>> CACHED_POS = MEMORY_MODULE_TYPES.register("cached_pos", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Boolean>> CAN_ATTACK = MEMORY_MODULE_TYPES.register("can_attack", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<CombatMode>> COMBAT_MODE = MEMORY_MODULE_TYPES.register("combat_mode", () -> new MemoryModuleType<>(Optional.of(CombatMode.CODEC)));
	public static final RegistryObject<MemoryModuleType<NPCComplaint>> COMPLAINT = MEMORY_MODULE_TYPES.register("complaint", () -> new MemoryModuleType<>(Optional.of(NPCComplaint.CODEC)));
	public static final RegistryObject<MemoryModuleType<TaskScrollOrder>> CURRENT_ORDER = MEMORY_MODULE_TYPES.register("current_order", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Integer>> CURRENT_ORDER_INDEX = MEMORY_MODULE_TYPES.register("current_order_index", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
	public static final RegistryObject<MemoryModuleType<Boolean>> DEFENDING_SELF = MEMORY_MODULE_TYPES.register("defending_self", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> DEPOSITING_ITEMS = MEMORY_MODULE_TYPES.register("depositing_items", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> ENGAGING_COMPLETED = MEMORY_MODULE_TYPES.register("engaging_target", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> EXECUTING_INSTRUCTION = MEMORY_MODULE_TYPES.register("executing_instruction", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> FINISHED_ATTACKING = MEMORY_MODULE_TYPES.register("finished_attacking", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<UUID>> IN_COMMAND_GROUP = MEMORY_MODULE_TYPES.register("in_command_group", () -> new MemoryModuleType<>(Optional.of(SerializableUUID.CODEC)));
	public static final RegistryObject<MemoryModuleType<FormationLeaderEntity>> IN_FORMATION = MEMORY_MODULE_TYPES.register("in_formation", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Integer>> JUMP_TO = MEMORY_MODULE_TYPES.register("jump_to", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Integer>> ON_PATROL = MEMORY_MODULE_TYPES.register("on_patrol", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
	public static final RegistryObject<MemoryModuleType<MobInteraction>> MOB_INTERACTION = MEMORY_MODULE_TYPES.register("mob_interaction", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Vec3>> PRECISE_POS = MEMORY_MODULE_TYPES.register("precise_pos", () -> new MemoryModuleType<>(Optional.of(PrecisePosCodec.CODEC)));
	public static final RegistryObject<MemoryModuleType<Boolean>> REACHED_MOVEMENT_TARGET = MEMORY_MODULE_TYPES.register("reached_movement_target", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> SHOULD_PREPARE_ATTACK = MEMORY_MODULE_TYPES.register("should_prepare_attack", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<Boolean>> STOP_EXECUTION = MEMORY_MODULE_TYPES.register("stop_execution", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
	public static final RegistryObject<MemoryModuleType<List<SupplyRequestPredicate>>> SUPPLY_REQUESTS = MEMORY_MODULE_TYPES.register("supply_request", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Position>> SHOOTING_POS = MEMORY_MODULE_TYPES.register("shooting_pos", () -> new MemoryModuleType<>(Optional.empty()));
	public static final RegistryObject<MemoryModuleType<Long>> WAIT_FOR = MEMORY_MODULE_TYPES.register("wait_for", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));
	
}
