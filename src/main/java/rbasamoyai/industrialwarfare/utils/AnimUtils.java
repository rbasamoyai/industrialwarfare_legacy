package rbasamoyai.industrialwarfare.utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class AnimUtils {

	/**
	 * Call on server thread
	 */
	public static <S extends Item & ISyncable> void syncItemStackAnim(ItemStack stack, LivingEntity entity, S syncableItem, int animId) {
		final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) entity.level);
		final PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
		GeckoLibNetwork.syncAnimation(target, syncableItem, id, animId);
	}
	
}
