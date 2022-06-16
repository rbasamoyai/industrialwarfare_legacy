package rbasamoyai.industrialwarfare.common.items.firearms;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.RevolverDataHandler;
import rbasamoyai.industrialwarfare.common.entities.BulletEntity;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;

public abstract class RevolverFirearmItem extends FirearmItem {
	
	public RevolverFirearmItem(Item.Properties itemProperties, RevolverFirearmItem.Properties firearmProperties) {
		super(itemProperties, firearmProperties, attachments -> {
			RevolverDataHandler handler = new RevolverDataHandler(attachments);
			handler.setMagazineSize(firearmProperties.cylinderSize);
			return handler;
		});
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = h.extractAmmo();
			if (ammo.isEmpty()) {
				h.setCycled(false);
				this.startCycle(firearm, shooter);
				this.setDamage(firearm, this.getDamage(firearm) - 1);
				return;
			}
			
			float quality = h.getQuality();
			float durability = 1.0f  - (float) firearm.getDamageValue() / (float) firearm.getMaxDamage();
			float effectiveness = getEffectivenessFromEntity(shooter);
			
			float damage = this.baseDamage * (quality + durability) / 2.0f;
			BulletEntity bullet = new BulletEntity(shooter.level, shooter, damage, this.headshotMultiplier);
			bullet.setItem(new ItemStack(PartItemInit.PART_BULLET.get()));
			
			Vector3d lookVector = shooter.getViewVector(1.0f);
			float spread = isAiming(firearm) ? this.spread : this.hipfireSpread;
			spread *= 1.0f + (3.0f - (quality + durability + effectiveness) / 3.0f);
			float velocity = this.muzzleVelocity * (quality + durability) / 2.0f;
			bullet.shoot(lookVector.x, lookVector.y, lookVector.z, velocity, spread);
			
			shooter.level.addFreshEntity(bullet);
			
			float recoilPitch = this.verticalRecoilSupplier.apply(shooter);
			float recoilYaw = this.horizontalRecoilSupplier.apply(shooter);
			
			h.setCycled(false);
			h.setAction(ActionType.NOTHING, this.cooldownTime);
			h.setRecoilTicks(0);
			h.setRecoil(recoilPitch, recoilYaw);
		});
	}

	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, this.cycleTime);
		});
	}

	@Override
	protected void endCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(true);
			h.setAmmoPosition(this.wrapInt(h.getAmmoPosition() + 1, h.getMagazineSize()));
			h.setAction(ActionType.NOTHING, 1);
		});
	}
	
	protected boolean isEmptyOrFired(ItemStack stack) { return stack.isEmpty() || stack.getItem() == ItemInit.CARTRIDGE_CASE.get(); }
	
	protected int wrapInt(int wrap, int mod) {
		if (wrap < 0) {
			return wrap % mod + mod;
		} else if (wrap >= mod) {
			return wrap % mod; 
		} else {
			return wrap;
		}
	}
	
	public static class Properties extends FirearmItem.AbstractProperties<Properties> {
		private int cylinderSize;
		
		@Override protected Properties getThis() { return this; }
		
		public Properties cylinderSize(int cylinderSize) {
			this.cylinderSize = cylinderSize;
			return this;
		}
	}

}
