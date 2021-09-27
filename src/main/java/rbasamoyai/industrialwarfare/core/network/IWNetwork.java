package rbasamoyai.industrialwarfare.core.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.network.messages.SEditLabelSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.STaskScrollSyncMessage;
import rbasamoyai.industrialwarfare.core.network.messages.SWorkstationPlayerActionMessage;

public class IWNetwork {

	public static final String NETWORK_VERSION = "0.0.6";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(IndustrialWarfare.MOD_ID, "network"), () -> NETWORK_VERSION, NETWORK_VERSION::equals,
			NETWORK_VERSION::equals);

	public void init() {
		// Tile entity messages
		int id = 0;
		CHANNEL.registerMessage(id++, SWorkstationPlayerActionMessage.class, SWorkstationPlayerActionMessage::encode, SWorkstationPlayerActionMessage::decode, SWorkstationPlayerActionMessage::handle);
		CHANNEL.registerMessage(id++, STaskScrollSyncMessage.class, STaskScrollSyncMessage::encode, STaskScrollSyncMessage::decode, STaskScrollSyncMessage::handle);
		CHANNEL.registerMessage(id++, SEditLabelSyncMessage.class, SEditLabelSyncMessage::encode, SEditLabelSyncMessage::decode, SEditLabelSyncMessage::handle);
	}

}
