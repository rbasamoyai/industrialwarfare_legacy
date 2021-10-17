package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public interface IWorkstationProfessionComponent {

	public Optional<BlockPos> getWorkingArea(World world, BlockPos pos, NPCEntity npc);
	
}
