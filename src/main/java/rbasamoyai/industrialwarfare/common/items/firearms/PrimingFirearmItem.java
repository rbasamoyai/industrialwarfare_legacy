package rbasamoyai.industrialwarfare.common.items.firearms;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class PrimingFirearmItem extends SingleShotFirearmItem {

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
		
		if (level.isClientSide && shooter instanceof ClientPlayerEntity && !shooter.isPassenger()) {
			getDataHandler(stack).ifPresent(h -> {
				if (h.isCycled() && !h.isAiming()) {
					((ClientPlayerEntity) shooter).input.forwardImpulse *= this.slowdownFactor;
					((ClientPlayerEntity) shooter).input.leftImpulse *= this.slowdownFactor;
				}
			});
		} else {
			getDataHandler(stack).ifPresent(h -> {
				if (!selected) {
					h.setCycled(false);
				} else {
					if (h.isCycled()) {
						h.setDisplaySprinting(false);
						if (shooter.isUsingItem());
					}
				}
			});
		}
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
