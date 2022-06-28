package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import org.apache.logging.log4j.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blockentities.ManufacturingBlockEntity;

/*
 * A class for sending packets relating to actions done by players in workstations.
 * Mostly used for when you click the craft button.
 */

public class SWorkstationPlayerActionMessage {

	private BlockPos blockEntityPos;
	private int action;

	public SWorkstationPlayerActionMessage() {
	}

	public SWorkstationPlayerActionMessage(BlockPos pos, int action) {
		this.blockEntityPos = pos;
		this.action = action;
	}

	public static void encode(SWorkstationPlayerActionMessage msg, FriendlyByteBuf buf) {
		buf
				.writeBlockPos(msg.blockEntityPos)
				.writeInt(msg.action);
	}

	public static SWorkstationPlayerActionMessage decode(FriendlyByteBuf buf) {
		return new SWorkstationPlayerActionMessage(buf.readBlockPos(), buf.readInt());
	}

	public static void handle(SWorkstationPlayerActionMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
			ServerLevel level = player.getLevel();
			if (!level.hasChunk(msg.blockEntityPos.getX() >> 4, msg.blockEntityPos.getZ() >> 4)) {
				IndustrialWarfare.LOGGER.printf(Level.WARN, "SWorkstationPlayerActionMessage received that points to a non-existent chunk");
			}
			BlockEntity te = level.getBlockEntity(msg.blockEntityPos);
			if (!(te instanceof ManufacturingBlockEntity)) return;
			ManufacturingBlockEntity manuBlock = (ManufacturingBlockEntity) te;
			switch (msg.action) {
			case 1:
				manuBlock.attemptCraft(player);
				break;
			case 2:
				manuBlock.onPlayerCloseScreen(player);
				break;
			default:
				IndustrialWarfare.LOGGER.printf(Level.WARN, "Invalid action int specifier passed to SWorkstationPlayerActionMessage, refer to SWorkstationPlayerActionMessage#handle for valid action numbers");
			}
		});
		ctx.setPacketHandled(true);
	}
}
