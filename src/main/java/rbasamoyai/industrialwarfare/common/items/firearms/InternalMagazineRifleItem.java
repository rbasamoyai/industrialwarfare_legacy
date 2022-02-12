package rbasamoyai.industrialwarfare.common.items.firearms;

import java.util.function.Predicate;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemDataHandler;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleContainer;
import rbasamoyai.industrialwarfare.common.entities.BulletEntity;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages.CApplyRecoil;

public abstract class InternalMagazineRifleItem extends InternalMagazineFirearmItem {
	
	private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".attachments_rifle");
	
	public InternalMagazineRifleItem(Item.Properties itemProperties, FirearmItem.Properties firearmProperties, int magazineSize, Predicate<ItemStack> speedloaderPredicate) {
		super(itemProperties, firearmProperties, magazineSize, speedloaderPredicate);
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = h.extractAmmo();
			// TODO: process ammo stack
			
			float quality = h.getQuality();
			float durability = 1 - firearm.getDamageValue() / firearm.getMaxDamage();
			float effectiveness = getEffectivenessFromEntity(shooter);
			
			float damage = this.baseDamage * (quality + 0.5f * effectiveness) / 1.5f;
			BulletEntity bullet = new BulletEntity(shooter.level, shooter, damage, this.headshotMultiplier);
			
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
			
			if (this.needsCycle) h.setCycled(false);
			h.setFired(true);
			h.setAction(ActionType.NOTHING, this.cooldownTime);
		});
	}
	
	@Override
	public boolean canCustomize(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::isFinishedAction).orElse(false);
	}
	
	@Override
	public INamedContainerProvider getAttachmentsContainerProvider(ItemStack stack) {
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
	
	@Override
	public boolean shouldHideCrosshair(ItemStack stack) {
		return true;
	}

}
