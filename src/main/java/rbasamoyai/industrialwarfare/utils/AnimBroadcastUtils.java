package rbasamoyai.industrialwarfare.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
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
		final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) entity.level);
		final PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
		GeckoLibNetwork.syncAnimation(target, syncableItem, id, animId);
	}

	/**
	 * Call on server thread
	 */
	public static <S extends Item & ISyncable> void syncItemStackAnimToSelf(ItemStack stack, LivingEntity entity, S syncableItem, int animId) {
		if (!(entity instanceof ServerPlayerEntity)) return;
		final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) entity.level);
		final PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity);
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
