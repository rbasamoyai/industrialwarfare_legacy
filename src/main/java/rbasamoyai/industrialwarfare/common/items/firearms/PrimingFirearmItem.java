package rbasamoyai.industrialwarfare.common.items.firearms;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemDataHandler;

public abstract class PrimingFirearmItem extends SingleShotFirearmItem {

	private static final UUID PRIMED_MOVEMENT_SPEED_UUID = UUID.fromString("2e92d292-de54-4119-8f1b-3d05bdcbe624");
	
	private final int unprimingTime;
	private final float slowdownFactor;
	
	public PrimingFirearmItem(Item.Properties itemProperties, PrimingFirearmItem.AbstractProperties<?> firearmProperties) {
		super(itemProperties, firearmProperties.needsCycle(true));
		this.unprimingTime = firearmProperties.unprimingTime;
		this.slowdownFactor = firearmProperties.slowdownFactor;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!(entity instanceof LivingEntity)) return;
		LivingEntity shooter = (LivingEntity) entity;
		
		getDataHandler(stack).ifPresent(h -> {
			if (!selected) {
				h.setCycled(false);
			} else {
				if (h.isCycled()) {
					h.setDisplaySprinting(false);
					shooter.setSprinting(false);
				}
			}
		});
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		super.shoot(firearm, shooter);
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(false);
		});
	}
	
	@Override
	protected void goToPreviousStance(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.PREVIOUS_STANCE, getTimeModifiedByEntity(shooter, this.getUnprimingTime(firearm, shooter)));
		});
	}
	
	protected int getUnprimingTime(ItemStack firearm, LivingEntity shooter) {
		return this.unprimingTime;
	}
	
	@Override
	protected void actuallyDoPreviousStance(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(false);
			h.setAction(ActionType.NOTHING, 1);
		});
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.getCyclingTime(firearm, shooter)));
		});
	}
	
	protected int getCyclingTime(ItemStack firearm, LivingEntity shooter) {
		return this.cycleTime;
	}
	
	@Override
	protected void endCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(true);
			h.setAction(ActionType.NOTHING, 1);
		});
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		if (getDataHandler(stack).map(IFirearmItemDataHandler::isCycled).orElse(false)) {
			return ImmutableMultimap.of(Attributes.MOVEMENT_SPEED, new AttributeModifier(PRIMED_MOVEMENT_SPEED_UUID, "industrialwarfare.item.firearm.primed_movement_speed", (double) this.slowdownFactor, Operation.MULTIPLY_TOTAL));
		}
		return super.getAttributeModifiers(slot, stack);
	}
	
	public abstract static class AbstractProperties<T extends AbstractProperties<T>> extends FirearmItem.AbstractProperties<T> {
		private int unprimingTime;
		private float slowdownFactor = 1.0f;
		
		public T unprimingTime(int unprimingTime) {
			this.unprimingTime = unprimingTime;
			return this.thisObj;
		}
		
		public T slowdownFactor(float slowdownFactor) {
			this.slowdownFactor = slowdownFactor;
			return this.thisObj;
		}
	}
	
	public static class Properties extends AbstractProperties<Properties> {
		@Override protected Properties getThis() { return this; }
	}

}
