package rbasamoyai.industrialwarfare.common.items;

import java.util.function.Supplier;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.LazyValue;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public enum IWArmorMaterial implements IArmorMaterial {
	WOOD("wood", 5, new int[] { 1, 2, 3, 1 }, 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0f, 0.0f, () -> {
		return Ingredient.of(ItemTags.PLANKS);
	});
	
	private static final int[] HEALTH_PER_SLOT = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int durabilityMultiplier;
	private final int[] slotProtections;
	private final int enchantmentValue;
	private final SoundEvent sound;
	private final float toughness;
	private final float knockbackResistance;
	private final LazyValue<Ingredient> repairIngredient;
	
	private IWArmorMaterial(String name, int durabilityMultiplier, int[] slotProtections, int enchantmentValue,
			SoundEvent sound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.slotProtections = slotProtections;
		this.enchantmentValue = enchantmentValue;
		this.sound = sound;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
		this.repairIngredient = new LazyValue<>(repairIngredient);
	}

	@Override public int getDurabilityForSlot(EquipmentSlotType type) { return HEALTH_PER_SLOT[type.getIndex()] * this.durabilityMultiplier; }
	@Override public int getDefenseForSlot(EquipmentSlotType type) { return this.slotProtections[type.getIndex()]; }
	@Override public int getEnchantmentValue() { return this.enchantmentValue; }
	@Override public SoundEvent getEquipSound() { return this.sound; }
	@Override public Ingredient getRepairIngredient() { return this.repairIngredient.get(); }
	@Override public String getName() { return this.name; }
	@Override public float getToughness() { return this.toughness; }
	@Override public float getKnockbackResistance() { return this.knockbackResistance; }

}
