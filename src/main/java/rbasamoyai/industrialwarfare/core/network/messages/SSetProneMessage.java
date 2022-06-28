package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.utils.IWMiscUtils;

public class SSetProneMessage {

	private boolean prone;
	
	public SSetProneMessage() {}
	
	public SSetProneMessage(boolean prone) {
		this.prone = prone;
	}
	
	public static void encode(SSetProneMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.prone);
	}
	
	public static SSetProneMessage decode(FriendlyByteBuf buf) {
		return new SSetProneMessage(buf.readBoolean());
	}
	
	public static void handle(SSetProneMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
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
						player.setDeltaMovement(new Vec3(0.0d, -1.0d, 0.0d));
					} else if (player.level.getBlockState(pos.above()).isAir()) {
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
