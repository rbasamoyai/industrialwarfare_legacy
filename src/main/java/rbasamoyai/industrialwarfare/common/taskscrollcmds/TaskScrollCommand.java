package rbasamoyai.industrialwarfare.common.taskscrollcmds;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

/**
 * Task scroll command.
 * 
 * @author rbasamoyai
 */

public class TaskScrollCommand extends ForgeRegistryEntry<TaskScrollCommand> {

	private final BiFunction<NPCEntity, TaskScrollOrder, Boolean> function;
	private final boolean usesBlockPos;
	private final boolean canUseFilter;
	private final List<Function<Integer, ArgSelector<Byte>>> selectors;
	
	public TaskScrollCommand(BiFunction<NPCEntity, TaskScrollOrder, Boolean> function, boolean usesBlockPos, boolean canUseFilter, List<Function<Integer, ArgSelector<Byte>>> selectors) {
		this.function = function;
		this.usesBlockPos = usesBlockPos;
		this.canUseFilter = canUseFilter;
		this.selectors = selectors;
	}
	
	public boolean apply(NPCEntity npc, TaskScrollOrder order) {
		return this.function.apply(npc, order);
	}
	
	public boolean usesBlockPos() {
		return this.usesBlockPos;
	}
	
	public boolean canUseFilter() {
		return this.canUseFilter;
	}
	
	public Function<Integer, ArgSelector<Byte>> getSelectorAt(int index) {
		return index >= 0 && index < this.selectors.size() ? this.selectors.get(index) : null;
	}
	
	public int getArgCount() {
		return this.selectors.size();
	}
	
}
