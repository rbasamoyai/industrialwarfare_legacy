package rbasamoyai.industrialwarfare.common.items.firearms.complete;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemDataHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.firearms.RevolverFirearmItem;
import rbasamoyai.industrialwarfare.common.tags.IWItemTags;
import rbasamoyai.industrialwarfare.core.init.SoundEventInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.AnimBroadcastUtils;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class ColtSAAFirearmItem extends RevolverFirearmItem {

	private static final int RELOAD_EMPTY_TIME = 15;
	private static final int RELOAD_SEEK_TIME = 5;
	private static final float HAMMER_ROT_CYCLED = (float) Math.toRadians(47.5d);
	private static final float CYLINDER_ROT = (float) Math.toRadians(60.0d);
	
	public ColtSAAFirearmItem() { 
		super(new Item.Properties()
							.stacksTo(1)
							.durability(1200)
							.tab(IWItemGroups.TAB_WEAPONS)
							.setISTER(() -> FirearmRenderer::new),
					new RevolverFirearmItem.Properties()
							.ammoPredicate(s -> s.getItem() == ItemInit.AMMO_GENERIC.get() || s.getItem() == ItemInit.INFINITE_AMMO_GENERIC.get())
							.baseDamage(15.0f)
							.headshotMultiplier(2.0f)
							.spread(2.5f)
							.hipfireSpread(5.0f)
							.muzzleVelocity(4.5f)
							.horizontalRecoil(e -> 1.0f * (float) e.getRandom().nextGaussian())
							.verticalRecoil(e -> 4.0f + 1.0f * e.getRandom().nextFloat())
							.cooldownTime(20)
							.cycleTime(15)
							.drawTime(5)
							.reloadStartTime(30)
							.reloadTime(20)
							.reloadEndTime(30)
							.projectileRange(20)
							.cylinderSize(6));
	}
	
	@Override
	public boolean canOpenScreen(ItemStack stack) {
		return false;
	}
	
	@Override
	public INamedContainerProvider getItemContainerProvider(ItemStack stack) {
		return null;
	}
	
	/*
	 * ANIMATION CONTROL METHODS
	 */
	
	@Override
	protected void onSelect(ItemStack firearm, LivingEntity shooter) {
		super.onSelect(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_SELECT_FIREARM);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("select_firearm", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		super.shoot(firearm, shooter);
		if (!shooter.level.isClientSide) {
			if (getDataHandler(firearm).map(IFirearmItemDataHandler::getAction).map(ActionType.CYCLING::equals).orElse(false)) {
				return;
			}
			
			ServerWorld slevel = (ServerWorld) shooter.level;
			shooter.level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEventInit.REVOLVER_FIRED.get(), SoundCategory.MASTER, 4.0f, 1.0f);
			
			Vector3d viewVector = shooter.getViewVector(1.0f);
			Vector3d smokePos = shooter.getEyePosition(1.0f).add(viewVector.scale(0.75d));
			Vector3d smokeDelta = viewVector.scale(0.5d);
			int count = 10 + random.nextInt(11);
			for (ServerPlayerEntity splayer : slevel.getPlayers(p -> true)) {
				slevel.sendParticles(splayer, ParticleTypes.POOF, true, smokePos.x, smokePos.y, smokePos.z, count, smokeDelta.x, smokeDelta.y, smokeDelta.z, 0.01d);
			}
			
			boolean isAiming = isAiming(firearm);
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, isAiming ? ANIM_ADS_FIRING : ANIM_HIP_FIRING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(isAiming ? "ads_firing" : "hip_firing", false));
			upperBody.add(new Tuple<>(isAiming ? "ads_aiming" : "hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		super.startCycle(firearm, shooter);
		if (!shooter.level.isClientSide) {
			boolean isAiming = isAiming(firearm);
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, isAiming ? ANIM_ADS_CYCLING : ANIM_HIP_CYCLING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(isAiming ? "ads_cycling" : "hip_cycling", false));
			upperBody.add(new Tuple<>(isAiming ? "ads_aiming" : "hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected void doNothing(ItemStack firearm, LivingEntity shooter) {
		super.doNothing(firearm, shooter);
		if (!shooter.level.isClientSide) {
			int animId;
			String animStr;
			if (isAiming(firearm)) {
				animId = ANIM_ADS_AIMING;
				animStr = "ads_aiming";
			} else if (shooter.isSprinting()) {
				animId = ANIM_SPRINTING;
				animStr = "sprinting";
			} else {
				animId = ANIM_HIP_AIMING;
				animStr = "hip_aiming";
			}
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, animId);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", animStr, true, 1.0f);
		}
	}
	
	@Override
	public void startAiming(ItemStack firearm, LivingEntity shooter) {
		super.startAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_ADS_AIMING);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", "ads_aiming", true, 1.0f);
		}
	}
	
	@Override
	public void stopAiming(ItemStack firearm, LivingEntity shooter) {
		super.stopAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_HIP_AIMING);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", "hip_aiming", true, 1.0f);
		}
	}
	
	@Override
	public void startSprinting(ItemStack firearm, LivingEntity shooter) {
		super.startSprinting(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_SPRINTING);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", "sprinting", true, 1.0f);
		}
	}
	
	@Override
	public void stopSprinting(ItemStack firearm, LivingEntity shooter) {
		super.stopSprinting(firearm, shooter);
		this.doNothing(firearm, shooter);
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		
		getDataHandler(firearm).ifPresent(h -> {
			h.setAmmoPosition(this.wrapInt(h.getAmmoPosition() - 1, h.getMagazineSize()));
			
			h.setAction(ActionType.START_RELOADING, getTimeModifiedByEntity(shooter, this.reloadStartTime));
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_START);
				
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("reload_start", false));
			upperBody.add(new Tuple<>("reload_hold", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		});
	}
	
	@Override
	protected void actuallyStartReloading(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		
		getDataHandler(firearm).ifPresent(h -> {
			int pos = h.getAmmoPosition();
			if (h.isFull() || !this.getAllSupportedProjectiles().test(shooter.getProjectile(firearm))) {
				h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.reloadEndTime));
				h.setAmmoPosition(this.wrapInt(++pos, h.getMagazineSize()));
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_END);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>("reload_end", false));
				upperBody.add(new Tuple<>("hip_aiming", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
				return;
			}
			
			ItemStack currentChamber = h.peekAmmo(pos);
			if (this.isEmptyOrFired(currentChamber)) {
				boolean isEmpty = currentChamber.isEmpty();
				h.setAction(ActionType.RELOADING, isEmpty ? RELOAD_EMPTY_TIME : this.reloadTime);
				h.setAmmoPosition(this.wrapInt(--pos, h.getMagazineSize()));
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, isEmpty ? ANIM_RELOAD_EMPTY : ANIM_RELOAD_EXTRACT);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>(isEmpty ? "reload_empty" : "reload_extract", false));
				upperBody.add(new Tuple<>("reload_hold", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
			} else {
				h.setAction(ActionType.START_RELOADING, getTimeModifiedByEntity(shooter, RELOAD_SEEK_TIME));
				h.setAmmoPosition(this.wrapInt(++pos, h.getMagazineSize()));
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_SEEK);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>("reload_seek", false));
				upperBody.add(new Tuple<>("reload_hold", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
			}
		});
	}
	
	@Override
	protected void reload(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		
		getDataHandler(firearm).ifPresent(h -> {
			int pos = this.wrapInt(h.getAmmoPosition() + 1, h.getMagazineSize());
			h.setAmmoPosition(pos);
			ItemStack ammo = shooter.getProjectile(firearm);
			if (h.isFull() || !this.getAllSupportedProjectiles().test(ammo)) {
				h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.reloadEndTime));
				h.setAmmoPosition(this.wrapInt(++pos, h.getMagazineSize()));
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_END);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>("reload_end", false));
				upperBody.add(new Tuple<>("hip_aiming", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
				return;
			}
			
			if (IWItemTags.CHEAT_AMMO.contains(ammo.getItem()) || shooter instanceof PlayerEntity && ((PlayerEntity) shooter).abilities.instabuild) {
				ammo = ammo.copy();
			}
			h.insertAmmo(ammo);
			
			int newPos = this.wrapInt(++pos, h.getMagazineSize());
			if (h.isFull() || !this.getAllSupportedProjectiles().test(shooter.getProjectile(firearm))) {
				h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.reloadEndTime));
				h.setAmmoPosition(newPos);
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_END);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>("reload_end", false));
				upperBody.add(new Tuple<>("hip_aiming", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
				return;
			}
			
			ItemStack newChamber = h.peekAmmo(newPos);
			if (this.isEmptyOrFired(newChamber)) {
				boolean isEmpty = newChamber.isEmpty();
				h.setAction(ActionType.RELOADING, isEmpty ? RELOAD_EMPTY_TIME : this.reloadTime);
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, isEmpty ? ANIM_RELOAD_EMPTY : ANIM_RELOAD_EXTRACT);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>(isEmpty ? "reload_empty" : "reload_extract", false));
				upperBody.add(new Tuple<>("reload_hold", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
			} else if (h.isFull()) {
				h.setAction(ActionType.CYCLING, getTimeModifiedByEntity(shooter, this.reloadEndTime));
				h.setAmmoPosition(this.wrapInt(++pos, h.getMagazineSize()));
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_END);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>("reload_end", false));
				upperBody.add(new Tuple<>("reload_hold", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
			} else {
				h.setAction(ActionType.START_RELOADING, getTimeModifiedByEntity(shooter, RELOAD_SEEK_TIME));
				h.setAmmoPosition(this.wrapInt(++pos, h.getMagazineSize()));
				
				AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_SEEK);
				
				List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
				upperBody.add(new Tuple<>("reload_seek", false));
				upperBody.add(new Tuple<>("reload_hold", true));
				AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
			}
		});
	}
	
	/*
	 * FIRST-PERSON ANIMATION METHODS
	 */
	
	@Override
	public void registerControllers(AnimationData data) {
		AnimationController<?> controller = new AnimationController<>(this, "controller", 1, this::firstPersonPredicate);
		controller.registerSoundListener(this::soundListener);
		controller.registerCustomInstructionListener(this::customInstructionListener);
		controller.registerParticleListener(this::particleListener);
		controller.markNeedsReload();
		data.addAnimationController(controller);
	}
	
	private <E extends Item & IAnimatable> PlayState firstPersonPredicate(AnimationEvent<E> event) {
		return PlayState.CONTINUE;
	}
	
	@Override
	public void setupAnimationState(FirearmRenderer renderer, ItemStack stack, MatrixStack matrixStack, float aimProgress) {
		if (renderer.getUniqueID(this).intValue() == -1) return;
		
		getDataHandler(stack).ifPresent(h -> {
			ActionType type = h.getAction();
			boolean reloading = type == ActionType.RELOADING;
			
			int pos = h.getAmmoPosition();
			float rot = (float)(pos + (reloading ? 2 : 0)) * -CYLINDER_ROT;
			renderer.addToBoneRotation("cylinder", 0.0f, 0.0f, rot);
			
			if (type == ActionType.NOTHING) {
				renderer.setBoneRotation("hammer", h.isCycled() ? HAMMER_ROT_CYCLED : 0.0f, 0.0f, 0.0f);
			}
			
			for (int i = 0; i < h.getMagazineSize(); ++i) {
				if (reloading && i == pos) continue;
				ItemStack chamber = h.peekAmmo(i);
				String num = Integer.toString(i + 1);
				if (chamber.isEmpty()) {
					renderer.hideBone("cartridge" + num, true);
				} else {
					renderer.hideBone("cartridge" + num, false);
					renderer.hideBone("bullet" + num, chamber.getItem() == ItemInit.CARTRIDGE_CASE.get());
				}
			}
		});
	}
	
	@Override
	protected void interpretFirstPersonInstructions(List<String> tokens, FirearmRenderer renderer) {
		super.interpretFirstPersonInstructions(tokens, renderer);
		String firstTok = tokens.get(0);
		if (tokens.size() < 2) return;
		
		if (firstTok.equals("hide_current_cartridge")) {
			boolean hide = Boolean.valueOf(tokens.get(1));
			ItemStack stack = renderer.getCurrentItem();
			if (stack == null) return;
			getDataHandler(stack).ifPresent(h -> {
				int pos = h.getAmmoPosition() + 1;
				renderer.hideBone("cartridge" + pos, hide);
			});
		} else if (firstTok.equals("hide_current_bullet")) {
			boolean hide = Boolean.valueOf(tokens.get(1));
			ItemStack stack = renderer.getCurrentItem();
			if (stack == null) return;
			getDataHandler(stack).ifPresent(h -> {
				int pos = h.getAmmoPosition() + 1;
				renderer.hideBone("bullet" + pos, hide);
			});
		}
	}

	public static final int ANIM_SELECT_FIREARM = 0;
	public static final int ANIM_SPRINTING = 1;
	public static final int ANIM_HIP_AIMING = 10;
	public static final int ANIM_HIP_FIRING = 11;
	public static final int ANIM_HIP_CYCLING = 12;
	public static final int ANIM_ADS_AIMING = 20;
	public static final int ANIM_ADS_FIRING = 21;
	public static final int ANIM_ADS_CYCLING = 22;
	public static final int ANIM_RELOAD_START = 30;
	public static final int ANIM_RELOAD_SEEK = 31;
	public static final int ANIM_RELOAD_EMPTY = 32;
	public static final int ANIM_RELOAD_EXTRACT = 33;
	public static final int ANIM_RELOAD_END = 34;
	
	@Override
	public void onAnimationSync(int id, int state) {
		AnimationBuilder builder = new AnimationBuilder();
		
		switch (state) {
		case ANIM_SELECT_FIREARM:
			builder
			.addAnimation("select_firearm", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_SPRINTING: builder.addAnimation("sprinting", true); break;
		case ANIM_HIP_AIMING: builder.addAnimation("hip_aiming", true); break;
		case ANIM_HIP_FIRING:
			builder
			.addAnimation("hip_firing", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_HIP_CYCLING:
			builder
			.addAnimation("hip_cycling", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_ADS_AIMING: builder.addAnimation("ads_aiming", true); break;
		case ANIM_ADS_FIRING:
			builder
			.addAnimation("ads_firing", false)
			.addAnimation("ads_aiming", true);
			break;
		case ANIM_ADS_CYCLING:
			builder
			.addAnimation("ads_cycling", false)
			.addAnimation("ads_aiming", true);
			break;
		case ANIM_RELOAD_START:
			builder
			.addAnimation("reload_start", false)
			.addAnimation("reload_hold", true);
			break;
		case ANIM_RELOAD_SEEK:
			builder
			.addAnimation("reload_seek", false)
			.addAnimation("reload_hold", true);
			break;
		case ANIM_RELOAD_EMPTY:
			builder
			.addAnimation("reload_empty", false)
			.addAnimation("reload_hold", true);
			break;
		case ANIM_RELOAD_EXTRACT:
			builder
			.addAnimation("reload_extract", false)
			.addAnimation("reload_hold", true);
			break;
		case ANIM_RELOAD_END:
			builder
			.addAnimation("reload_end", false)
			.addAnimation("hip_aiming", true);
			break;
		}
		
		final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, "controller");
		controller.markNeedsReload();
		controller.setAnimation(builder);
	}
	
	/*
	 * THIRD-PERSON ANIMATION METHODS
	 */

	@Override
	public boolean shouldSpecialRender(ItemStack stack, LivingEntity entity) {
		return entity instanceof AbstractClientPlayerEntity || entity instanceof NPCEntity;
	}
	
	@Override
	public void onPreRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {
		super.onPreRender(entity, animatable, entityYaw, partialTicks, stack, bufferIn, packedLightIn, renderer);
		
		ItemStack item = entity.getMainHandItem();
		getDataHandler(item).ifPresent(h -> {
			ActionType type = h.getAction();
			boolean reloading = type == ActionType.RELOADING;
			
			int pos = h.getAmmoPosition();
			float rot = (float)(pos + (reloading ? 2 : 0)) * -CYLINDER_ROT;
			renderer.addToBoneRotation("cylinder", 0.0f, 0.0f, rot);
			
			if (type == ActionType.NOTHING) {
				renderer.setBoneRotation("hammer", h.isCycled() ? HAMMER_ROT_CYCLED : 0.0f, 0.0f, 0.0f);
			}
			
			for (int i = 0; i < h.getMagazineSize(); ++i) {
				if (reloading && i == pos) continue;
				ItemStack chamber = h.peekAmmo(i);
				String num = Integer.toString(i + 1);
				if (chamber.isEmpty()) {
					renderer.hideBone("cartridge" + num, true);
				} else {
					renderer.hideBone("cartridge" + num, false);
					renderer.hideBone("bullet" + num, chamber.getItem() == ItemInit.CARTRIDGE_CASE.get());
				}
			}
		});
	}

	private static final ResourceLocation ANIM_FILE_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "animations/third_person/colt_saa_t.animation.json");
	@Override
	public ResourceLocation getAnimationFileLocation(ItemStack stack, LivingEntity entity) {
		return ANIM_FILE_LOC;
	}

	private static final ResourceLocation MODEL_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "geo/third_person/colt_saa_t.geo.json");
	@Override
	public ResourceLocation getModelLocation(ItemStack stack, LivingEntity entity) {
		return MODEL_LOC;
	}

	private static final ResourceLocation TEXTURE_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/item/colt_saa.png");
	@Override
	public ResourceLocation getTextureLocation(ItemStack stack, LivingEntity entity) {
		return TEXTURE_LOC;
	}

	@Override
	public AnimationBuilder getDefaultAnimation(ItemStack stack, LivingEntity entity,
			AnimationController<?> controller) {
		return (new AnimationBuilder())
				.addAnimation("select_firearm", false)
				.addAnimation(getDataHandler(stack).map(IFirearmItemDataHandler::shouldDisplaySprinting).orElse(false) ? "sprinting" : "hip_aiming", true);
	}
	
	@Override
	public void interpretThirdPersonInstructions(List<String> tokens, ItemStack stack, ThirdPersonItemAnimRenderer renderer) {
		super.interpretThirdPersonInstructions(tokens, stack, renderer);
		String firstTok = tokens.get(0);
		if (tokens.size() < 2) return;
		
		if (firstTok.equals("hide_current_cartridge")) {
			boolean hide = Boolean.valueOf(tokens.get(1));
			getDataHandler(stack).ifPresent(h -> {
				int pos = h.getAmmoPosition() + 1;
				renderer.hideBone("cartridge" + pos, hide);
			});
		} else if (firstTok.equals("hide_current_bullet")) {
			boolean hide = Boolean.valueOf(tokens.get(1));
			getDataHandler(stack).ifPresent(h -> {
				int pos = h.getAmmoPosition() + 1;
				renderer.hideBone("bullet" + pos, hide);
			});
		}
	}

}
