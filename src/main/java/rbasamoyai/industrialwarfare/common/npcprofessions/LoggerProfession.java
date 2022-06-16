package rbasamoyai.industrialwarfare.common.npcprofessions;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class LoggerProfession extends ResourceGatheringProfession {

	public LoggerProfession() {
		super(ImmutableList.of(
				SupplyRequestPredicate.forTool(ToolType.AXE, IntBound.atLeast(1)),
				SupplyRequestPredicate.forTool(ToolType.HOE, IntBound.atLeast(1))),
				BlockInit.TREE_FARM.get());
	}
	
	@Override
	protected boolean canBreakBlockWith(BlockState state, ItemStack stack, NPCEntity npc) {
		return stack.getToolTypes().stream().anyMatch(state::isToolEffective);
	}
	
}
