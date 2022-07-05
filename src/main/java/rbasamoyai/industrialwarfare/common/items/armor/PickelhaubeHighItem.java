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
import rbasamoyai.industrialwarfare.client.items.models.PickelhaubeHighModel;

public class PickelhaubeHighItem extends DyeableArmorItem {

	public PickelhaubeHighItem(ArmorMaterial material, EquipmentSlot type, Item.Properties properties) {
		super(material, type, properties);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return String.format("%s:/textures/models/armor/pickelhaube_high%s.png", IndustrialWarfare.MOD_ID, type == null ? "" : String.format("_%s", type));
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
				return new PickelhaubeHighModel(mc.getEntityModels().bakeLayer(ModModelLayers.PICKELHAUBE_HIGH));
			}
		});
	}
	
}
