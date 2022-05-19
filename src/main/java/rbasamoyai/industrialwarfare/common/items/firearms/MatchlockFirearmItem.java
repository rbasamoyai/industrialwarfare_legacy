package rbasamoyai.industrialwarfare.common.items.firearms;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemDataHandler;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;

public abstract class MatchlockFirearmItem extends PrimingFirearmItem {

	private int reloadUnprimingTime;
	private int reloadUnprimingNoCordTime;
	private int unprimingNoCordTime;
	private int primingNoCordTime; 
	
	public MatchlockFirearmItem(Item.Properties itemProperties, MatchlockFirearmItem.Properties firearmProperties) {
		super(itemProperties, firearmProperties.slowdownFactor(-0.3f));
		this.reloadUnprimingTime = firearmProperties.reloadUnprimingTime;
		this.reloadUnprimingNoCordTime = firearmProperties.reloadUnprimingNoCordTime;
		this.unprimingNoCordTime = firearmProperties.unprimingNoCordTime;
		this.primingNoCordTime = firearmProperties.primingNoCordTime;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide || !(entity instanceof LivingEntity)) return;
		LivingEntity shooter = (LivingEntity) entity;
		ItemStack offhand = shooter.getOffhandItem();
		if (offhand == stack) return;
		
		getDataHandler(stack).ifPresent(h -> {
			if (!selected) {
				h.setCycled(false);
				h.setFired(false);
			}
			if (!(shooter.getOffhandItem().getItem() instanceof MatchCordItem) || !MatchCordItem.isLit(shooter.getOffhandItem())) {
				h.setFired(false);
			}
		});
	}
	
	@Override
	protected boolean canShoot(ItemStack firearm, LivingEntity shooter) {
		return super.canShoot(firearm, shooter) && isFired(firearm);
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		boolean previousFired = isFired(firearm);
		boolean wasCycled = isCycled(firearm);
		super.shoot(firearm, shooter);
		getDataHandler(firearm).ifPresent(h -> {
			h.setFired(previousFired);
			h.setCycled(wasCycled);
		});
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		if (shooter.getOffhandItem().getItem() instanceof MatchCordItem && MatchCordItem.isLit(shooter.getOffhandItem())) {
			super.startCycle(firearm, shooter);
		}
	}
	
	@Override
	protected int getCyclingTime(ItemStack firearm, LivingEntity shooter) {
		return getDataHandler(firearm).map(IFirearmItemDataHandler::isFired).orElse(false) ? this.primingNoCordTime : this.cycleTime;
	}
	
	@Override
	protected void endCycle(ItemStack firearm, LivingEntity shooter) {
		ItemStack offhand = shooter.getOffhandItem();
		if (offhand.getItem() instanceof MatchCordItem && MatchCordItem.isLit(offhand)) {
			super.endCycle(firearm, shooter);
			getDataHandler(firearm).ifPresent(h -> h.setFired(true));
		}
	}
	
	@Override
	public int getUnprimingTime(ItemStack firearm, LivingEntity shooter) {
		return getDataHandler(firearm).map(IFirearmItemDataHandler::isFired).orElse(false) ? super.getUnprimingTime(firearm, shooter) : this.unprimingNoCordTime;
	}
	
	@Override
	protected void actuallyDoPreviousStance(ItemStack firearm, LivingEntity shooter) {
		super.actuallyDoPreviousStance(firearm, shooter);
		getDataHandler(firearm).ifPresent(h -> {
			h.setFired(false);
		});
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			if (h.isAiming()) return;
			int time;
			if (!h.isCycled()) {
				time = this.reloadTime;
			} else if (h.isFired()) {
				time = this.reloadUnprimingTime;
			} else {
				time = this.reloadUnprimingNoCordTime;
			}
			h.setAction(ActionType.START_RELOADING, getTimeModifiedByEntity(shooter, time));
		});
	}
	
	@Override
	protected void reload(ItemStack firearm, LivingEntity shooter) {
		super.reload(firearm, shooter);
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(false);
		});
	}
	
	public static class Properties extends PrimingFirearmItem.AbstractProperties<Properties> {
		@Override protected Properties getThis() { return this; }
		
		private int reloadUnprimingTime;
		private int reloadUnprimingNoCordTime;
		private int unprimingNoCordTime;
		private int primingNoCordTime;
		
		public Properties reloadUnreadyTime(int time) {
			this.reloadUnprimingTime = time;
			return this;
		}
		
		public Properties reloadUnreadyNoCordTime(int time) {
			this.reloadUnprimingNoCordTime = time; 
			return this;
		}
		
		public Properties unprimingNoCordTime(int time) {
			this.unprimingNoCordTime = time;
			return this;
		}
		
		public Properties primingNoCordTime(int time) {
			this.primingNoCordTime = time;
			return this;
		}
	}

}
