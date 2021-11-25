package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTree;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.TaskScrollCommandInit;

/**
 * Task scroll command.
 * 
 * @author rbasamoyai
 */

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
public abstract class TaskScrollCommand extends ForgeRegistryEntry<TaskScrollCommand> {
	
	protected static final double MAX_DISTANCE_FROM_POI = 100.0d;
	protected static final float SPEED_MODIFIER = 3.0f;
	protected static final int CLOSE_ENOUGH_DIST = 0;
	protected static final Vector3i TOO_FAR = new Vector3i(MAX_DISTANCE_FROM_POI + 1.0d, MAX_DISTANCE_FROM_POI + 1.0d, MAX_DISTANCE_FROM_POI + 1.0d);
	
	private final CommandTree tree;
	private final Supplier<Map<MemoryModuleType<?>, MemoryModuleStatus>> requiredMemoriesSupplier;
	private Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemories;
	
	public TaskScrollCommand(CommandTree tree, Supplier<Map<MemoryModuleType<?>, MemoryModuleStatus>> requiredMemories) {
		this.tree = tree;
		this.requiredMemoriesSupplier = requiredMemories;
	}
	
	public abstract boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order);
	
	public abstract void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public final CommandTree getCommandTree() {
		return this.tree;
	}
	
	public final boolean hasRequiredMemories(Brain<NPCEntity> brain) {
		for (Entry<MemoryModuleType<?>, MemoryModuleStatus> e : this.requiredMemories.entrySet()) {
			if (!brain.checkMemory(e.getKey(), e.getValue())) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	private void initPostSetup() {
		this.requiredMemories = this.requiredMemoriesSupplier.get();
	}
	
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		TaskScrollCommandInit.TASK_SCROLL_COMMANDS.getEntries()
				.stream()
				.map(RegistryObject::get)
				.forEach(TaskScrollCommand::initPostSetup);
	}
	
}
