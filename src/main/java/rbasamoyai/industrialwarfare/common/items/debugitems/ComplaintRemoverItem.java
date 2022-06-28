package rbasamoyai.industrialwarfare.common.items.debugitems;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class ComplaintRemoverItem extends Item {

	private static final String REMOVED_COMPLAINTS_KEY = "gui." + IndustrialWarfare.MOD_ID + ".text.removed_complaints";
	
	public ComplaintRemoverItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_DEBUG));
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		if (player.level.isClientSide) return InteractionResult.SUCCESS;
		if (!entity.isAlive()) return InteractionResult.PASS;
		if (!(entity instanceof NPCEntity)) return InteractionResult.PASS;
		
		NPCEntity npc = (NPCEntity) entity;
		Brain<?> brain = npc.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT.get())) return InteractionResult.CONSUME;
		NPCComplaint oldComplaint = brain.getMemory(MemoryModuleTypeInit.COMPLAINT.get()).orElse(NPCComplaintInit.CLEAR.get());
		brain.eraseMemory(MemoryModuleTypeInit.COMPLAINT.get());
		player.displayClientMessage(new TranslatableComponent(REMOVED_COMPLAINTS_KEY, npc.getDisplayName(), oldComplaint.getRegistryName()), true);
		return InteractionResult.CONSUME;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
}
