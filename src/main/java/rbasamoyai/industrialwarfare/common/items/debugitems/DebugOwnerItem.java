package rbasamoyai.industrialwarfare.common.items.debugitems;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class DebugOwnerItem extends Item {

	public DebugOwnerItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_DEBUG));
	}
	
	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (player.level.isClientSide) return ActionResultType.SUCCESS;
		if (!entity.isAlive()) return ActionResultType.PASS;
		if (!(entity instanceof NPCEntity)) return ActionResultType.PASS;
		
		((NPCEntity) entity).getDataHandler().ifPresent(h -> h.setOwner(PlayerIDTag.of(player)));
		return ActionResultType.CONSUME;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
}
