package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class JoblessWorkstationComponent implements IWorkstationProfessionComponent {

	@Override
	public Optional<BlockPos> getWorkingArea(World world, BlockPos pos, NPCEntity npc) {
		return Optional.empty();
	}

}
