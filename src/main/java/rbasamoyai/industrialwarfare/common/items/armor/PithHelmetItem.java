package rbasamoyai.industrialwarfare.common.items.armor;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.ModModelLayers;
import rbasamoyai.industrialwarfare.client.items.models.PithHelmetModel;

public class PithHelmetItem extends ArmorItem {

	public PithHelmetItem(ArmorMaterial material, EquipmentSlot type, Item.Properties properties) {
		super(material, type, properties);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return IndustrialWarfare.MOD_ID + ":textures/models/armor/pith_helmet_armor.png";
	}
	
	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {
			@Override
			public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				Minecraft mc = Minecraft.getInstance();
				return new PithHelmetModel(mc.getEntityModels().bakeLayer(ModModelLayers.PITH_HELMET));
			}
		});
	}
	
}
