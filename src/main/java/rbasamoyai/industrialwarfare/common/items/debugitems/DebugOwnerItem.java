package rbasamoyai.industrialwarfare.common.items.debugitems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class DebugOwnerItem extends Item {

	public DebugOwnerItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_DEBUG));
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		if (player.level.isClientSide) return InteractionResult.SUCCESS;
		if (!entity.isAlive()) return InteractionResult.PASS;
		if (!(entity instanceof NPCEntity)) return InteractionResult.PASS;
		
		((NPCEntity) entity).getDataHandler().ifPresent(h -> h.setOwner(PlayerIDTag.of(player)));
		return InteractionResult.CONSUME;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
}
