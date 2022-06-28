package rbasamoyai.industrialwarfare.common.npcprofessions;

import com.google.common.collect.ImmutableList;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class LoggerProfession extends ResourceGatheringProfession {

	public LoggerProfession() {
		super(ImmutableList.of(
				SupplyRequestPredicate.forTool(ToolActions.AXE_DIG, Tiers.WOOD),
				SupplyRequestPredicate.forTool(ToolActions.HOE_DIG, Tiers.WOOD)),
				BlockInit.TREE_FARM.get());
	}
	
	@Override
	protected boolean canBreakBlockWith(BlockState state, ItemStack stack, NPCEntity npc) {
		if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.MINEABLE_WITH_HOE)) {
			return stack.isCorrectToolForDrops(state);
		}
		return true;
	}
	
}
