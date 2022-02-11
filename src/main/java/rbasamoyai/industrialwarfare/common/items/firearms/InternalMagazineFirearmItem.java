package rbasamoyai.industrialwarfare.common.items.firearms;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.InternalMagazineDataHandler;
import rbasamoyai.industrialwarfare.common.items.ISpeedloadable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.util.GeckoLibUtil;

public abstract class InternalMagazineFirearmItem extends FirearmItem implements ISpeedloadable {
	
	private final Predicate<ItemStack> speedloaderPredicate;
	
	public InternalMagazineFirearmItem(Item.Properties itemProperties, FirearmItem.Properties firearmProperties, int magazineSize, Predicate<ItemStack> speedloaderPredicate) {
		super(itemProperties, firearmProperties, () -> {
			InternalMagazineDataHandler handler = new InternalMagazineDataHandler();
			handler.setMagazineSize(magazineSize);
			return handler;
		});
		this.speedloaderPredicate = speedloaderPredicate;
	}
	
	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return super.getAllSupportedProjectiles().or(this.speedloaderPredicate);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		
		if (!selected && entity instanceof LivingEntity && !world.isClientSide) {
			AnimationController<?> controller = GeckoLibUtil.getControllerForStack(factory, stack, "controller");
			controller.markNeedsReload();
		}
	}

	@Override
	protected void reload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = shooter.getProjectile(firearm);
			if (ammo.isEmpty()) {
				h.setAction(ActionType.NOTHING, 1);
				return;
			}
			
			if (shooter instanceof PlayerEntity && ((PlayerEntity) shooter).abilities.instabuild) {
				ammo = ammo.copy();
			}
			
			boolean ammoMatched = false;
			if (this.speedloaderPredicate.test(ammo) /* TODO: checking to see if can speedload, otherwise load regularly */) {
				// TODO: speedloaders
				ammoMatched = true;
			} else if (super.getAllSupportedProjectiles().test(ammo)) {
				h.insertAmmo(ammo);
				ammoMatched = true;
			}
			
			if (ammoMatched && this.getAllSupportedProjectiles().test(shooter.getProjectile(firearm)) && !h.isFull()) {
				this.midReload(firearm, shooter);
			} else if (h.hasAmmo()) {
				this.endReload(firearm, shooter);
			} else {
				h.setAction(ActionType.NOTHING, 1);
			}
		});
	}
	
	@Override
	protected void actuallyStartReloading(ItemStack firearm, LivingEntity shooter) {
		this.midReload(firearm, shooter);
	}
	
	protected void midReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.RELOADING, getTimeModifiedByEntity(shooter, this.reloadTime));
		});
		// TODO: speedloaders
	}
	
	protected void endReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.reloadEndTime));
		});
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.cycleTime));
		});
	}
	
	@Override
	protected void endCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(true);
			h.setFired(false);
			h.setAction(ActionType.NOTHING, 1);
		});
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			if (h.isFull()) return;
			h.setAction(ActionType.START_RELOADING, getTimeModifiedByEntity(shooter, this.reloadStartTime));
		});
		// TODO: speedloaders
	}
	
	@Override
	protected void startAiming(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAiming(true);
			h.setAction(ActionType.NOTHING, 10);
		});
	}
	
	@Override
	protected void stopAiming(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAiming(false);
			h.setAction(ActionType.NOTHING, 10);
		});
	}
	
	@Override
	public boolean canSpeedload(ItemStack stack) {
		// TODO: speedloader capability
		return false;
	}
	
	@Override
	public Predicate<ItemStack> getSpeedloaderPredicate() {
		return this.speedloaderPredicate;
	}
	
}