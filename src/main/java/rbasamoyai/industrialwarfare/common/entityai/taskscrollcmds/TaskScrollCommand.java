package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.BlockPosArgHolder;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.EmptyArgHolder;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.FilterItemArgHolder;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.ItemCountArgHolder;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.StorageSideAccessArgHolder;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;
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
	
	protected static final List<Supplier<IArgHolder>> POS_ONLY = ImmutableList.of(BlockPosArgHolder::new);
	protected static final List<Supplier<IArgHolder>> ITEM_TRANSFER_ARGS = ImmutableList.of(BlockPosArgHolder::new, FilterItemArgHolder::new, StorageSideAccessArgHolder::new, ItemCountArgHolder::new);
	
	private final List<Supplier<IArgHolder>> providers;
	
	public TaskScrollCommand(List<Supplier<IArgHolder>> selectorsAndArgMethods) {
		this.providers = selectorsAndArgMethods;
	}
	
	public abstract boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order);
	
	public abstract void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public final Supplier<IArgHolder> getArgHolderSupplier(int i) {
		return 0 <= i && i < this.getArgCount() ? this.providers.get(i) : EmptyArgHolder::new;
	}
	
	public final int getArgCount() {
		return this.providers.size();
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
}
