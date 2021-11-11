package rbasamoyai.industrialwarfare.common.items.debugitems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class JobSitePointerItem extends Item {

	private static final String TAG_UUID = "UUID";
	
	private static final String TRANSLATION_ROOT_KEY = "gui." + IndustrialWarfare.MOD_ID + ".text";
	private static final String SET_JOB_SITE_KEY = TRANSLATION_ROOT_KEY + ".set_job_site";
	private static final ITextComponent NOT_OWNED_BY_YOU = new TranslationTextComponent(TRANSLATION_ROOT_KEY + ".not_owned_by_you").withStyle(TextFormatting.RED);
	
	public JobSitePointerItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_DEBUG));
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		PlayerEntity player = context.getPlayer();
		World world = context.getLevel();
		
		if (world.isClientSide) return ActionResultType.SUCCESS;
		if (!(world instanceof ServerWorld)) return ActionResultType.SUCCESS;
		
		CompoundNBT tag = context.getItemInHand().getOrCreateTag();
		if (!tag.hasUUID(TAG_UUID)) return ActionResultType.FAIL;
		
		Entity e = ((ServerWorld) world).getEntity(tag.getUUID(TAG_UUID));
		tag.remove(TAG_UUID);
		if (!(e instanceof NPCEntity)) return ActionResultType.FAIL;
		
		NPCEntity npc = (NPCEntity) e;
		Brain<?> brain = npc.getBrain();
		if (!brain.checkMemory(MemoryModuleType.JOB_SITE, MemoryModuleStatus.REGISTERED)) return ActionResultType.FAIL;
		
		RegistryKey<World> dimension = world.dimension();
		BlockPos pos = context.getClickedPos();
		GlobalPos siteLocation = GlobalPos.of(dimension, pos);
		brain.setMemory(MemoryModuleType.JOB_SITE, siteLocation);
		
		String locationString = dimension.location().toString() + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
		player.displayClientMessage(new TranslationTextComponent(SET_JOB_SITE_KEY, npc.getDisplayName(), locationString), true);
		
		player.getCooldowns().addCooldown(this, 10);
		
		return ActionResultType.CONSUME;
	}
	
	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (player.level.isClientSide) return ActionResultType.SUCCESS;
		if (!entity.isAlive()) return ActionResultType.PASS;
		if (!(entity instanceof NPCEntity)) return ActionResultType.PASS;
		
		PlayerIDTag owner = ((NPCEntity) entity).getDataHandler()
				.map(INPCDataHandler::getOwner)
				.orElse(PlayerIDTag.NO_OWNER);
		if (!PlayerIDTag.of(player).equals(owner)) {
			player.displayClientMessage(NOT_OWNED_BY_YOU, true);
			return ActionResultType.FAIL;
		}
		
		CompoundNBT tag = stack.getOrCreateTag();
		tag.putUUID(TAG_UUID, entity.getUUID());
		
		player.getCooldowns().addCooldown(this, 10);
		
		return ActionResultType.CONSUME;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
}
