package rbasamoyai.industrialwarfare.common.npcprofessions;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class QuarrierProfession extends ResourceGatheringProfession {

	public QuarrierProfession() {
		super(ImmutableList.of(
				SupplyRequestPredicate.forTool(ToolType.PICKAXE, IntBound.atLeast(1)),
				SupplyRequestPredicate.forItem(ItemInit.WORKER_SUPPORT.get(), IntBound.atLeast(1))),
				BlockInit.QUARRY.get());
	}
	
	@Override
	protected boolean canBreakBlockWith(BlockState state, ItemStack stack, NPCEntity npc) {
		return stack.getToolTypes().stream().anyMatch(state::isToolEffective);
	}
	
}
