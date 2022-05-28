package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public abstract class NPCProfession extends ForgeRegistryEntry<NPCProfession> {
	
	public abstract boolean checkMemories(NPCEntity npc);
	
	public abstract Optional<BlockPos> getWorkingArea(World level, BlockPos pos, NPCEntity npc);
	
	public abstract void work(World level, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
	public abstract void stopWorking(World level, NPCEntity npc, long gameTime, TaskScrollOrder order);
	
}
