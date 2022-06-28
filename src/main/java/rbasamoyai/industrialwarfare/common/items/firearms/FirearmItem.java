package rbasamoyai.industrialwarfare.common.items.firearms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Constants;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.client.entities.renderers.ThirdPersonItemAnimRenderer;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.SpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.FirearmItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.IPartItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.IQualityItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataHandler;
import rbasamoyai.industrialwarfare.common.entities.IQualityModifier;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import rbasamoyai.industrialwarfare.common.items.FirstPersonTransform;
import rbasamoyai.industrialwarfare.common.items.FovModifierItem;
import rbasamoyai.industrialwarfare.common.items.HideCrosshair;
import rbasamoyai.industrialwarfare.common.items.ISimultaneousUseAndAttack;
import rbasamoyai.industrialwarfare.common.items.ItemWithScreen;
import rbasamoyai.industrialwarfare.common.items.PartItem;
import rbasamoyai.industrialwarfare.common.items.QualityItem;
import rbasamoyai.industrialwarfare.utils.AnimUtils;
import rbasamoyai.industrialwarfare.utils.IWMiscUtils;
import rbasamoyai.industrialwarfare.utils.TextureUtils;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.ParticleKeyFrameEvent;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;
import software.bernie.geckolib3.util.RenderUtils;

public abstract class FirearmItem extends ProjectileWeaponItem implements
		ISimultaneousUseAndAttack,
		FovModifierItem,
		HideCrosshair,
		ItemWithScreen,
		FirstPersonTransform,
		IAnimatable,
		ISyncable,
		SpecialThirdPersonRender {
	
	protected final boolean needsCycle;
	protected final int cooldownTime;
	protected final int cycleTime;
	protected final int drawTime;
	protected final int meleeToggleTime;
	protected final int projectileRange;
	protected final int reloadEndTime;
	protected final int reloadStartTime;
	protected final int reloadTime;
	protected final float baseDamage;
	protected final float headshotMultiplier;
	protected final float hipfireSpread;
	protected final float muzzleVelocity;
	protected final float spread;
	protected final float fovModifier;
	protected final Function<LivingEntity, Float> horizontalRecoilSupplier;
	protected final Function<LivingEntity, Float> verticalRecoilSupplier;
	protected final Predicate<ItemStack> ammoPredicate;
	protected final Function<IItemHandlerModifiable, IFirearmItemData> dataHandlerSupplier;
	protected final Supplier<IItemHandlerModifiable> attachmentsHandler;
	
	public AnimationFactory factory = new AnimationFactory(this);
	
	public FirearmItem(Item.Properties itemProperties, FirearmItem.AbstractProperties<?> firearmProperties,
			Function<IItemHandlerModifiable, IFirearmItemData> dataHandlerSupplier) {
		super(itemProperties);
		
		this.needsCycle = firearmProperties.needsCycle;
		this.cooldownTime = firearmProperties.cooldownTime;
		this.cycleTime = firearmProperties.cycleTime;
		this.drawTime = firearmProperties.drawTime;
		this.meleeToggleTime = firearmProperties.meleeToggleTime;
		this.projectileRange = firearmProperties.projectileRange;
		this.reloadEndTime = firearmProperties.reloadEndTime;
		this.reloadStartTime = firearmProperties.reloadStartTime;
		this.reloadTime = firearmProperties.reloadTime;
		this.baseDamage = firearmProperties.baseDamage;
		this.headshotMultiplier = firearmProperties.headshotMultiplier;
		this.hipfireSpread = firearmProperties.hipfireSpread;
		this.muzzleVelocity = firearmProperties.muzzleVelocity;
		this.spread = firearmProperties.spread;
		this.fovModifier = firearmProperties.fovModifier;
		this.horizontalRecoilSupplier = firearmProperties.horizontalRecoilSupplier;
		this.verticalRecoilSupplier = firearmProperties.verticalRecoilSupplier;
		this.ammoPredicate = firearmProperties.ammoPredicate;
		this.attachmentsHandler = firearmProperties.attachmentsHandler;
		this.dataHandlerSupplier = dataHandlerSupplier;
		
		GeckoLibNetwork.registerSyncable(this);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		BundledProvider provider = this.new BundledProvider();
		provider.getCapability(FirearmItemCapability.INSTANCE).ifPresent(h -> {
			h.setAction(ActionType.NOTHING, this.drawTime);
			h.setAiming(false);
		});
		provider.getCapability(QualityItemCapability.INSTANCE).ifPresent(h -> {
			h.setQuality(1.0f);
		});
		provider.getCapability(PartItemCapability.INSTANCE).ifPresent(h -> {
			h.setPartCount(1);
			h.setWeight(1.0f);
		});
		if (nbt != null) {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		
		return provider;
	}
	
	private class BundledProvider implements ICapabilitySerializable<CompoundTag> {
		private final IFirearmItemData firearmDataInterface = FirearmItem.this.dataHandlerSupplier.apply(FirearmItem.this.attachmentsHandler.get());
		private final IPartItemData partDataInterface = new PartItemDataHandler();
		private final IQualityItemData qualityDataInterface = new QualityItemDataHandler();
		private final LazyOptional<IFirearmItemData> firearmDataOptional = LazyOptional.of(() -> this.firearmDataInterface);
		private final LazyOptional<IPartItemData> partDataOptional = LazyOptional.of(() -> this.partDataInterface);
		private final LazyOptional<IQualityItemData> qualityDataOptional = LazyOptional.of(() -> this.qualityDataInterface);
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (cap == FirearmItemCapability.INSTANCE) {
				return this.firearmDataOptional.cast();
			}
			if (cap == QualityItemCapability.INSTANCE) {
				return this.qualityDataOptional.cast();
			}
			return cap == PartItemCapability.INSTANCE ? this.partDataOptional.cast() : LazyOptional.empty();
		}
		
		@Override
		public CompoundTag serializeNBT() {
			CompoundTag tag = new CompoundTag();
			if (FirearmItemCapability.INSTANCE.isRegistered()) {
				this.firearmDataInterface.writeTag(tag);
			}
			if (QualityItemCapability.INSTANCE.isRegistered()) {
				this.qualityDataInterface.writeTag(tag);
			}
			if (PartItemCapability.INSTANCE.isRegistered()) {
				this.partDataInterface.writeTag(tag);
			}
			return tag;
		}
		
		@Override
		public void deserializeNBT(CompoundTag nbt) {
			if (FirearmItemCapability.INSTANCE.isRegistered()) {
				this.firearmDataInterface.readTag(nbt);
			}
			if (QualityItemCapability.INSTANCE.isRegistered()) {
				this.qualityDataInterface.readTag(nbt);
			}
			if (PartItemCapability.INSTANCE.isRegistered()) {
				this.partDataInterface.readTag(nbt);
			}
		}
	}
		
	public static LazyOptional<IFirearmItemData> getDataHandler(ItemStack stack) {
		return stack.getCapability(FirearmItemCapability.INSTANCE);
	}
	
	@Override
	public CompoundTag getShareTag(ItemStack stack) {
		CompoundTag itemCap = new CompoundTag();
		getDataHandler(stack).ifPresent(h -> h.writeTag(itemCap));
		PartItem.getDataHandler(stack).ifPresent(h -> h.writeTag(itemCap));
		QualityItem.getDataHandler(stack).ifPresent(h -> h.writeTag(itemCap));
		CompoundTag tag = stack.getOrCreateTag();
		tag.put("item_cap", itemCap);
		return tag;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundTag nbt) {
		stack.setTag(nbt);
		
		if (nbt == null) return;
		
		if (nbt.contains("creativeData", Tag.TAG_COMPOUND)) {
			readCreativeData(stack, nbt.getCompound("creativeData"));
			nbt.remove("creativeData");
			return;
		}
		
		CompoundTag itemCap = nbt.getCompound("item_cap");
		getDataHandler(stack).ifPresent(h -> h.readTag(itemCap));
		PartItem.getDataHandler(stack).ifPresent(h -> h.readTag(itemCap));
		QualityItem.getDataHandler(stack).ifPresent(h -> h.readTag(itemCap));
	}
	
	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return this.ammoPredicate;
	}

	@Override
	public int getDefaultProjectileRange() {
		return this.projectileRange;
	}
	
	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity.level.isClientSide) return true;
		if (entity instanceof Player) {
			AbstractContainerMenu ct = ((Player) entity).containerMenu;
			if (ct != null && !(ct instanceof InventoryMenu)) return true;
		}
		
		getDataHandler(stack).ifPresent(h -> {
			if (!h.isFinishedAction() || h.isMeleeing()) return;
			
			ItemStack ammo = entity.getProjectile(stack);
			
			if (h.hasAmmo() && this.canShoot(stack, entity)) {
				this.shoot(stack, entity);
				stack.hurtAndBreak(1, entity, e -> {
					e.broadcastBreakEvent(entity.swingingArm);
				});
			} else if (this.needsCycle && !h.isCycled()) {
				this.startCycle(stack, entity);
			} else if (!ammo.isEmpty() && this.getAllSupportedProjectiles().test(ammo) && !h.isFull() && !h.isAiming()) {
				this.startReload(stack, entity);
			}
		});
		return true;
	}
	
	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		return stack.getMaxDamage() - stack.getDamageValue() > 1 ? super.damageItem(stack, amount, entity, onBroken) : 0;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
		if (getDataHandler(stack).map(h -> {
			return !h.isFinishedAction() || h.isMeleeing();
		}).orElse(false)) {
			return InteractionResultHolder.fail(stack);
		}
		player.startUsingItem(hand);
		this.startAiming(stack, player);
		return level.isClientSide ? InteractionResultHolder.pass(stack) : InteractionResultHolder.consume(stack);
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}
	
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}
	
	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int time) {
		this.stopAiming(stack, entity);
	}
	
	protected abstract void shoot(ItemStack firearm, LivingEntity shooter);
	
	protected boolean canShoot(ItemStack firearm, LivingEntity shooter) {
		return !this.needsCycle || isCycled(firearm);
	}
	
	protected abstract void startCycle(ItemStack firearm, LivingEntity shooter);
	
	protected int getCycleTime(ItemStack firearm, LivingEntity shooter) {
		return this.cycleTime;
	}
	
	protected abstract void startReload(ItemStack firearm, LivingEntity shooter);
	
	public void startAiming(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAiming(true);
		});
		shooter.setSprinting(false);
	}
	
	public void stopAiming(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setAiming(false);
		});
	}
	
	public void startSprinting(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setDisplaySprinting(true);
		});
	}
	
	public void stopSprinting(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			h.setDisplaySprinting(false);
		});
	}
	
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof LivingEntity)) return;
		LivingEntity shooter = (LivingEntity) entity;
		
		if (level.isClientSide) return;
		GeckoLibUtil.guaranteeIDForStack(stack, (ServerLevel) level);
		
		getDataHandler(stack).ifPresent(h -> {
			if (!h.isSelected() && selected) {
				this.onSelect(stack, shooter);
			}
			h.setSelected(selected);
			
			if (!selected) {
				// No time modified as an IQualityModifier entity could return a different time modifier each call
				h.setAction(ActionType.NOTHING, this.drawTime);
				h.setAiming(false);
				h.setMelee(false);
				h.setRecoilTicks(0);
				h.setRecoil(0.0f, 0.0f);
				return;
			}
			
			h.tickRecoil();
			
			if (h.getAction() == ActionType.NOTHING) {
				boolean sprinting = entity.isSprinting();
				if (h.shouldDisplaySprinting() && !sprinting) {
					this.stopSprinting(stack, shooter);
				} else if (!h.shouldDisplaySprinting() && sprinting) {
					this.startSprinting(stack, shooter);
				}
			}
			
			if (!h.isFinishedAction()) {
				h.countdownAction();
				if (h.isFinishedAction()) this.onActionComplete(stack, shooter);
			}
		});
	}
	
	protected void onActionComplete(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			switch (h.getAction()) {
			case NOTHING: this.doNothing(firearm, shooter); break;
			case CYCLING: this.endCycle(firearm, shooter); break;
			case RELOADING: this.reload(firearm, shooter); break;
			case START_RELOADING: this.actuallyStartReloading(firearm, shooter); break;
			case TOGGLE_MELEE: this.endToggleMelee(firearm, shooter); break;
			case PREVIOUS_STANCE: this.actuallyDoPreviousStance(firearm, shooter); break;
			}
		});
	}
	
	public boolean needsCycle(ItemStack stack) { return this.needsCycle; }
	
	protected abstract void endCycle(ItemStack firearm, LivingEntity shooter);
	
	protected abstract void reload(ItemStack firearm, LivingEntity shooter);
	
	/** For starting repeated reload animations (e.g. tube loading) */
	protected void actuallyStartReloading(ItemStack firearm, LivingEntity shooter) {}
	
	protected void doNothing(ItemStack firearm, LivingEntity shooter) {}
	
	protected void onSelect(ItemStack firearm, LivingEntity shooter) {}
	
	protected void goToPreviousStance(ItemStack firearm, LivingEntity shooter) {}
	
	protected void actuallyDoPreviousStance(ItemStack firearm, LivingEntity shooter) {}
	
	public static void tryReloadFirearm(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		Item item = firearm.getItem();
		if (!(item instanceof FirearmItem)) return;
		FirearmItem firearmItem = (FirearmItem) item;
		
		ItemStack ammo = shooter.getProjectile(firearm);
		if (ammo.isEmpty() || !firearmItem.getAllSupportedProjectiles().test(ammo)) return;
		getDataHandler(firearm).ifPresent(h -> {
			if (h.isFinishedAction() && !h.isFull()) firearmItem.startReload(firearm, shooter);
		});
	}
	
	public static void resetSelected(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		getDataHandler(firearm).ifPresent(h -> {
			h.setSelected(false);
		});
	}
	
	public static void tryPreviousStance(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		Item item = firearm.getItem(); 
		if (!(item instanceof FirearmItem)) return;
		((FirearmItem) item).goToPreviousStance(firearm, shooter);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		QualityItem.appendHoverTextStatic(stack, world, tooltip, flag);
		PartItem.appendHoverTextStatic(stack, world, tooltip, flag);
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}
	
	@Override
	public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
		return false; // TODO: maybe trowel melee?
	}
	
	protected boolean canMelee(ItemStack firearm, LivingEntity shooter) {
		return false;
	}
	
	protected void startToggleMelee(ItemStack firearm, LivingEntity shooter) {}
	
	protected void endToggleMelee(ItemStack firearm, LivingEntity shooter) {}
	
	public final void trySettingMelee(ItemStack firearm, LivingEntity shooter) {
		getDataHandler(firearm).ifPresent(h -> {
			if (this.canMelee(firearm, shooter)) {
				h.setAction(ActionType.TOGGLE_MELEE, getTimeModifiedByEntity(shooter, this.meleeToggleTime));
			}
		});
	}
	
	@Override
	public float getFovModifier(ItemStack stack) {
		return isAiming(stack) ? this.fovModifier : 1.0f;
	}
	
	@Override
	public boolean shouldTransform(ItemStack stack, Player player) {
		return true;
	}
	
	@Override
	public void transformPoseStack(ItemStack itemStack, Player player, PoseStack poseStack) {
		poseStack.scale(1.0f, 1.0f, isAiming(itemStack) ? 0.5f : 1.0f);
	}
	
	/*
	 * ANIMATION METHODS
	 */
	
	protected <P extends IAnimatable> void soundListener(SoundKeyframeEvent<P> event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(event.sound));
		mc.player.playSound(sound, 16.0f, 1.0f);
	}
	
	protected <P extends IAnimatable> void customInstructionListener(CustomInstructionKeyframeEvent<P> event) {
		List<String> instructions = Arrays.stream(event.instructions.split(";")).filter(s -> s.length() > 0).collect(Collectors.toList());
		List<List<String>> instructionTokens =
				instructions
				.stream()
				.map(s -> Arrays.asList(s.split(" ")).stream().filter(tk -> tk.length() > 0).collect(Collectors.toList()))
				.filter(tks -> !tks.isEmpty())
				.collect(Collectors.toList());
		
		if (instructionTokens.isEmpty()) return;
		
		BlockEntityWithoutLevelRenderer ister = RenderProperties.get(this).getItemStackRenderer();
		if (!(ister instanceof FirearmRenderer)) return;
		FirearmRenderer renderer = (FirearmRenderer) ister;
		
		instructionTokens
		.stream()
		.filter(tks -> !tks.isEmpty())
		.forEach(tks -> this.interpretFirstPersonInstructions(tks, renderer));
	}
	
	protected void interpretFirstPersonInstructions(List<String> tokens, FirearmRenderer renderer) {
		String firstTok = tokens.get(0);
		if (tokens.size() < 2) return;
		
		String boneName = tokens.get(1);
		
		if (firstTok.equals("set_hidden")) {
			boolean hidden = Boolean.valueOf(tokens.get(2));
			renderer.hideBone(boneName, hidden);
		} else if (firstTok.equals("move")) {
			float x = Float.valueOf(tokens.get(2));
			float y = Float.valueOf(tokens.get(3));
			float z = Float.valueOf(tokens.get(4));
			renderer.setBonePosition(boneName, x, y, z);
		} else if (firstTok.equals("rotate")) {
			float x = Float.valueOf(tokens.get(2));
			float y = Float.valueOf(tokens.get(3));
			float z = Float.valueOf(tokens.get(4));
			renderer.setBoneRotation(boneName, x, y, z);
		} else if (firstTok.equals("suppress_mod")) {
			renderer.suppressModification(boneName);
		} else if (firstTok.equals("allow_mod")) {
			renderer.allowModification(boneName);
		}
	}
	
	protected <P extends IAnimatable> void particleListener(ParticleKeyFrameEvent<P> event) {
	}
	
	protected <P extends IAnimatable> void thirdPersonSoundListener(SoundKeyframeEvent<P> event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) event.getEntity();
		LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
		if (entity == null) return;
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(event.sound));
		Vec3 entityPos = entity.position();
		float volume = mc.player == entity ? 16.0f : 1.0f;
		mc.player.level.playLocalSound(entityPos.x, entityPos.y, entityPos.z, sound, SoundSource.MASTER, volume, 1.0f, true);
	}

	@Override
	public AnimationFactory getFactory() {
		return this.factory;
	}
	
	public void setupAnimationState(FirearmRenderer renderer, ItemStack stack, PoseStack matrixStack, float aimProgress) {}
	
	/*
	 * THIRD-PERSON RENDERING METHODS
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onPreRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			PoseStack stack, MultiBufferSource bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {
		Minecraft mc = Minecraft.getInstance();
		
		LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> baseRenderer =
				(LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) mc.getEntityRenderDispatcher().getRenderer(entity);
		EntityModel<?> model = baseRenderer.getModel();
		ResourceLocation loc = baseRenderer.getTextureLocation(entity);
		int packedOverlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0f);
		
		float bob = (float) entity.tickCount + partialTicks;
		
		boolean falling = entity.getFallFlyingTicks() > 4;
		float animSpeed = Mth.lerp(partialTicks, entity.animationSpeedOld, entity.animationSpeed);
		if (animSpeed > 1.0f) animSpeed = 1.0f;
		float animPos = entity.animationPosition - entity.animationSpeed * (1.0f - partialTicks);
		if (entity.isBaby()) animPos *= 3.0f;
		
		boolean isStill = entity.getDeltaMovement().lengthSqr() < 0.00625d;
		boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
		float headYaw;
		float undoYaw;
		if (shouldSit && entity.getVehicle() instanceof LivingEntity) {
			LivingEntity livingVehicle = (LivingEntity) entity.getVehicle();
			headYaw = Mth.rotLerp(partialTicks, livingVehicle.yBodyRotO, livingVehicle.yBodyRot);
			undoYaw = headYaw;
		} else {
			headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
			undoYaw = headYaw + entityYaw;
		}
		
		stack.mulPose(Vector3f.YN.rotationDegrees(headYaw));
		
		if (model instanceof HumanoidModel) {
			HumanoidModel<?> bmodel = (HumanoidModel<?>) model;
			bmodel.riding = shouldSit;
			
			float fallRestriction = 1.0f;
			if (falling) {
				fallRestriction = (float) entity.getDeltaMovement().lengthSqr() / 0.2f;
				fallRestriction = fallRestriction * fallRestriction * fallRestriction;
			}
			if (fallRestriction < 1.0f) fallRestriction = 1.0f;
			
			bmodel.leftLeg.y = 12.0f;
			bmodel.leftLeg.xRot = Mth.cos(animPos * 0.6662f + (float) Math.PI) * 1.4f * animSpeed / fallRestriction;
			bmodel.leftLeg.yRot = 0.0f;
			bmodel.leftLeg.zRot = 0.0f;
			
			bmodel.rightLeg.y = 12.0f;
			bmodel.rightLeg.xRot = Mth.cos(animPos * 0.6662f) * 1.4f * animSpeed / fallRestriction;
			bmodel.rightLeg.yRot = 0.0f;
			bmodel.rightLeg.zRot = 0.0f;
			
			if (shouldSit) {
				bmodel.rightLeg.xRot = -1.4137167f;
				bmodel.rightLeg.yRot = Constants.PI * 0.1f;
				bmodel.rightLeg.zRot = 0.07853982f;
				bmodel.leftLeg.xRot = -1.4137167f;
				bmodel.leftLeg.yRot = -Constants.PI * 0.1f;
				bmodel.leftLeg.zRot = -0.07853982f;
			}
			
			if (entity.isCrouching() && isStill) {
				bmodel.leftLeg.z = -4.0f;
				bmodel.leftLeg.xRot = 0.0f;
				
				bmodel.rightLeg.z = 0.0f;
				bmodel.rightLeg.xRot = 0.5f;
			}
			
			if (entity.isFallFlying()) {
				float ffTicks = (float) entity.getFallFlyingTicks() + partialTicks;
				float progress = Mth.clamp(ffTicks * ffTicks * 0.01f, 0.0f, 1.0f);
				
				if (!entity.isAutoSpinAttack()) {
					stack.mulPose(Vector3f.XN.rotationDegrees(progress * (-90.0f - entity.getXRot())));
				}
				
				Vec3 view = entity.getViewVector(partialTicks);
				Vec3 vel = entity.getDeltaMovement();
				double viewHDSq = view.horizontalDistanceSqr();
				double velHDSq = vel.horizontalDistanceSqr();
				if (viewHDSq > 0.0f && velHDSq > 0.0f) {
					double dist = (vel.x * view.x + vel.z * view.z) / Math.sqrt(viewHDSq * velHDSq);
					double cross = vel.x * view.z - vel.z * view.x;
					stack.mulPose(Vector3f.YP.rotation((float)(Math.signum(cross) * Math.acos(dist))));
				}
				
			} else if (entity.getSwimAmount(partialTicks) > 0.0f) {
				bmodel.leftLeg.xRot = Mth.lerp(bmodel.swimAmount, bmodel.leftLeg.xRot, 0.3F * Mth.cos(animPos * 0.33333334F + (float)Math.PI));
				bmodel.rightLeg.xRot = Mth.lerp(bmodel.swimAmount, bmodel.rightLeg.xRot, 0.3F * Mth.cos(animPos * 0.33333334F));
				float swimRot = entity.isInWater() ? -90.0f - entity.getXRot() : -90.0f;
				float swimRotL = Mth.lerp(bmodel.swimAmount, 0.0f, swimRot);
				
				if (entity.isVisuallySwimming()) stack.translate(0.0f, entity.isInWater() ? 0.3125f : 0.125f, 0.0f);
				
				stack.mulPose(Vector3f.XN.rotationDegrees(swimRotL));
				
				if (entity.isVisuallySwimming()) stack.translate(0.0f, -1.0f, 0.0f);
			}
			
			stack.scale(0.9375f, 0.9375f, 0.9375f);
			
			stack.pushPose();
			
			List<HumanoidArmorLayer> armorLayers = AnimUtils.getLayers(HumanoidArmorLayer.class, baseRenderer);
			HumanoidArmorLayer armor = armorLayers.isEmpty() ? null : armorLayers.get(0);
			
			if (entity.deathTime > 0) {
				float deathRot = ((float) entity.deathTime + partialTicks - 1.0f) * 0.08f; // / 20.0f * 1.6f
				deathRot = Mth.sqrt(deathRot);
				if (deathRot > 1.0f) deathRot = 1.0f;
				stack.mulPose(Vector3f.ZN.rotationDegrees(deathRot * 90.0f));				
			}
			
			stack.scale(1.0f, -1.0f, -1.0f);
			stack.translate(0.0d, (double) -1.501f, 0.0d);
			
			this.renderLegs(entity, stack, bufferIn, bmodel, armor, partialTicks, packedLightIn, packedOverlay, loc);
			
			if (entity instanceof AbstractClientPlayer) {
				AbstractClientPlayer client = (AbstractClientPlayer) entity;
				List<CapeLayer> capes = AnimUtils.getLayers(CapeLayer.class, baseRenderer);
				for (CapeLayer layer : capes) {
					layer.render(stack, bufferIn, packedLightIn, client, -1.0f, -1.0f, partialTicks, bob, -1.0f, -1.0f);
				}
			}
			
			List<ElytraLayer> elytras = AnimUtils.getLayers(ElytraLayer.class, baseRenderer);
			for (ElytraLayer layer : elytras) {
				layer.render(stack, bufferIn, packedLightIn, entity, -1.0f, -1.0f, partialTicks, bob, -1.0f, -1.0f);
			}
			
			
			stack.popPose();
			
			if (entity.isCrouching()) {
				stack.translate(0.0f, -0.25f, 0.0f);
			}
			
			stack.translate(0.0f, -0.01f, 0.0f);
			
			bmodel.setAllVisible(false);
		}
		
		stack.mulPose(Vector3f.YP.rotationDegrees(undoYaw));
	}
	
	@SuppressWarnings("rawtypes")
	protected void renderLegs(LivingEntity entity, PoseStack stack, MultiBufferSource bufferIn, HumanoidModel<?> bmodel,
			HumanoidArmorLayer armor, float partialTicks, int packedLightIn, int packedOverlayIn, ResourceLocation loc) {
		Minecraft mc = Minecraft.getInstance();
		
		List<ModelPart> legs = new ArrayList<>();
		legs.add(bmodel.leftLeg);
		legs.add(bmodel.rightLeg);
		
		if (bmodel instanceof PlayerModel) {
			PlayerModel<?> pmodel = (PlayerModel<?>) bmodel;
			pmodel.leftPants.copyFrom(pmodel.leftLeg);
			pmodel.rightPants.copyFrom(pmodel.rightLeg);
			legs.add(pmodel.leftPants);
			legs.add(pmodel.rightPants);
		}
		
		float skinAlpha = entity.isInvisible() ? entity == mc.player ? 0.15f : 0.0f : 1.0f;
		
		VertexConsumer builder = bufferIn.getBuffer(RenderType.entityTranslucent(loc));
		
		legs.forEach(p -> p.render(stack, builder, packedLightIn, packedOverlayIn, 1.0f, 1.0f, 1.0f, skinAlpha));
		
		if (armor != null) {
			armor.innerModel.setAllVisible(true);
			armor.outerModel.setAllVisible(true);
			
			ItemStack leggings = entity.getItemBySlot(EquipmentSlot.LEGS);
			Item leggingsItem = leggings.getItem();
			if (leggingsItem instanceof ArmorItem) {
				ResourceLocation armorLoc = armor.getArmorResource(entity, leggings, EquipmentSlot.LEGS, null);
				VertexConsumer armorBuilder = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(armorLoc), false, leggings.hasFoil());
				
				armor.innerModel.leftLeg.copyFrom(bmodel.leftLeg);
				armor.innerModel.rightLeg.copyFrom(bmodel.rightLeg);
				
				List<ModelPart> parts = new ArrayList<>();
				parts.add(armor.innerModel.leftLeg);
				parts.add(armor.innerModel.rightLeg);
				
				if (leggingsItem instanceof DyeableArmorItem) {
					int color = ((DyeableArmorItem) leggingsItem).getColor(leggings);
					float r = (float)(color >> 16 & 255) / 255.0F;
					float g = (float)(color >> 8 & 255) / 255.0F;
					float b = (float)(color & 255) / 255.0F;
					
					parts.forEach(p -> p.render(stack, armorBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f));
					
					ResourceLocation overlayLoc = armor.getArmorResource(entity, leggings, EquipmentSlot.LEGS, "overlay");
					VertexConsumer overlayBuilder = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(overlayLoc), false, leggings.hasFoil());
					
					parts.forEach(p -> p.render(stack, overlayBuilder, packedLightIn, OverlayTexture.NO_OVERLAY));
				} else {
					parts.forEach(p -> p.render(stack, armorBuilder, packedLightIn, OverlayTexture.NO_OVERLAY));
				}
			}
			
			ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
			Item bootsItem = boots.getItem();
			if (bootsItem instanceof ArmorItem) {
				ResourceLocation armorLoc = armor.getArmorResource(entity, boots, EquipmentSlot.FEET, null);
				VertexConsumer armorBuilder = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(armorLoc), false, boots.hasFoil());
				
				armor.outerModel.leftLeg.copyFrom(bmodel.leftLeg);
				armor.outerModel.rightLeg.copyFrom(bmodel.rightLeg);
				
				List<ModelPart> parts = new ArrayList<>();
				parts.add(armor.outerModel.leftLeg);
				parts.add(armor.outerModel.rightLeg);
				
				if (bootsItem instanceof DyeableArmorItem) {
					int color = ((DyeableArmorItem) bootsItem).getColor(boots);
					float r = (float)(color >> 16 & 255) / 255.0F;
					float g = (float)(color >> 8 & 255) / 255.0F;
					float b = (float)(color & 255) / 255.0F;
					
					parts.forEach(p -> p.render(stack, armorBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f));
					
					ResourceLocation overlayLoc = armor.getArmorResource(entity, boots, EquipmentSlot.FEET, "overlay");
					VertexConsumer overlayBuilder = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(overlayLoc), false, boots.hasFoil());
					
					parts.forEach(p -> p.render(stack, overlayBuilder, packedLightIn, OverlayTexture.NO_OVERLAY));
				} else {
					parts.forEach(p -> p.render(stack, armorBuilder, packedLightIn, OverlayTexture.NO_OVERLAY));
				}
			}
		}
	}
	
	@Override
	public void onJustAfterRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			PoseStack stack, MultiBufferSource bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {
		Minecraft mc = Minecraft.getInstance();
		
		@SuppressWarnings("unchecked")
		LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> baseRenderer =
				(LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) mc.getEntityRenderDispatcher().getRenderer(entity);
		
		AnimUtils.hideLayers(CapeLayer.class, baseRenderer);
		AnimUtils.hideLayers(ElytraLayer.class, baseRenderer);
		AnimUtils.hideLayers(PlayerItemInHandLayer.class, baseRenderer);
		AnimUtils.hideLayers(HumanoidArmorLayer.class, baseRenderer);
	}

	@Override
	public void onPostRender(LivingEntity entity, IAnimatable animatable, float entityYaw, float partialTicks,
			PoseStack stack, MultiBufferSource bufferIn, int packedLightIn, ThirdPersonItemAnimRenderer renderer) {
		Minecraft mc = Minecraft.getInstance();
		@SuppressWarnings("unchecked")
		LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> baseRenderer =
				(LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) mc.getEntityRenderDispatcher().getRenderer(entity);
		EntityModel<?> model = baseRenderer.getModel();
		
		if (model instanceof PlayerModel) {
			PlayerModel<?> pmodel = (PlayerModel<?>) model;
			pmodel.setAllVisible(true);
		}
		
		AnimUtils.restoreLayers(baseRenderer);
	}
	
	@Override
	public List<AnimationController<ThirdPersonItemAnimEntity>> getAnimationControlllers(ItemStack stack,
			LivingEntity entity) {
		AnimationController<ThirdPersonItemAnimEntity> upperBody = new AnimationController<>(
				new ThirdPersonItemAnimEntity(entity.getUUID(), InteractionHand.MAIN_HAND), "upper_body", 1,
				this::upperBodyPredicate);
		upperBody.registerSoundListener(this::thirdPersonSoundListener);
		upperBody.registerCustomInstructionListener(this::thirdPersonCustomInstructionListener);
		upperBody.registerParticleListener(this::particleListener);
		upperBody.setAnimation(this.getDefaultAnimation(stack, entity, upperBody));
		
		List<AnimationController<ThirdPersonItemAnimEntity>> controllers = new ArrayList<>();
		controllers.add(upperBody);
		return controllers;
	}
	
	private <E extends IAnimatable> PlayState upperBodyPredicate(AnimationEvent<E> event) {
		ThirdPersonItemAnimEntity animEntity = (ThirdPersonItemAnimEntity) event.getAnimatable();
		AnimationController<?> controller = event.getController();
		
		if (controller.getCurrentAnimation() == null) {
			LivingEntity entity = RenderEvents.ENTITY_CACHE.get(animEntity.getUUID());
			if (entity != null) {
				controller.markNeedsReload();
				controller.setAnimation(this.getDefaultAnimation(entity.getMainHandItem(), entity, controller));
			}
		}
		
		AnimationBuilder builder = animEntity.popAndGetAnim(controller.getName());
		if (builder != null) {
			controller.markNeedsReload();
			controller.setAnimation(builder);
			controller.setAnimationSpeed(animEntity.getSpeed());
		}
		
		return PlayState.CONTINUE;
	}
	
	private <E extends IAnimatable> void thirdPersonCustomInstructionListener(CustomInstructionKeyframeEvent<E> event) {
		ThirdPersonItemAnimRenderer.parse(event);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onRenderRecursively(ItemStack item, LivingEntity entity, float partialTicks, GeoBone bone, PoseStack stack,
			MultiBufferSource bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue,
			float alpha, ThirdPersonItemAnimRenderer renderer) {
		Minecraft mc = Minecraft.getInstance();
		
		LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> baseRenderer =
				(LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) mc.getEntityRenderDispatcher().getRenderer(entity);
		EntityModel<?> model = baseRenderer.getModel();
		ResourceLocation loc = baseRenderer.getTextureLocation(entity);
		
		ThirdPersonItemAnimRenderer animRenderer = RenderEvents.RENDERER_CACHE.get(entity.getUUID());
		
		String name = bone.getName();
		boolean lockedLimbs = animRenderer == null ? false : animRenderer.areLimbsLocked();
		
		if (model instanceof HumanoidModel) {
			HumanoidModel<?> bmodel = (HumanoidModel<?>) model;
			PlayerModel<?> pmodel = bmodel instanceof PlayerModel ? (PlayerModel<?>) bmodel : null;
			
			List<HumanoidArmorLayer> armorLayers = AnimUtils.getLayers(HumanoidArmorLayer.class, baseRenderer);
			HumanoidArmorLayer armor = armorLayers.isEmpty() ? null : armorLayers.get(0);
			
			boolean isSneaking = bmodel.crouching && entity.getDeltaMovement().lengthSqr() > 0.00625d;
			boolean isBody = name.equals("body");
			boolean isBodyChild = bone.parent != null && bone.parent.name.equals("body");
			boolean isFreeBone = !isBody && bone.parent == null;
			
			if (isSneaking) {
				if (isBody) {
					RenderUtils.moveToPivot(bone, stack);
					stack.mulPose(Vector3f.XN.rotation(0.5f));
					RenderUtils.moveBackFromPivot(bone, stack);
					stack.translate(0.0f, -0.0625f, 0.375f);
				} else if (isBodyChild) {
					RenderUtils.moveToPivot(bone, stack);
					stack.mulPose(Vector3f.XP.rotation(0.5f));
					RenderUtils.moveBackFromPivot(bone, stack);
				}
			}
			
			if (entity.isVisuallySwimming() || entity.isFallFlying()) {
				if (isBody) {
					stack.translate(0.0f, -0.0625f, 0.0f);
				} else if (isBodyChild) {
					RenderUtils.moveToPivot(bone, stack);
					stack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
					RenderUtils.moveBackFromPivot(bone, stack);
				} else if (isFreeBone) {
					stack.translate(0.0f, 1.375f, 0.0f);
					stack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
					stack.translate(0.0f, -1.375f, 0.0f);
				}
			}
			
			stack.pushPose();
			
			if (!lockedLimbs && !isBody) {
				RenderUtils.moveToPivot(bone, stack);
				stack.mulPose(Vector3f.XN.rotationDegrees(Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));
				RenderUtils.moveBackFromPivot(bone, stack);
			}
			
			if (isBody || isBodyChild) {
				RenderUtils.translate(bone, stack);
				RenderUtils.moveToPivot(bone, stack);
				RenderUtils.rotate(bone, stack);
				stack.mulPose(Vector3f.ZP.rotationDegrees(180f));
				RenderUtils.scale(bone, stack);
				RenderUtils.moveBackFromPivot(bone, stack);
				
				ModelPart bodyPart = null;
				ModelPart garment = null;
				
				switch (name) {
				case "body":
					bodyPart = bmodel.body;
					if (pmodel != null) garment = pmodel.jacket;
					stack.translate(0.0f, -0.75f, 0.0f);
					break;
				case "arm_left":
					bodyPart = bmodel.leftArm;
					if (pmodel != null) garment = pmodel.leftSleeve;
					stack.translate(-0.0625f, 0.0f, 0.0f);
					break;
				case "arm_right":
					bodyPart = bmodel.rightArm;
					if (pmodel != null) garment = pmodel.rightSleeve;
					stack.translate(0.0625f, 0.0f, 0.0f);
					break;
				case "head":
					bodyPart = bmodel.head;
					garment = bmodel.hat;
				}
				
				VertexConsumer entityBuilder = bufferIn.getBuffer(RenderType.entityTranslucent(loc));
				
				float skinAlpha = 1.0f;
				if (entity.isInvisible()) skinAlpha = entity == mc.player ? 0.15f : 0.0f;
				
				if (bodyPart != null) {
					bodyPart.visible = true;
					AnimUtils.renderPartOverBone(bodyPart, bone, stack, entityBuilder, packedLightIn, packedOverlayIn, skinAlpha);				
					bodyPart.visible = false;
				}
				
				if (garment != null) {
					garment.visible = true;
					AnimUtils.renderPartOverBone(garment, bone, stack, entityBuilder, packedLightIn, packedOverlayIn, skinAlpha);			
					garment.visible = false;
				}
				
				Map<EquipmentSlot, HumanoidModel<?>> armorModels = new HashMap<>();
				
				EquipmentSlot[] slots;
				switch (name) {
				case "body":
					slots = new EquipmentSlot[] { EquipmentSlot.CHEST, EquipmentSlot.LEGS };
					break;
				case "head":
					slots = new EquipmentSlot[] { EquipmentSlot.HEAD };
					break;
				default:
					slots = new EquipmentSlot[] { EquipmentSlot.CHEST };
				}
				
				for (EquipmentSlot slot : slots) {
					ItemStack slotItem = entity.getItemBySlot(slot);
					HumanoidModel<?> defaultModel = slot == EquipmentSlot.LEGS ? armor.innerModel : armor.outerModel;
					Model hookModel = ForgeHooksClient.getArmorModel(entity, slotItem, slot, defaultModel);
					if (!(hookModel instanceof HumanoidModel)) continue;
					HumanoidModel<?> moddedModel = (HumanoidModel<?>) hookModel;
					moddedModel.young = entity.isBaby();
					moddedModel.setAllVisible(false);
					switch (slot) {
					case HEAD:
						moddedModel.head.visible = true;
						moddedModel.hat.visible = true;
						AnimUtils.setupModelFromBone(moddedModel.head, bone);
						AnimUtils.setupModelFromBone(moddedModel.hat, bone);
						break;
					case CHEST:
						switch (name) {
						case "body":
							moddedModel.body.visible = true;
							AnimUtils.setupModelFromBone(moddedModel.body, bone);
							break;
						case "arm_left":
							moddedModel.leftArm.visible = true;
							AnimUtils.setupModelFromBone(moddedModel.leftArm, bone);
							break;
						case "arm_right":
							moddedModel.rightArm.visible = true;
							AnimUtils.setupModelFromBone(moddedModel.rightArm, bone);
						}
						break;
					case LEGS:
						moddedModel.body.visible = true;
						AnimUtils.setupModelFromBone(moddedModel.body, bone);
						break;
					default:
					}
					armorModels.put(slot, moddedModel);
				}
				
				for (Map.Entry<EquipmentSlot, HumanoidModel<?>> entry : armorModels.entrySet()) {
					ItemStack armorStack = entity.getItemBySlot(entry.getKey());
					Item stackItem = armorStack.getItem();
					if (stackItem instanceof ArmorItem) {
						ResourceLocation armorLoc = armor.getArmorResource(entity, armorStack, entry.getKey(), null);
						VertexConsumer armorBuilder = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(armorLoc), false, armorStack.hasFoil());
						
						if (stackItem instanceof DyeableArmorItem) {
							int color = ((DyeableArmorItem) armorStack.getItem()).getColor(armorStack);
							float r = (float)(color >> 16 & 255) / 255.0F;
							float g = (float)(color >> 8 & 255) / 255.0F;
							float b = (float)(color & 255) / 255.0F;
							
							entry.getValue().renderToBuffer(stack, armorBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
							
							ResourceLocation overlayLoc = armor.getArmorResource(entity, armorStack, entry.getKey(), "overlay");
							VertexConsumer overlayBuilder = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(overlayLoc), false, armorStack.hasFoil());
							
							entry.getValue().renderToBuffer(stack, overlayBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
						} else {
							entry.getValue().renderToBuffer(stack, armorBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
						}
					}
				}
			}
				
			stack.popPose();
				
			if (!lockedLimbs && isFreeBone) {
				stack.translate(0.0f, 1.375f, 0.0f);
				stack.mulPose(Vector3f.XN.rotationDegrees(Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));
				stack.translate(0.0f, -1.375f, 0.0f);
			}
		}
	}
	
	@Override
	public boolean shouldHideCubes(ItemStack item, LivingEntity entity, GeoBone bone, float argAlpha) {
		String name = bone.getName();
		return name.equals("body") || name.equals("arm_left") || name.equals("arm_right") || name.equals("head");
	}
	
	@Override public boolean shouldHideCrosshair(ItemStack stack) { return true; }
	
	@Override
	public ResourceLocation getTextureLocation(ItemStack stack, LivingEntity entity) {
		return TextureUtils.getWeaponSkinTexture(stack);
	}
	
	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {
			private BlockEntityWithoutLevelRenderer renderer = null;
			
			@Override
			public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
				if (this.renderer == null) {
					this.renderer = new FirearmRenderer();
				}
				return this.renderer;
			}
		});
	}
	
	/*
	 * STATIC QUERY METHODS
	 */
	
	public static boolean isMeleeing(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemData::isMeleeing).orElse(false);
	}
	
	public static boolean isFinishedAction(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemData::isFinishedAction).orElse(false);
	}
	
	public static boolean isCycled(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemData::isCycled).orElse(false);
	}
	
	public static boolean isFired(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemData::isFired).orElse(false);
	}
	
	public static boolean isAiming(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemData::isAiming).orElse(false);
	}
	
	public static ItemStack stackOf(Item item, float quality, int partCount, float weight) {
		return PartItem.stackOf(item, quality, partCount, weight);
	}
	
	public static boolean isSubmergedInWater(LivingEntity shooter) {
		return shooter.level.getBlockState(new BlockPos(shooter.getEyePosition(1.0f))).getBlock() == Blocks.WATER
				&& (!(shooter instanceof Player) || !((Player) shooter).isCreative());
	}
	
	protected static float getEffectivenessFromEntity(LivingEntity entity) {
		return entity instanceof IQualityModifier ? ((IQualityModifier) entity).getEffectiveness() : 1.0f;
	}
	
	protected static int getTimeModifiedByEntity(LivingEntity entity, int baseTime) {
		return Mth.ceil(getTimeModifier(entity) * (float) baseTime);
	}
	
	protected static float getTimeModifier(LivingEntity entity) {
		return entity instanceof IQualityModifier ? ((IQualityModifier) entity).getTimeModifier() : 1.0f;
	}
	
	/* Creative mode methods to get around issue where capability data is lost */
	
	public static ItemStack creativeStack(Item item, float quality, int partCount, float weight) {
		ItemStack stack = stackOf(item, quality, partCount, weight);
		stack.getOrCreateTag().put("creativeData", getCreativeData(stack));
		return stack;
	}
	
	public static CompoundTag getCreativeData(ItemStack stack) {
		CompoundTag tag = PartItem.getCreativeData(stack);
		getDataHandler(stack).ifPresent(h -> {
			
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundTag nbt) {
		PartItem.readCreativeData(stack, nbt);
		getDataHandler(stack).ifPresent(h -> {
			
		});
	}

	public abstract static class AbstractProperties<T extends AbstractProperties<T>> {
		private boolean needsCycle = true;
		private int cooldownTime;
		private int cycleTime;
		private int drawTime;
		private int meleeToggleTime;
		private int projectileRange;
		private int reloadEndTime;
		private int reloadStartTime;
		private int reloadTime;
		private float baseDamage;
		private float headshotMultiplier;
		private float hipfireSpread;
		private float muzzleVelocity;
		private float spread;
		private float fovModifier = 1.0f;
		private Function<LivingEntity, Float> horizontalRecoilSupplier = le -> 0.0f;
		private Function<LivingEntity, Float> verticalRecoilSupplier = le -> 0.0f;
		private Predicate<ItemStack> ammoPredicate;
		private Supplier<IItemHandlerModifiable> attachmentsHandler = ItemStackHandler::new;
		
		protected final T thisObj;
		
		public AbstractProperties() {
			this.thisObj = this.getThis();
		}
		
		protected abstract T getThis();
		
		public T needsCycle(boolean needsCycle) {
			this.needsCycle = needsCycle;
			return this.thisObj;
		}
		
		public T cooldownTime(int cooldownTime) {
			this.cooldownTime = cooldownTime;
			return this.thisObj;
		}
		
		public T cycleTime(int cycleTime) {
			this.cycleTime = cycleTime;
			return this.thisObj;
		}
		
		public T drawTime(int drawTime) {
			this.drawTime = drawTime;
			return this.thisObj;
		}
		
		public T meleeToggleTime(int meleeToggleTime) {
			this.meleeToggleTime = meleeToggleTime;
			return this.thisObj;
		}
		
		public T projectileRange(int projectileRange) {
			this.projectileRange = projectileRange;
			return this.thisObj;
		}
		
		public T reloadEndTime(int reloadEndTime) {
			this.reloadEndTime = reloadEndTime;
			return this.thisObj;
		}
		
		public T reloadStartTime(int reloadStartTime) {
			this.reloadStartTime = reloadStartTime;
			return this.thisObj;
		}
		
		public T reloadTime(int reloadTime) {
			this.reloadTime = reloadTime;
			return this.thisObj;
		}
		
		public T baseDamage(float baseDamage) {
			this.baseDamage = baseDamage;
			return this.thisObj;
		}
		
		public T headshotMultiplier(float headshotMultiplier) {
			this.headshotMultiplier = headshotMultiplier;
			return this.thisObj;
		}
		
		public T hipfireSpread(float hipfireSpread) {
			this.hipfireSpread = hipfireSpread;
			return this.thisObj;
		}
		
		public T muzzleVelocity(float muzzleVelocity) {
			this.muzzleVelocity = muzzleVelocity;
			return this.thisObj;
		}
		
		public T spread(float spread) {
			this.spread = spread;
			return this.thisObj;
		}
		
		public T horizontalRecoil(Function<LivingEntity, Float> horizontalRecoil) {
			this.horizontalRecoilSupplier = horizontalRecoil;
			return this.thisObj;
		}
		
		public T verticalRecoil(Function<LivingEntity, Float> verticalRecoil) {
			this.verticalRecoilSupplier = verticalRecoil;
			return this.thisObj;
		}
		
		public T ammoPredicate(Predicate<ItemStack> ammoPredicate) {
			this.ammoPredicate = ammoPredicate;
			return this.thisObj;
		}
		
		public T attachmentsHandler(Supplier<IItemHandlerModifiable> supplier) {
			this.attachmentsHandler = supplier;
			return this.thisObj;
		}
		
		public T fovModifier(float fovModifier) {
			this.fovModifier = fovModifier;
			return this.thisObj;
		}
	}
	
	public static class Properties extends AbstractProperties<Properties> {
		@Override protected Properties getThis() { return this; }
	}
	
	public static enum ActionType {
		NOTHING(0),
		CYCLING(1),
		RELOADING(2),
		START_RELOADING(3),
		TOGGLE_MELEE(4),
		PREVIOUS_STANCE(5);
		
		private static final ActionType[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ActionType::getId)).toArray(sz -> new ActionType[sz]);
		
		private final int id;
		
		private ActionType(int id) {
			this.id = id;
		}
		
		public int getId() { return this.id; }
		public static ActionType fromId(int id) { return BY_ID[id]; }
	}
	
	public void onCameraSetup(CameraSetup event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		ItemStack stack = mc.player.getMainHandItem();
		getDataHandler(stack).ifPresent(h -> {
			float ticks = (float) h.getRecoilTicks() + (float) event.getPartialTicks();
			float oldTicks = Math.max(ticks - mc.getDeltaFrameTime(), 0.0f);
			ticks *= 0.1f;
			oldTicks *= 0.1f;
			if (ticks >= 1.0f) return;
			
			float addPitch = h.getRecoilPitch() * (this.getVerticalRecoilScalar(ticks) - this.getVerticalRecoilScalar(oldTicks)); 
			float addYaw = h.getRecoilYaw() * (this.getHorizontalRecoilScalar(ticks) - this.getHorizontalRecoilScalar(oldTicks));
			
			mc.player.turn((double) addYaw, (double) -addPitch);
		});
	}
	
	protected float getHorizontalRecoilScalar(float time) {
		if (time <= 0.0f) {
			return 0.0f;
		}
		if (time < 0.25f) {
			return IWMiscUtils.quadEasingOut(time * 4.0f);
		}
		if (time <= 1.0f) {
			float f = 1.0f / 3.0f;
			return 1.0f - IWMiscUtils.quadEasingInOut(time * 4.0f * f - f);
		}
		return 0.0f;
	}
	
	protected float getVerticalRecoilScalar(float time) {
		return this.getHorizontalRecoilScalar(time);
	}
	
}
