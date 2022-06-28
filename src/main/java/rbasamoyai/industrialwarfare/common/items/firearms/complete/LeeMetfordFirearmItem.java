package rbasamoyai.industrialwarfare.common.items.firearms.complete;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemData;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.firearms.InternalMagazineFirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.InternalMagazineRifleItem;
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

public class LeeMetfordFirearmItem extends InternalMagazineRifleItem {

	public LeeMetfordFirearmItem() {
		super(new Item.Properties()
							.stacksTo(1)
							.durability(1200)
							.tab(IWItemGroups.TAB_WEAPONS),
					new InternalMagazineFirearmItem.Properties()
							.ammoPredicate(s -> s.getItem() == ItemInit.AMMO_GENERIC.get() || s.getItem() == ItemInit.INFINITE_AMMO_GENERIC.get())
							.baseDamage(19.0f)
							.headshotMultiplier(3.0f)
							.spread(0.1f)
							.hipfireSpread(5.0f)
							.muzzleVelocity(7.5f)
							.horizontalRecoil(e -> 1.0f * (float) e.getRandom().nextGaussian())
							.verticalRecoil(e -> 4.0f + 1.0f * e.getRandom().nextFloat())
							.cooldownTime(20)
							.cycleTime(20)
							.drawTime(10)
							.reloadStartTime(20)
							.reloadTime(20)
							.reloadEndTime(20)
							.projectileRange(80)
							.magazineSize(10));
	}
	
	/*
	 * ANIMATION CONTROL METHODS
	 */
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
		super.shoot(firearm, shooter);
		if (!shooter.level.isClientSide) {
			ServerLevel slevel = (ServerLevel) shooter.level;
			shooter.level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEventInit.RIFLE_FIRED.get(), SoundSource.MASTER, 4.0f, 1.0f);
			
			Vec3 viewVector = shooter.getViewVector(1.0f);
			Vec3 smokePos = shooter.getEyePosition(1.0f).add(viewVector.scale(2.0d));
			Vec3 smokeDelta = viewVector.scale(0.5d);
			int count = 30 + shooter.getRandom().nextInt(31);
			for (ServerPlayer splayer : slevel.getPlayers(p -> true)) {
				slevel.sendParticles(splayer, ParticleTypes.POOF, true, smokePos.x, smokePos.y, smokePos.z, count, smokeDelta.x, smokeDelta.y, smokeDelta.z, 0.02d);
			}
			
			boolean isAiming = isAiming(firearm);
			int fpsAnim = isAiming ? ANIM_ADS_FIRING : ANIM_HIP_FIRING;
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, fpsAnim);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			if (isAiming) {
				upperBody.add(new Tuple<>("ads_firing", false));
				upperBody.add(new Tuple<>("ads_aiming", true));
			} else {
				upperBody.add(new Tuple<>("hip_firing", false));
				upperBody.add(new Tuple<>("hip_aiming", true));
			}
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected void onSelect(ItemStack firearm, LivingEntity shooter) {
		super.onSelect(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnim(firearm, shooter, this, ANIM_SELECT_FIREARM);
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("select_firearm", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
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
				animId = ANIM_PORT_ARMS;
				animStr = "port_arms";
			} else {
				animId = ANIM_HIP_AIMING;
				animStr = "hip_aiming";
			}
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, animId);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", animStr, true, 1.0f);
		}
	}
	
	@Override
	public void startSprinting(ItemStack firearm, LivingEntity shooter) {
		super.startSprinting(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_PORT_ARMS);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", "port_arms", true, 1.0f);
		}
	}
	
	@Override
	public void stopSprinting(ItemStack firearm, LivingEntity shooter) {
		super.stopSprinting(firearm, shooter);
		this.doNothing(firearm, shooter);
	}
	
	@Override
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		if (isAiming(firearm)) return;
		super.startReload(firearm, shooter);
		if (!shooter.level.isClientSide) {
			int animCode = ANIM_RELOAD_START;
			String animName = "reload_start";
			if (getDataHandler(firearm).map(h -> h.isCycled() && h.hasAmmo()).orElse(false)) {
				animCode = ANIM_RELOAD_START_CHAMBERED;
				animName = "reload_start_chambered";
			} else if (isFired(firearm)) {
				animCode = ANIM_RELOAD_START_EXTRACT;
				animName = "reload_start_extract";
			}
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, animCode);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(animName, false));
			upperBody.add(new Tuple<>("reload_hold", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected int getReloadStartTime(ItemStack firearm, LivingEntity shooter) {
		return getDataHandler(firearm).map(h -> {
			 return h.isCycled() && h.hasAmmo() ? 30 : super.getReloadStartTime(firearm, shooter);
		}).orElse(super.getReloadStartTime(firearm, shooter));
	}

	@Override
	protected void midReload(ItemStack firearm, LivingEntity shooter) {
		super.midReload(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("reload", false));
			upperBody.add(new Tuple<>("reload_hold", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected void endReload(ItemStack firearm, LivingEntity shooter) {
		super.endReload(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_RELOAD_END);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("reload_end", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		super.startCycle(firearm, shooter);
		if (!shooter.level.isClientSide) {
			boolean aiming = isAiming(firearm);
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, aiming ? ANIM_ADS_CYCLING : ANIM_HIP_CYCLING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(aiming ? "ads_cycling" : "hip_cycling", false));
			upperBody.add(new Tuple<>(aiming ? "ads_aiming" : "hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected int getCycleTime(ItemStack firearm, LivingEntity shooter) {
		return isAiming(firearm) ? 30 : super.getCycleTime(firearm, shooter);
	}
	
	@Override
	public void startAiming(ItemStack firearm, LivingEntity shooter) {
		super.startAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_ADS_AIMING_START);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("ads_aiming_start", false));
			upperBody.add(new Tuple<>("ads_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);			
		}
	}
	
	@Override
	public void stopAiming(ItemStack firearm, LivingEntity shooter) {
		super.stopAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_ADS_AIMING_STOP);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("ads_aiming_stop", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
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
	public void setupAnimationState(FirearmRenderer renderer, ItemStack stack, PoseStack matrixStack, float aimProgress) {
		if (renderer.getUniqueID(this).intValue() == -1) return;
		getDataHandler(stack).ifPresent(h -> {
			if (h.getAction() == ActionType.NOTHING) {
				renderer.setBonePosition("firing_piece", 0.0f, 0.0f, h.isCycled() ? 0.5f : 0.0f);
			}
		});
	}
	
	public static final int ANIM_PORT_ARMS = 00;
	public static final int ANIM_TRAIL_ARMS = 01;
	public static final int ANIM_SELECT_FIREARM = 02;
	public static final int ANIM_HIP_AIMING = 10;
	public static final int ANIM_HIP_FIRING = 11;
	public static final int ANIM_HIP_CYCLING = 12;
	public static final int ANIM_ADS_AIMING = 20;
	public static final int ANIM_ADS_AIMING_START = 21;
	public static final int ANIM_ADS_AIMING_STOP = 22;
	public static final int ANIM_ADS_FIRING = 23;
	public static final int ANIM_ADS_CYCLING = 24;
	public static final int ANIM_RELOAD_START = 30;
	public static final int ANIM_RELOAD_START_EXTRACT = 301;
	public static final int ANIM_RELOAD_START_CHAMBERED = 302;
	public static final int ANIM_RELOAD = 31;
	public static final int ANIM_RELOAD_END = 32;

	@Override
	public void onAnimationSync(int id, int state) {
		AnimationBuilder builder = new AnimationBuilder();
		final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, "controller");
		
		switch (state) {
		case ANIM_PORT_ARMS: builder.addAnimation("port_arms", true); break;
		case ANIM_TRAIL_ARMS: builder.addAnimation("trail_arms", true); break;
		case ANIM_SELECT_FIREARM:
			builder
			.addAnimation("select_firearm", false)
			.addAnimation("hip_aiming", true);
			break;
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
		case ANIM_ADS_AIMING_START:
			builder
			.addAnimation("ads_aiming_start", false)
			.addAnimation("ads_aiming", true);
			break;
		case ANIM_ADS_AIMING_STOP:
			builder
			.addAnimation("ads_aiming_stop", false)
			.addAnimation("hip_aiming", true);
			break;
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
		case ANIM_RELOAD_START_EXTRACT:
			builder
			.addAnimation("reload_start_extract", false)
			.addAnimation("reload_hold", true);
			break;
		case ANIM_RELOAD_START_CHAMBERED:
			builder
			.addAnimation("reload_start_chambered", false)
			.addAnimation("reload_hold", true);
			break;
		case ANIM_RELOAD:
			builder
			.addAnimation("reload", false)
			.addAnimation("reload_hold", true);
			break;
		case ANIM_RELOAD_END:
			builder
			.addAnimation("reload_end", false)
			.addAnimation("hip_aiming", true);
			break;
		}
		
		controller.markNeedsReload();
		controller.setAnimation(builder);
	}
	
	/*
	 * THIRD-PERSON ANIMATION METHODS
	 */

	@Override
	public boolean shouldSpecialRender(ItemStack stack, LivingEntity entity) {
		return entity instanceof AbstractClientPlayer || entity instanceof NPCEntity;
	}
	
	@Override
	public void onPreRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			PoseStack stack, MultiBufferSource bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {
		super.onPreRender(entity, animatable, entityYaw, partialTicks, stack, bufferIn, packedLightIn, renderer);
		
		ItemStack item = entity.getMainHandItem();
		
		getDataHandler(item).ifPresent(h -> {
			if (h.getAction() == ActionType.NOTHING) {
				renderer.setBonePosition("firing_piece", 0.0f, 0.0f, h.isCycled() ? 0.5f : 0.0f);
			}
		});
	}

	private static final ResourceLocation ANIM_FILE_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "animations/third_person/lee_metford_t.animation.json");
	@Override
	public ResourceLocation getAnimationFileLocation(ItemStack stack, LivingEntity entity) {
		return ANIM_FILE_LOC;
	}

	private static final ResourceLocation MODEL_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "geo/third_person/lee_metford_t.geo.json");
	@Override
	public ResourceLocation getModelLocation(ItemStack stack, LivingEntity entity) {
		return MODEL_LOC;
	}

	@Override
	public AnimationBuilder getDefaultAnimation(ItemStack stack, LivingEntity entity,
			AnimationController<?> controller) {
		return (new AnimationBuilder())
				.addAnimation("select_firearm", false)
				.addAnimation(getDataHandler(stack).map(IFirearmItemData::shouldDisplaySprinting).orElse(false) ? "port_arms" : "hip_aiming", true);
	}

}
