package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.models.PithHelmetModel;

public class PithHelmetItem extends ArmorItem {

	public PithHelmetItem(IArmorMaterial material, EquipmentSlotType type, Item.Properties properties) {
		super(material, type, properties);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, A _default) {
		return (A) new PithHelmetModel(0.75f);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		return IndustrialWarfare.MOD_ID + ":textures/models/armor/pith_helmet_armor.png";
	}
	
}
