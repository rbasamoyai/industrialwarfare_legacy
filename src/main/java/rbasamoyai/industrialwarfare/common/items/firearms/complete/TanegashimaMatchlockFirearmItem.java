package rbasamoyai.industrialwarfare.common.items.firearms.complete;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemDataHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.MatchCordItem;
import rbasamoyai.industrialwarfare.common.items.firearms.MatchlockFirearmItem;
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

public class TanegashimaMatchlockFirearmItem extends MatchlockFirearmItem {

	public TanegashimaMatchlockFirearmItem() {
		super(new Item.Properties()
							.stacksTo(1)
							.durability(1200)
							.tab(IWItemGroups.TAB_WEAPONS)
							.setISTER(() -> FirearmRenderer::new),
					new MatchlockFirearmItem.Properties()
							.ammoPredicate(s -> s.getItem() == ItemInit.PAPER_CARTRIDGE.get() || s.getItem() == ItemInit.INFINITE_PAPER_CARTRIDGE.get())
							.baseDamage(30.0f)
							.headshotMultiplier(3.0f)
							.spread(1.25f)
							.hipfireSpread(5.0f)
							.muzzleVelocity(6.0f)
							.horizontalRecoil(e -> 1.25f * (float) e.getRandom().nextGaussian())
							.verticalRecoil(e -> 6.0f + 2.0f * e.getRandom().nextFloat())
							.cooldownTime(20)
							.drawTime(20)
							.reloadTime(260)
							.reloadUnreadyTime(275)
							.reloadUnreadyNoCordTime(260)
							.cycleTime(50)
							.primingNoCordTime(60)
							.unprimingTime(40)
							.unprimingNoCordTime(20)
							.projectileRange(75));
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
	
	private static final float PAN_COVER_ROT = (float) Math.toRadians(90.0f);
	private static final float SERPENTINE_ROT = (float) Math.toRadians(-12.5f);
	private static final int FLAG_SNAPPED = 32;
	
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
		if (isFired(firearm)) return;
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
	public void startAiming(ItemStack firearm, LivingEntity shooter) {
		super.startAiming(firearm, shooter);
		if (!shooter.level.isClientSide) {
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_ADS_AIMING);
			
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
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, ANIM_HIP_AIMING);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>("ads_aiming_stop", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f);
		}
	}
	
	@Override
	protected void shoot(ItemStack firearm, LivingEntity shooter) {
  		if (isSubmergedInWater(shooter)) {
  			firearm.setDamageValue(firearm.getDamageValue() - 1);
  			return;
  		}
		super.shoot(firearm, shooter);
		if (!shooter.level.isClientSide) {
			setSnapped(firearm, true);
			
			ServerWorld slevel = (ServerWorld) shooter.level;
			shooter.level.playSound(null, shooter, SoundEventInit.HEAVY_RIFLE_FIRED.get(), SoundCategory.MASTER, 5.0f, 1.0f);
			
			Vector3d viewVector = shooter.getViewVector(1.0f);
			Vector3d smokePos = shooter.getEyePosition(1.0f).add(viewVector.scale(2.0d));
			Vector3d smokeDelta = viewVector.scale(0.3d);
			int count = 50 + random.nextInt(51);
			for (ServerPlayerEntity splayer : slevel.getPlayers(p -> true)) {
				slevel.sendParticles(splayer, ParticleTypes.POOF, true, smokePos.x, smokePos.y, smokePos.z, count, smokeDelta.x, smokeDelta.y, smokeDelta.z, 0.03d);
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
	protected void startReload(ItemStack firearm, LivingEntity shooter) {
		if (isSubmergedInWater(shooter) || isAiming(firearm) || shooter.isVisuallySwimming()) return;
		super.startReload(firearm, shooter);
		if (!shooter.level.isClientSide) {
			int animation;
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			if (!isCycled(firearm)) {
				animation = ANIM_RELOAD;
				upperBody.add(new Tuple<>("reload", false));
			} else if (isFired(firearm)) {
				animation = ANIM_UNREADY_RELOAD;
				upperBody.add(new Tuple<>("unready_reload", false));
			} else {
				animation = ANIM_UNREADY_RELOAD_NO_CORD;
				upperBody.add(new Tuple<>("unready_reload_no_cord", false));
			}
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, animation);
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected void reload(ItemStack firearm, LivingEntity shooter) {
		super.reload(firearm, shooter);
		setSnapped(firearm, false);
	}
	
	@Override
	protected void startCycle(ItemStack firearm, LivingEntity shooter) {
		if (isSubmergedInWater(shooter) || isAiming(firearm) || isCycled(firearm)) return;
		super.startCycle(firearm, shooter);
		if (!shooter.level.isClientSide && shooter.getOffhandItem().getItem() instanceof MatchCordItem && MatchCordItem.isLit(shooter.getOffhandItem())) {
			boolean isCycled = isCycled(firearm);
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, isCycled ? ANIM_READY_FIREARM_NO_CORD : ANIM_READY_FIREARM);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(isCycled ? "ready_firearm_no_cord" : "ready_firearm", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
		}
	}
	
	@Override
	protected void goToPreviousStance(ItemStack firearm, LivingEntity shooter) {
		if (isAiming(firearm) || !isCycled(firearm)) return;
		super.goToPreviousStance(firearm, shooter);
		if (!shooter.level.isClientSide) {
			boolean hasMatch = isFired(firearm);
			AnimBroadcastUtils.syncItemStackAnimToSelf(firearm, shooter, this, hasMatch ? ANIM_UNREADY_FIREARM : ANIM_UNREADY_FIREARM_NO_CORD);
			
			List<Tuple<String, Boolean>> upperBody = new ArrayList<>();
			upperBody.add(new Tuple<>(hasMatch ? "unready_firearm" : "unready_firearm_no_cord", false));
			upperBody.add(new Tuple<>("hip_aiming", true));
			AnimBroadcastUtils.broadcastThirdPersonAnim(firearm, shooter, "upper_body", upperBody, 1.0f / getTimeModifier(shooter));
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
	public void setupAnimationState(FirearmRenderer renderer, ItemStack stack, MatrixStack matrixStack, float aimProgress) {
		if (renderer.getUniqueID(this).intValue() == -1) return;
		
		getDataHandler(stack).ifPresent(h -> {
			if (h.getAction() == ActionType.NOTHING) {
				renderer.setBoneRotation("pan_cover", 0.0f, h.isCycled() ? PAN_COVER_ROT : 0.0f, 0.0f);
				renderer.setBoneRotation("serpentine", isSnapped(stack) ? SERPENTINE_ROT : 0.0f, 0.0f, 0.0f);
				renderer.hideBone("slow_match", !h.isFired());
			}
		});
		float f = 1.0f / MathHelper.lerp(aimProgress, 1.0f, this.fovModifier);
		matrixStack.scale(1.0f, 1.0f, f);
	}

	private static final int ANIM_SELECT_FIREARM = 0;
	private static final int ANIM_PORT_ARMS = 1;
	private static final int ANIM_HIP_AIMING = 10;
	private static final int ANIM_HIP_FIRING = 11;
	private static final int ANIM_ADS_AIMING = 20;
	private static final int ANIM_ADS_FIRING = 21;
	private static final int ANIM_READY_FIREARM = 30;
	private static final int ANIM_READY_FIREARM_NO_CORD = 31;
	private static final int ANIM_UNREADY_FIREARM = 40;
	private static final int ANIM_UNREADY_FIREARM_NO_CORD = 41;
	private static final int ANIM_RELOAD = 50;
	private static final int ANIM_UNREADY_RELOAD = 51;
	private static final int ANIM_UNREADY_RELOAD_NO_CORD = 52;
	
	@Override
	public void onAnimationSync(int id, int state) {
		AnimationBuilder builder = new AnimationBuilder();
		switch (state) {
		case ANIM_SELECT_FIREARM:
			builder
			.addAnimation("select_firearm", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_PORT_ARMS: builder.addAnimation("port_arms", true); break;
		case ANIM_HIP_AIMING: builder.addAnimation("hip_aiming", true); break;
		case ANIM_HIP_FIRING:
			builder
			.addAnimation("hip_firing", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_ADS_AIMING: builder.addAnimation("ads_aiming", true); break;
		case ANIM_ADS_FIRING:
			builder
			.addAnimation("ads_firing", false)
			.addAnimation("ads_aiming", true);
			break;
		case ANIM_READY_FIREARM:
			builder
			.addAnimation("ready_firearm", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_READY_FIREARM_NO_CORD:
			builder
			.addAnimation("ready_firearm_no_cord", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_UNREADY_FIREARM:
			builder
			.addAnimation("unready_firearm", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_UNREADY_FIREARM_NO_CORD:
			builder
			.addAnimation("unready_firearm_no_cord", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_RELOAD:
			builder
			.addAnimation("reload", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_UNREADY_RELOAD:
			builder
			.addAnimation("unready_reload", false)
			.addAnimation("hip_aiming", true);
			break;
		case ANIM_UNREADY_RELOAD_NO_CORD:
			builder
			.addAnimation("unready_reload_no_cord", false)
			.addAnimation("hip_aiming", true);
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
			if (h.getAction() == ActionType.NOTHING) {
				renderer.setBoneRotation("pan_cover", 0.0f, h.isCycled() ? PAN_COVER_ROT : 0.0f, 0.0f);
				renderer.setBoneRotation("serpentine", isSnapped(item) ? SERPENTINE_ROT : 0.0f, 0.0f, 0.0f);
				renderer.hideBone("slow_match", !h.isFired());
			}
		});
	}

	private static final ResourceLocation ANIM_FILE_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "animations/third_person/tanegashima_matchlock_t.animation.json");
	@Override
	public ResourceLocation getAnimationFileLocation(ItemStack stack, LivingEntity entity) {
		return ANIM_FILE_LOC;
	}

	private static final ResourceLocation MODEL_LOC = new ResourceLocation(IndustrialWarfare.MOD_ID, "geo/third_person/tanegashima_matchlock_t.geo.json");
	@Override
	public ResourceLocation getModelLocation(ItemStack stack, LivingEntity entity) {
		return MODEL_LOC;
	}

	@Override
	public AnimationBuilder getDefaultAnimation(ItemStack stack, LivingEntity entity, AnimationController<?> controller) {
		return (new AnimationBuilder())
				.addAnimation("select_firearm", false)
				.addAnimation("hip_aiming", true);
	}
	
	public static void setSnapped(ItemStack stack, boolean snapped) {
		getDataHandler(stack).ifPresent(h -> {
			h.setState(snapped ? h.getState() | FLAG_SNAPPED : h.getState() & (0xFFFFFFFF ^ FLAG_SNAPPED));
		});
	}
	
	public static boolean isSnapped(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::getState).map(h -> (h & FLAG_SNAPPED) == FLAG_SNAPPED).orElse(false);
	}

}
