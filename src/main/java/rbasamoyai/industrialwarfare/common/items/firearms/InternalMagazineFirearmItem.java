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
import rbasamoyai.industrialwarfare.utils.AnimUtils;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.util.GeckoLibUtil;

public abstract class InternalMagazineFirearmItem extends FirearmItem implements ISpeedloadable {

	public static final int ANIM_LOOP_MASK = 0x80000000;
	
	public static final int ANIM_IDLE = 0;
	public static final int ANIM_FIRING = 1;
	public static final int ANIM_CYCLE = 2;
	public static final int ANIM_RELOAD_START = 3;
	public static final int ANIM_RELOAD_START_SPEED = 4;
	public static final int ANIM_RELOAD = 5;
	public static final int ANIM_RELOAD_SPEED = 6;
	public static final int ANIM_RELOAD_END = 7;
	public static final int ANIM_TOGGLE_TO_MELEE = 8;
	public static final int ANIM_TOGGLE_TO_SHOOT = 9;
	public static final int ANIM_AIMING = 10;
	
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
			AnimUtils.syncItemStackAnim(stack, (LivingEntity) entity, this, ANIM_IDLE | ANIM_LOOP_MASK);
		}
	}

	@Override
	protected void reload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			ItemStack ammo = shooter.getProjectile(firearm);
			if (ammo.isEmpty()) {
				AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_IDLE | ANIM_LOOP_MASK);
				h.setAction(ActionType.NOTHING, 0);
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
				AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_IDLE | ANIM_LOOP_MASK);
				h.setAction(ActionType.NOTHING, 0);
			}
		});
	}
	
	protected void midReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.RELOADING, getTimeModifiedByEntity(shooter, this.reloadTime));
		});
		// TODO: speedloaders
		AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_RELOAD);
	}
	
	protected void endReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.reloadEndTime));
		});
		AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_RELOAD_END);
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.cycleTime));
		});
		AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_CYCLE);
	}
	
	@Override
	protected void endCycle(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setCycled(true);
			h.setAction(ActionType.NOTHING, 0);
		});
		AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_IDLE | ANIM_LOOP_MASK);
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAction(ActionType.RELOADING, getTimeModifiedByEntity(shooter, this.reloadStartTime));
		});
		// TODO: speedloaders
		AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_RELOAD_START);
	}
	
	@Override
	protected void startAiming(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAiming(true);
		});
		if (!shooter.level.isClientSide) {
			AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_AIMING | ANIM_LOOP_MASK);
		}
	}
	
	@Override
	protected void stopAiming(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAiming(false);
		});
		if (!shooter.level.isClientSide) {
			AnimUtils.syncItemStackAnim(firearm, shooter, this, ANIM_IDLE | ANIM_LOOP_MASK);
		}
	}
	
	@Override
	protected void doNothing(ItemStack firearm, LivingEntity shooter) {
		AnimUtils.syncItemStackAnim(firearm, shooter, this, (isAiming(firearm) ? ANIM_AIMING : ANIM_IDLE) | ANIM_LOOP_MASK);
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
	
	public static final int ANIM_STATE_MASK = 0x0fffffff;
	
	@Override
	public void onAnimationSync(int id, int state) {
		boolean loop = (state & ANIM_LOOP_MASK) != 0;
		int state2 = state & ANIM_STATE_MASK;
		
		String anim;
		switch (state2) {
		case ANIM_FIRING: anim = "firing"; break;
		case ANIM_CYCLE: anim = "cycle"; break;
		case ANIM_RELOAD_START: anim = "reload_start"; break;
		// case ANIM_RELOAD_START_SPEED: anim = "reload_start_speed"; break;
		case ANIM_RELOAD: anim = "reload"; break;
		// case ANIM_RELOAD_SPEED: anim = "reload_speed"; break;
		case ANIM_RELOAD_END: anim = "reload_end"; break;
		case ANIM_TOGGLE_TO_MELEE: anim = ""; break;
		case ANIM_TOGGLE_TO_SHOOT: anim = ""; break;
		case ANIM_AIMING: anim = "aiming"; break;
		default: anim = "idle";
		}
		
		final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, "controller");
		controller.markNeedsReload();
		controller.setAnimation(new AnimationBuilder().addAnimation(anim, loop));
	}
	
}