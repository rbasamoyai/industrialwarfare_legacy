package rbasamoyai.industrialwarfare.common.npcprofessions;

import com.google.common.collect.ImmutableList;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class QuarrierProfession extends BlockGatheringProfession {

	public QuarrierProfession() {
		super(ImmutableList.of(
				SupplyRequestPredicate.forTool(ToolActions.PICKAXE_DIG, Tiers.WOOD),
				SupplyRequestPredicate.forItem(ItemInit.WORKER_SUPPORT.get(), IntBound.atLeast(1))),
				BlockInit.QUARRY.get());
	}
	
	@Override
	protected boolean canBreakBlockWith(BlockState state, ItemStack stack, NPCEntity npc) {
		if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
			return stack.isCorrectToolForDrops(state);
		}
		return true;
	}
	
}
