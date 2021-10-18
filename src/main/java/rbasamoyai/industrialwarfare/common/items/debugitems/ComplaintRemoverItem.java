package rbasamoyai.industrialwarfare.common.items.debugitems;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
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
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (player.level.isClientSide) return ActionResultType.SUCCESS;
		if (!entity.isAlive()) return ActionResultType.PASS;
		if (!(entity instanceof NPCEntity)) return ActionResultType.PASS;
		
		NPCEntity npc = (NPCEntity) entity;
		Brain<?> brain = npc.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT.get())) return ActionResultType.CONSUME;
		NPCComplaint oldComplaint = brain.getMemory(MemoryModuleTypeInit.COMPLAINT.get()).orElse(NPCComplaintInit.CLEAR.get());
		brain.eraseMemory(MemoryModuleTypeInit.COMPLAINT.get());
		player.displayClientMessage(new TranslationTextComponent(REMOVED_COMPLAINTS_KEY, npc.getDisplayName(), oldComplaint.getRegistryName()), true);
		return ActionResultType.CONSUME;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
}
