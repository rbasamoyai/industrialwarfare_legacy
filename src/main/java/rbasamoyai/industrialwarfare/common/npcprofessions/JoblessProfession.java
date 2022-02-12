package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class JoblessProfession extends NPCProfession {

	@Override
	public boolean checkMemories(NPCEntity npc) {
		return false;
	}
	
	@Override
	public Optional<BlockPos> getWorkingArea(World world, BlockPos pos, NPCEntity npc) {
		return Optional.empty();
	}
	
	@Override
	public void work(World world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
	}

}
