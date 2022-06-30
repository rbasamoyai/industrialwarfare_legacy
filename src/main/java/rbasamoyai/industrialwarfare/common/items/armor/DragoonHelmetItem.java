package rbasamoyai.industrialwarfare.common.items.armor;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.ModModelLayers;
import rbasamoyai.industrialwarfare.client.items.models.DragoonHelmetModel;

public class DragoonHelmetItem extends DyeableArmorItem {

	public DragoonHelmetItem(ArmorMaterial material, EquipmentSlot type, Item.Properties properties) {
		super(material, type, properties);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		String suf = "";
		if (type != null) {
			int modelData = stack.getOrCreateTag().getInt("CustomModelData");
			suf = String.format("_%s%s", type, modelData == 0 ? "" : Integer.toString(modelData));
		}
		return String.format("%s:/textures/models/armor/dragoon_helmet%s.png", IndustrialWarfare.MOD_ID, type == null ? "" : suf);
	}
	
	@Override
	public int getColor(ItemStack stack) {
		CompoundTag display = stack.getTagElement("display");
		return display != null && display.contains("color", 99) ? display.getInt("color") : 1710619;
	}
	
	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {
			@Override
			public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				Minecraft mc = Minecraft.getInstance();
				return new DragoonHelmetModel(mc.getEntityModels().bakeLayer(ModModelLayers.DRAGOON_HELMET));
			}
		});
	}
	
}
