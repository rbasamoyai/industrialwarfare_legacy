package rbasamoyai.industrialwarfare.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.CQueueEntityAnimMessage;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class AnimBroadcastUtils {

	/**
	 * Call on server thread
	 */
	public static <S extends Item & ISyncable> void syncItemStackAnim(ItemStack stack, LivingEntity entity, S syncableItem, int animId) {
		final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerLevel) entity.level);
		final PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
		GeckoLibNetwork.syncAnimation(target, syncableItem, id, animId);
	}

	/**
	 * Call on server thread
	 */
	public static <S extends Item & ISyncable> void syncItemStackAnimToSelf(ItemStack stack, LivingEntity entity, S syncableItem, int animId) {
		if (!(entity instanceof ServerPlayer)) return;
		final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerLevel) entity.level);
		final PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayer) entity);
		GeckoLibNetwork.syncAnimation(target, syncableItem, id, animId);
	}

	/**
	 * Call on server thread
	 */
	public static void broadcastThirdPersonAnim(ItemStack stack, LivingEntity entity, String controller, List<Tuple<String, Boolean>> anim, float speed) {
		final CQueueEntityAnimMessage msg = new CQueueEntityAnimMessage(entity.getId(), controller, anim, speed);
		final PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
		IWNetwork.CHANNEL.send(target, msg);
	}

	/**
	 * Single animation method.
	 * Call on server thread
	 */
	public static void broadcastThirdPersonAnim(ItemStack stack, LivingEntity entity, String controller, String animName, boolean shouldLoop, float speed) {
		List<Tuple<String, Boolean>> anim = new ArrayList<>();
		anim.add(new Tuple<>(animName, shouldLoop));
		broadcastThirdPersonAnim(stack, entity, controller, anim, speed);
	}

}
