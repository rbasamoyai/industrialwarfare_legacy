package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public interface IWorkUnit {

	public boolean checkMemories(NPCEntity npc);
	
	public Optional<BlockPos> getWorkingArea(World world, BlockPos pos, NPCEntity npc);
	
	public void work(World world, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
}
