package rbasamoyai.industrialwarfare.common.items.firearms;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.SingleShotDataHandler;
import rbasamoyai.industrialwarfare.common.entities.BulletEntity;
import rbasamoyai.industrialwarfare.common.tags.IWItemTags;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;

public abstract class SingleShotFirearmItem extends FirearmItem {

	public SingleShotFirearmItem(Item.Properties itemProperties, FirearmItem.AbstractProperties<?> firearmProperties) {
		super(itemProperties, firearmProperties, SingleShotDataHandler::new);
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
			float spread = h.isAiming() ? this.spread : this.hipfireSpread;
			spread *= 1.0f + (3.0f - (quality + durability + effectiveness) / 3.0f);
			float velocity = this.muzzleVelocity * (quality + durability) / 2.0f;
			bullet.shoot(lookVector.x, lookVector.y, lookVector.z, velocity, spread);
			
			shooter.level.addFreshEntity(bullet);
			
			float recoilPitch = this.verticalRecoilSupplier.apply(shooter);
			float recoilYaw = this.horizontalRecoilSupplier.apply(shooter);
			
			h.setFired(true);
			h.setAction(ActionType.NOTHING, this.cooldownTime);
			h.setRecoilTicks(0);
			h.setRecoil(recoilPitch, recoilYaw);
		});
	}

	@Override 
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.NOTHING, 1);
		});
	}

	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		if (isAiming(firearm)) return;
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.START_RELOADING, getTimeModifiedByEntity(shooter, this.reloadTime));
		});
	}
	
	@Override
	protected void actuallyStartReloading(ItemStack firearm, LivingEntity shooter) {
		this.reload(firearm, shooter);
	}

	@Override
	protected void endCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.NOTHING, 1);
		});
	}

	@Override
	protected void reload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = shooter.getProjectile(firearm);
			if (h.isFull() || !this.getAllSupportedProjectiles().test(ammo)) {
				h.setAction(ActionType.NOTHING, 1);
				return;
			}
			
			if (IWItemTags.CHEAT_AMMO.contains(ammo.getItem()) || shooter instanceof PlayerEntity && ((PlayerEntity) shooter).abilities.instabuild) {
				ammo = ammo.copy();
			}
			
			h.insertAmmo(ammo);
			h.setFired(false);
			h.setAction(ActionType.NOTHING, 1);
		});
	}

}
