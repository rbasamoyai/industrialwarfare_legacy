package rbasamoyai.industrialwarfare.common.npcprofessions;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.core.init.BlockInit;

public class FarmerProfession extends BlockGatheringProfession {

	public FarmerProfession() {
		super(ImmutableList.of(
				SupplyRequestPredicate.forTool(ToolActions.HOE_TILL, Tiers.WOOD),
				SupplyRequestPredicate.forItem(Tags.Items.SEEDS, IntBound.ANY)), BlockInit.FARMING_PLOT.get());
	}
	
}
