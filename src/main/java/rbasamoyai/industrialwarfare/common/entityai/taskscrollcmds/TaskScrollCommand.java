package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTree;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

/**
 * Task scroll command.
 * 
 * @author rbasamoyai
 */

public abstract class TaskScrollCommand extends ForgeRegistryEntry<TaskScrollCommand> {
	
	protected static final double MAX_DISTANCE_FROM_POI = 100.0d;
	protected static final float SPEED_MODIFIER = 3.0f;
	protected static final int CLOSE_ENOUGH_DIST = 0;
	protected static final Vec3i TOO_FAR = new Vec3i(MAX_DISTANCE_FROM_POI + 1.0d, MAX_DISTANCE_FROM_POI + 1.0d, MAX_DISTANCE_FROM_POI + 1.0d);
	
	private final CommandTree tree;
	private final Supplier<Map<MemoryModuleType<?>, MemoryStatus>> requiredMemoriesSupplier;
	private Map<MemoryModuleType<?>, MemoryStatus> requiredMemories = null;
	
	public TaskScrollCommand(CommandTree tree, Supplier<Map<MemoryModuleType<?>, MemoryStatus>> requiredMemories) {
		this.tree = tree;
		this.requiredMemoriesSupplier = requiredMemories;
	}
	
	public abstract boolean checkExtraStartConditions(ServerLevel world, NPCEntity npc, TaskScrollOrder order);
	
	public abstract void start(ServerLevel world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void tick(ServerLevel world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void stop(ServerLevel world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public final CommandTree getCommandTree() {
		return this.tree;
	}
	
	public final boolean hasRequiredMemories(Brain<NPCEntity> brain) {
		if (this.requiredMemories == null) {
			this.requiredMemories = this.requiredMemoriesSupplier.get();
		}
		for (Entry<MemoryModuleType<?>, MemoryStatus> e : this.requiredMemories.entrySet()) {
			if (!brain.checkMemory(e.getKey(), e.getValue())) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
}
