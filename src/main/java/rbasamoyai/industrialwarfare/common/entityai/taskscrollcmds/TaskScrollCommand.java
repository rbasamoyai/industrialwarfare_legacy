package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.ItemCountArgSelector;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.StorageSideAccessArgSelector;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
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
	
	protected static final List<Function<Integer, ArgSelector<Byte>>> NO_ARGS = ImmutableList.of();
	protected static final List<Function<Integer, ArgSelector<Byte>>> ITEM_TRANSFER_ARGS = ImmutableList.of(StorageSideAccessArgSelector::new, ItemCountArgSelector::new);
	
	private final boolean usesBlockPos;
	private final boolean canUseFilter;
	private final List<Function<Integer, ArgSelector<Byte>>> selectors;
	
	public TaskScrollCommand(boolean usesBlockPos, boolean canUseFilter, List<Function<Integer, ArgSelector<Byte>>> selectors) {
		this.usesBlockPos = usesBlockPos;
		this.canUseFilter = canUseFilter;
		this.selectors = selectors;
	}
	
	public abstract boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order);
	
	public abstract void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public final boolean usesBlockPos() {
		return this.usesBlockPos;
	}
	
	public final boolean canUseFilter() {
		return this.canUseFilter;
	}
	
	public final Function<Integer, ArgSelector<Byte>> getSelectorAt(int index) {
		return index >= 0 && index < this.selectors.size() ? this.selectors.get(index) : null;
	}
	
	public final int getArgCount() {
		return this.selectors.size();
	}
	
}
