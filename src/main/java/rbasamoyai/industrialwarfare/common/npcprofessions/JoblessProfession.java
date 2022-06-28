package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class JoblessProfession extends NPCProfession {

	@Override
	public boolean checkMemories(NPCEntity npc) {
		return false;
	}
	
	@Override
	public Optional<BlockPos> getWorkingArea(Level world, BlockPos pos, NPCEntity npc) {
		return Optional.empty();
	}
	
	@Override
	public void work(Level world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
	}
	
	@Override
	public void stopWorking(Level world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
	}

}
