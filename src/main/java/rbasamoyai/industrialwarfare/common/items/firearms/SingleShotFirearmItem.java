package rbasamoyai.industrialwarfare.common.items.firearms;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.PacketDistributor;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.SingleShotDataHandler;
import rbasamoyai.industrialwarfare.common.entities.BulletEntity;
import rbasamoyai.industrialwarfare.common.tags.IWItemTags;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages.CApplyRecoil;

public abstract class SingleShotFirearmItem extends FirearmItem {

	public SingleShotFirearmItem(Item.Properties itemProperties, FirearmItem.Properties firearmProperties) {
		super(itemProperties, firearmProperties.needsCycle(false), SingleShotDataHandler::new);
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = h.extractAmmo();
			// TODO: process ammo stack
			
			float quality = h.getQuality();
			float durability = 1 - firearm.getDamageValue() / firearm.getMaxDamage();
			float effectiveness = getEffectivenessFromEntity(shooter);
			
			float damage = this.baseDamage * (quality + 0.5f * durability) / 1.5f;
			BulletEntity bullet = new BulletEntity(shooter.level, shooter, damage, this.headshotMultiplier);
			bullet.setItem(new ItemStack(PartItemInit.PART_BULLET.get()));
			
			Vector3d lookVector = shooter.getViewVector(1.0f);
			float spread = isAiming(firearm) ? this.spread : this.hipfireSpread;
			spread *= (2.0f - (0.5f * quality + 0.5f * durability + effectiveness) / 2.0f);
			bullet.shoot(lookVector.x, lookVector.y, lookVector.z, this.muzzleVelocity, spread);
			
			shooter.level.addFreshEntity(bullet);
			
			shooter.yRotO = shooter.yRot;
			shooter.xRotO = shooter.xRot;
			shooter.yRot = MathHelper.wrapDegrees(shooter.yRot + this.horizontalRecoilSupplier.apply(shooter));
			shooter.xRot = MathHelper.wrapDegrees(shooter.xRot - this.verticalRecoilSupplier.apply(shooter));
			
			if (shooter instanceof ServerPlayerEntity) {
				PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) shooter);
				CApplyRecoil msg = new CApplyRecoil(shooter.xRot, shooter.yRot);
				IWNetwork.CHANNEL.send(target, msg);
			}
			
			h.setFired(true);
			h.setAction(ActionType.NOTHING, this.cooldownTime);
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
			if (ammo.isEmpty() || h.isFull()) {
				h.setAction(ActionType.NOTHING, 1);
				return;
			}
			
			if (IWItemTags.CHEAT_AMMO.contains(ammo.getItem()) || shooter instanceof PlayerEntity && ((PlayerEntity) shooter).abilities.instabuild) {
				ammo = ammo.copy();
			}
			ammo = ammo.split(1);
			
			if (super.getAllSupportedProjectiles().test(ammo)) {
				h.insertAmmo(ammo);
			}
			h.setFired(false);
			h.setAction(ActionType.NOTHING, 1);
		});
	}

}
