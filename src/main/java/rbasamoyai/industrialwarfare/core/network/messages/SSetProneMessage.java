package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.utils.IWMiscUtils;

public class SSetProneMessage {

	public boolean prone;
	
	public SSetProneMessage() {}
	
	public SSetProneMessage(boolean prone) {
		this.prone = prone;
	}
	
	public static void encode(SSetProneMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.prone);
	}
	
	public static SSetProneMessage decode(PacketBuffer buf) {
		return new SSetProneMessage(buf.readBoolean());
	}
	
	public static void handle(SSetProneMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ServerPlayerEntity player = ctx.getSender();
			if (player.level.isClientSide) return;

			Pose currentPose = player.getPose();
			
			boolean isSneaking = player.isShiftKeyDown();
			boolean canGoProne = msg.prone && player.isOnGround();
			
			if (currentPose == Pose.STANDING && canGoProne) {
				player.setForcedPose(Pose.SWIMMING);
			} else if (currentPose == Pose.CROUCHING && canGoProne) {
				player.setForcedPose(Pose.SWIMMING);
				player.setShiftKeyDown(false);
			} else if (player.isVisuallySwimming() && !player.isInWater()) {				
				if (isSneaking) {
					player.setPose(Pose.CROUCHING);
					player.setForcedPose(null);
				} else if (!msg.prone || !player.isOnGround()) {
					BlockPos pos = player.blockPosition();
					if (IWMiscUtils.isTopSlabAt(player.level, pos) || IWMiscUtils.isTopSlabAt(player.level, pos.above())) {
						player.setPose(Pose.CROUCHING);
						player.setForcedPose(null);
						player.setDeltaMovement(new Vector3d(0.0d, -1.0d, 0.0d));
					} else if (IWMiscUtils.isAirAt(player.level, pos.above())) {
						player.setPose(Pose.STANDING);
						player.setForcedPose(null);
					}
				} else {
					player.setForcedPose(Pose.SWIMMING);
					player.setShiftKeyDown(false);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
	
}
