package rbasamoyai.industrialwarfare.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.KeyBindingsInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SSetProneMessage;
import rbasamoyai.industrialwarfare.utils.IWMiscUtils;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientGameplayEvents {
	
	@SubscribeEvent
	public static void onClientTickEvent(ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		
		if (mc.player != null) {
			Pose currentPose = mc.player.getPose();
			
			boolean isSneaking = mc.player.isShiftKeyDown();
			boolean isProning = KeyBindingsInit.PRONE.isDown();
			boolean canGoProne = isProning && mc.player.isOnGround();
			
			if (currentPose == Pose.STANDING && canGoProne) {
				mc.player.setPose(Pose.SWIMMING);
				IWNetwork.CHANNEL.sendToServer(new SSetProneMessage(true));
			} else if (currentPose == Pose.CROUCHING && canGoProne) {
				if (isSneaking) {
					mc.options.keyShift.setDown(mc.options.toggleCrouch);
				}
				mc.player.setForcedPose(Pose.SWIMMING);
				mc.player.setShiftKeyDown(false);
				IWNetwork.CHANNEL.sendToServer(new SSetProneMessage(true));
			} else if (mc.player.isVisuallySwimming() && !mc.player.isInWater()) {				
				if (isSneaking) {
					mc.player.setPose(Pose.CROUCHING);
					mc.player.setForcedPose(null);
					KeyBindingsInit.PRONE.setDown(mc.options.toggleCrouch);
					IWNetwork.CHANNEL.sendToServer(new SSetProneMessage(false));
				} else if (!mc.player.isOnGround() || !isProning) {
					BlockPos pos = mc.player.blockPosition();
					if (isProning) {
						KeyBindingsInit.PRONE.setDown(mc.options.toggleCrouch);
					}
					if (IWMiscUtils.isTopSlabAt(mc.player.level, pos) || IWMiscUtils.isTopSlabAt(mc.player.level, pos.above())) {
						mc.player.setPose(Pose.CROUCHING);
						mc.player.setForcedPose(null);
						mc.player.setDeltaMovement(new Vector3d(0.0d, -1.0d, 0.0d));
					} else if (IWMiscUtils.isAirAt(mc.player.level, pos.above())) {
						mc.player.setPose(Pose.STANDING);
						mc.player.setForcedPose(null);
					}
					IWNetwork.CHANNEL.sendToServer(new SSetProneMessage(false));
				} else {
					mc.player.setForcedPose(Pose.SWIMMING);
					IWNetwork.CHANNEL.sendToServer(new SSetProneMessage(true));
				}
			}
		}
	}
	
}
