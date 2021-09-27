package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;

/*
 * A class for sending packets relating to actions done by players in workstations.
 * Mostly used for when you click the craft button.
 */

public class SWorkstationPlayerActionMessage {

	public BlockPos tileEntityPos;
	public int action;

	public SWorkstationPlayerActionMessage() {
	}

	public SWorkstationPlayerActionMessage(BlockPos pos, int action) {
		this.tileEntityPos = pos;
		this.action = action;
	}

	public static void encode(SWorkstationPlayerActionMessage msg, PacketBuffer buf) {
		buf
				.writeBlockPos(msg.tileEntityPos)
				.writeInt(msg.action);
	}

	public static SWorkstationPlayerActionMessage decode(PacketBuffer buf) {
		return new SWorkstationPlayerActionMessage(buf.readBlockPos(), buf.readInt());
	}

	public static void handle(SWorkstationPlayerActionMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			ServerWorld world = player.getLevel();
			if (world.hasChunk(msg.tileEntityPos.getX() / 16, msg.tileEntityPos.getZ() / 16) && !world.isClientSide) {
				TileEntity te = world.getBlockEntity(msg.tileEntityPos);
				if (te instanceof WorkstationTileEntity) {
					WorkstationTileEntity workstationTE = (WorkstationTileEntity) te;
					switch (msg.action) {
					case 1:
						workstationTE.attemptCraft(player);
						break;
					case 2:
						workstationTE.onPlayerCloseScreen(player);
						break;
					default:
						IndustrialWarfare.LOGGER.printf(Level.WARN, "Wrong action int specifier passed to WorkstationPlayerActionMessage, refer to WorkstationPlayerActionMessage#handle for valid action numbers");
					}
				}
			} else {
				IndustrialWarfare.LOGGER.printf(Level.WARN, "WorkstationPlayerActionMessage received with tileEntityPos field pointing to a non-existent chunk");
			}
		});
		context.setPacketHandled(true);
	}
}
