package rbasamoyai.industrialwarfare.common.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
		if (!(e instanceof Player)) return;
		
		Level world = event.getWorld();
		if (world == null) return;
		if (!(world instanceof ServerLevel)) return;
		
		Player player = (Player) e;
		
		DiplomacySaveData diplomacyData = DiplomacySaveData.get(world);
		if (!diplomacyData.hasPlayerIdTag(PlayerIDTag.of(player))) {
			diplomacyData.initPlayerDiplomacyStatuses(player);
			IndustrialWarfare.LOGGER.info("Initialized diplomacy for new player {} ({}) and updated diplomacy data", player.getGameProfile().getName(), player.getUUID());
		}
	}
	
	@SubscribeEvent
	public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getPlayer();
		Entity target = event.getTarget();
		InteractionHand hand = event.getHand();
		
		ItemStack stack = player.getItemInHand(hand);
		
		if (event.isCancelable() && stack.getItem() instanceof FirearmItem) {
			if (!player.level.isClientSide) {
				if (target instanceof ItemFrame) {
					ItemFrame frame = (ItemFrame) target;
					frame.interact(player, hand);
				}
				
				player.startUsingItem(hand);
				((FirearmItem) stack.getItem()).startAiming(stack, player);
			}
			
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.CONSUME);
		}
	}
	
	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		Player player = event.getPlayer();
		if (player.swingingArm == null) return;
		
		ItemStack stack = player.getItemInHand(player.swingingArm);
		
		if (stack.getItem() instanceof FirearmItem && event.isCancelable()) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getPlayer();
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
		Player player = event.getPlayer();
		InteractionHand hand = player.getUsedItemHand();
		if (hand == null) return;
		
		if (item instanceof FirearmItem) {
			if (!FirearmItem.isMeleeing(stack) && event.isCancelable()) {
				
			}
		}
		
		if (hand == InteractionHand.MAIN_HAND && item == Items.FLINT_AND_STEEL) {
			ItemStack offhand = player.getOffhandItem();
			if (offhand.getItem() instanceof MatchCordItem && !MatchCordItem.isLit(offhand)) {
				MatchCordItem.lightMatch(offhand, true);
				player.level.playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, player.getRandom().nextFloat() * 0.4F + 0.8F);
				stack.hurtAndBreak(1, player, e -> {
					e.broadcastBreakEvent(hand);
				});
			}
		}
	}
	
	@SubscribeEvent
	public static void onOpenContainer(PlayerContainerEvent.Open event) {
		Player player = event.getPlayer();
		ItemStack mainhand = player.getMainHandItem();
		
		if (mainhand.getItem() instanceof PrimingFirearmItem) {
			FirearmItem.tryPreviousStance(mainhand, player);
		}
	}
	
}
