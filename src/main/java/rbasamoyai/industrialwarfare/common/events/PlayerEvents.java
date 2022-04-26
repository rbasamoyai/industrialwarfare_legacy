package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.PrimingFirearmItem;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE)
public class PlayerEvents {

	@SubscribeEvent
	public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		Entity e = event.getEntity();
		if (e == null) return;
		if (!(e instanceof PlayerEntity)) return;
		
		World world = event.getWorld();
		if (world == null) return;
		if (!(world instanceof ServerWorld)) return;
		
		PlayerEntity player = (PlayerEntity) e;
		
		DiplomacySaveData diplomacyData = DiplomacySaveData.get(world);
		if (!diplomacyData.hasPlayerIdTag(PlayerIDTag.of(player))) {
			diplomacyData.initPlayerDiplomacyStatuses(player);
			IndustrialWarfare.LOGGER.info("Initialized diplomacy for new player {} ({}) and updated diplomacy data", player.getGameProfile().getName(), player.getUUID());
		}
	}
	
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		PlayerEntity player = event.getPlayer();
		Entity target = event.getTarget();
		Hand hand = event.getHand();
		
		ItemStack stack = player.getItemInHand(hand);
		
		if (event.isCancelable() && stack.getItem() instanceof FirearmItem) {
			if (!player.level.isClientSide) {
				if (target instanceof ItemFrameEntity) {
					ItemFrameEntity frame = (ItemFrameEntity) target;
					frame.interact(player, hand);
				}
				
				player.startUsingItem(hand);
				((FirearmItem) stack.getItem()).startAiming(stack, player);
			}
			
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.CONSUME);
		}
	}
	
	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player.swingingArm == null) return;
		
		ItemStack stack = player.getItemInHand(player.swingingArm);
		
		if (stack.getItem() instanceof FirearmItem && event.isCancelable()) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		PlayerEntity player = event.getPlayer();
		if (player.swingingArm == null) return;
		
		ItemStack stack = player.getItemInHand(player.swingingArm);
		
		if (stack.getItem() instanceof FirearmItem && event.isCancelable()) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		PlayerEntity player = event.getPlayer();
		Hand hand = player.getUsedItemHand();
		if (hand == null) return;
		
		if (item instanceof FirearmItem) {
			if (!FirearmItem.isMeleeing(stack) && event.isCancelable()) {
				
			}
		}
		
		if (hand == Hand.MAIN_HAND && item == Items.FLINT_AND_STEEL) {
			ItemStack offhand = player.getOffhandItem();
			if (offhand.getItem() instanceof MatchCordItem && !MatchCordItem.isLit(offhand)) {
				MatchCordItem.lightMatch(offhand, true);
				player.level.playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F, player.getRandom().nextFloat() * 0.4F + 0.8F);
				stack.hurtAndBreak(1, player, e -> {
					e.broadcastBreakEvent(hand);
				});
			}
		}
	}
	
	@SubscribeEvent
	public static void onOpenContainer(PlayerContainerEvent.Open event) {
		PlayerEntity player = event.getPlayer();
		ItemStack mainhand = player.getMainHandItem();
		
		if (mainhand.getItem() instanceof PrimingFirearmItem) {
			FirearmItem.tryPreviousStance(mainhand, player);
		}
	}
	
}
