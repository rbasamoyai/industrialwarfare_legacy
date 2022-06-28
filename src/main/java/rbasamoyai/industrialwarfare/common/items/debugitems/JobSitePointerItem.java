package rbasamoyai.industrialwarfare.common.items.debugitems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCData;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class JobSitePointerItem extends Item {

	private static final String TAG_UUID = "UUID";
	
	private static final String TRANSLATION_ROOT_KEY = "gui." + IndustrialWarfare.MOD_ID + ".text";
	private static final String SET_JOB_SITE_KEY = TRANSLATION_ROOT_KEY + ".set_job_site";
	private static final Component NOT_OWNED_BY_YOU = new TranslatableComponent(TRANSLATION_ROOT_KEY + ".not_owned_by_you").withStyle(ChatFormatting.RED);
	
	public JobSitePointerItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_DEBUG));
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!(level instanceof ServerLevel)) return InteractionResult.SUCCESS;
		
		CompoundTag tag = context.getItemInHand().getOrCreateTag();
		if (!tag.hasUUID(TAG_UUID)) return InteractionResult.FAIL;
		
		Entity e = ((ServerLevel) level).getEntity(tag.getUUID(TAG_UUID));
		tag.remove(TAG_UUID);
		if (!(e instanceof NPCEntity)) return InteractionResult.FAIL;
		
		NPCEntity npc = (NPCEntity) e;
		Brain<?> brain = npc.getBrain();
		if (!brain.checkMemory(MemoryModuleType.JOB_SITE, MemoryStatus.REGISTERED)) return InteractionResult.FAIL;
		
		ResourceKey<Level> dimension = level.dimension();
		BlockPos pos = context.getClickedPos();
		GlobalPos siteLocation = GlobalPos.of(dimension, pos);
		brain.setMemory(MemoryModuleType.JOB_SITE, siteLocation);
		
		String locationString = dimension.location().toString() + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
		player.displayClientMessage(new TranslatableComponent(SET_JOB_SITE_KEY, npc.getDisplayName(), locationString), true);
		
		player.getCooldowns().addCooldown(this, 10);
		
		return InteractionResult.CONSUME;
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		if (player.level.isClientSide) return InteractionResult.SUCCESS;
		if (!entity.isAlive()) return InteractionResult.PASS;
		if (!(entity instanceof NPCEntity)) return InteractionResult.PASS;
		
		PlayerIDTag owner = ((NPCEntity) entity).getDataHandler()
				.map(INPCData::getOwner)
				.orElse(PlayerIDTag.NO_OWNER);
		if (!PlayerIDTag.of(player).equals(owner)) {
			player.displayClientMessage(NOT_OWNED_BY_YOU, true);
			return InteractionResult.FAIL;
		}
		
		CompoundTag tag = stack.getOrCreateTag();
		tag.putUUID(TAG_UUID, entity.getUUID());
		
		player.getCooldowns().addCooldown(this, 10);
		
		return InteractionResult.CONSUME;
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
	
}
