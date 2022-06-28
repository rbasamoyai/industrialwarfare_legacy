package rbasamoyai.industrialwarfare.common.entityai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

public class NPCComplaint extends ForgeRegistryEntry<NPCComplaint> {

	public static final Codec<NPCComplaint> CODEC = Codec.STRING.comapFlatMap(NPCComplaint::read, NPCComplaint::toString).stable();
	
	private static final String COMPLAINT_ROOT = "complaint.";
	
	public Component getMessage() {
		return new TranslatableComponent(COMPLAINT_ROOT + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath());
	}
	
	private static DataResult<NPCComplaint> read(String id) {
		return DataResult.success(IWModRegistries.NPC_COMPLAINTS.get().getValue(new ResourceLocation(id)));
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
}
