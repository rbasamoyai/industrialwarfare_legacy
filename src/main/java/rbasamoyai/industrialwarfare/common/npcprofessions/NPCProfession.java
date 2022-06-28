package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public abstract class NPCProfession extends ForgeRegistryEntry<NPCProfession> {
	
	public abstract boolean checkMemories(NPCEntity npc);
	
	public abstract Optional<BlockPos> getWorkingArea(Level level, BlockPos pos, NPCEntity npc);
	
	public abstract void work(Level level, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void stopWorking(Level level, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
}
