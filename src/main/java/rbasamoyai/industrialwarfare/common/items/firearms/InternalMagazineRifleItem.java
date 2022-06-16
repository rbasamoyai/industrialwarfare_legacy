package rbasamoyai.industrialwarfare.common.items.firearms;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemDataHandler;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleContainer;
import rbasamoyai.industrialwarfare.common.entities.BulletEntity;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;

public abstract class InternalMagazineRifleItem extends InternalMagazineFirearmItem {
	
	private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".attachments_rifle");
	
	public InternalMagazineRifleItem(Item.Properties itemProperties, InternalMagazineFirearmItem.Properties firearmProperties) {
		super(itemProperties, firearmProperties);
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = h.extractAmmo();
			// TODO: process ammo stack
			
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
			
			if (this.needsCycle) h.setCycled(false);
			h.setFired(true);
			h.setAction(ActionType.NOTHING, this.cooldownTime);
			h.setRecoilTicks(0);
			h.setRecoil(recoilPitch, recoilYaw);
		});
	}
	
	@Override
	public boolean canOpenScreen(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::isFinishedAction).orElse(false);
	}
	
	@Override
	public INamedContainerProvider getItemContainerProvider(ItemStack stack) {
		IContainerProvider provider = AttachmentsRifleContainer.getServerContainerProvider(stack);
		return new SimpleNamedContainerProvider(provider, TITLE);
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		if (isAiming(firearm)) return;
		super.startReload(firearm, shooter);
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		if (this.needsCycle) {
			super.startCycle(firearm, shooter);
		}
	}
	
	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity.level.isClientSide) return true;
		if (isMeleeing(stack)) return false;
		if (!isFinishedAction(stack)) return true;
		return super.onEntitySwing(stack, entity);
	}

}
