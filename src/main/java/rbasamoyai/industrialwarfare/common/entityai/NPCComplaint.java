package rbasamoyai.industrialwarfare.common.entityai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

public class NPCComplaint extends ForgeRegistryEntry<NPCComplaint> {

	public static final Codec<NPCComplaint> CODEC = Codec.STRING.comapFlatMap(NPCComplaint::read, NPCComplaint::toString).stable();
	
	private static final String COMPLAINT_ROOT = "complaint.";
	
	public NPCComplaint(String complaintId) {
		this.setRegistryName(IndustrialWarfare.MOD_ID, complaintId);
	}
	
	public ITextComponent getMessage() {
		return new TranslationTextComponent(COMPLAINT_ROOT + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath());
	}
	
	private static DataResult<NPCComplaint> read(String id) {
		return DataResult.success(IWModRegistries.NPC_COMPLAINTS.getValue(new ResourceLocation(id)));
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
}
