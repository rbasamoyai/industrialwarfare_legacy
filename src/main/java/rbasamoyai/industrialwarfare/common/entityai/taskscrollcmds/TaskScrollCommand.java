package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;
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
	protected static final Vector3i TOO_FAR = new Vector3i(MAX_DISTANCE_FROM_POI + 1.0d, MAX_DISTANCE_FROM_POI + 1.0d, MAX_DISTANCE_FROM_POI + 1.0d);
	
	private final CommandTree tree;
	
	public TaskScrollCommand(CommandTree tree) {
		this.tree = tree;
	}
	
	public abstract boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order);
	
	public abstract void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public final CommandTree getCommandTree() {
		return this.tree;
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
}
