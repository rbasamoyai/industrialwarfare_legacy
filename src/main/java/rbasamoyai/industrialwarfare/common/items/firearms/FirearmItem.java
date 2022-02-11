package rbasamoyai.industrialwarfare.common.items.firearms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.client.events.RenderEvents;
import rbasamoyai.industrialwarfare.client.items.renderers.FirearmRenderer;
import rbasamoyai.industrialwarfare.client.items.renderers.ISpecialThirdPersonRender;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.FirearmItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.FirearmItemDataProvider;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem.IFirearmItemDataHandler;
import rbasamoyai.industrialwarfare.common.entities.IQualityModifier;
import rbasamoyai.industrialwarfare.common.entities.ThirdPersonItemAnimEntity;
import rbasamoyai.industrialwarfare.common.items.IFirstPersonTransform;
import rbasamoyai.industrialwarfare.common.items.IFovModifier;
import rbasamoyai.industrialwarfare.common.items.IHideCrosshair;
import rbasamoyai.industrialwarfare.common.items.IItemWithAttachments;
import rbasamoyai.industrialwarfare.common.items.ISimultaneousUseAndAttack;
import rbasamoyai.industrialwarfare.common.items.PartItem;
import rbasamoyai.industrialwarfare.common.items.QualityItem;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.ParticleKeyFrameEvent;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;

public abstract class FirearmItem extends ShootableItem implements
		ISimultaneousUseAndAttack,
		IFovModifier,
		IFirstPersonTransform,
		IHideCrosshair,
		IItemWithAttachments,
		IAnimatable,
		ISyncable,
		ISpecialThirdPersonRender {
	
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
	protected final float fovModifier;
	protected final float headshotMultiplier;
	protected final float hipfireSpread;
	protected final float muzzleVelocity;
	protected final float spread;
	protected final Function<LivingEntity, Float> horizontalRecoilSupplier;
	protected final Function<LivingEntity, Float> verticalRecoilSupplier;
	protected final Predicate<ItemStack> ammoPredicate;
	protected final Supplier<IFirearmItemDataHandler> dataHandlerSupplier;
	
	public AnimationFactory factory = new AnimationFactory(this);
	
	public FirearmItem(Item.Properties itemProperties, FirearmItem.Properties firearmProperties, Supplier<IFirearmItemDataHandler> dataHandlerSupplier) {
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
		this.fovModifier = firearmProperties.fovModifier;
		this.headshotMultiplier = firearmProperties.headshotMultiplier;
		this.hipfireSpread = firearmProperties.hipfireSpread;
		this.muzzleVelocity = firearmProperties.muzzleVelocity;
		this.spread = firearmProperties.spread;
		this.horizontalRecoilSupplier = firearmProperties.horizontalRecoilSupplier;
		this.verticalRecoilSupplier = firearmProperties.verticalRecoilSupplier;
		this.ammoPredicate = firearmProperties.ammoPredicate;
		this.dataHandlerSupplier = dataHandlerSupplier;
		
		GeckoLibNetwork.registerSyncable(this);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		FirearmItemDataProvider provider = new FirearmItemDataProvider(this.dataHandlerSupplier.get());
		provider.getCapability(FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY).ifPresent(h -> {
			h.setAction(ActionType.NOTHING, 0);
			h.setAiming(false);
		});
		if (nbt != null) {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		
		return provider;
	}
		
	public static LazyOptional<IFirearmItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			if (FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY != null)
				tag.put("item_cap", FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY.writeNBT(h, null));
		});
		return tag;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundNBT nbt) {
		stack.setTag(nbt);
		
		if (nbt == null) return;
		
		if (nbt.contains("creativeData", Constants.NBT.TAG_COMPOUND)) {
			readCreativeData(stack, nbt.getCompound("creativeData"));
			nbt.remove("creativeData");
			return;
		}
		
		getDataHandler(stack).ifPresent(h -> {
			if (FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY != null)
				FirearmItemDataCapability.FIREARM_ITEM_DATA_CAPABILITY.readNBT(h, null, nbt.getCompound("item_cap"));
		});
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
		
		getDataHandler(stack).ifPresent(h -> {
			if (!h.isFinishedAction() || h.isMeleeing()) return;
			
			if (h.hasAmmo() && (!this.needsCycle || h.isCycled())) {
				this.shoot(stack, entity);
				stack.hurtAndBreak(1, entity, e -> {
					e.broadcastBreakEvent(entity.swingingArm);
				});
			} else if (this.needsCycle && !h.isCycled() && !h.isAiming()) {
				this.startCycle(stack, entity);
			} else if (this.getAllSupportedProjectiles().test(entity.getProjectile(stack)) && !h.isFull() && !h.isAiming()) {
				this.startReload(stack, entity);
			}
		});
		return true;
	}
	
	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand != Hand.MAIN_HAND) return ActionResult.pass(stack);
		if (getDataHandler(stack).map(h -> {
			return !h.isFinishedAction() && h.getAction() != ActionType.NOTHING || h.isMeleeing();
		}).orElse(false)) {
			return ActionResult.fail(stack);
		}
		player.startUsingItem(hand);
		this.startAiming(stack, player);
		return world.isClientSide ? ActionResult.pass(stack) : ActionResult.consume(stack);
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}
	
	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.NONE;
	}
	
	@Override
	public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int time) {
		this.stopAiming(stack, entity);
	}
	
	protected abstract void shoot(ItemStack firearm, LivingEntity shooter);
	
	protected abstract void startCycle(ItemStack firearm, LivingEntity shooter);
	
	protected abstract void startReload(ItemStack firearm, LivingEntity shooter);
	
	protected abstract void startAiming(ItemStack firearm, LivingEntity shooter);
	
	protected abstract void stopAiming(ItemStack firearm, LivingEntity shooter);
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!(entity instanceof LivingEntity)) return;
		LivingEntity shooter = (LivingEntity) entity;

		if (selected) {
			shooter.yBodyRotO = shooter.yBodyRot;
			shooter.yBodyRot = shooter.yHeadRot;
		}
		
		if (world.isClientSide) return;
		GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) world);
			
		getDataHandler(stack).ifPresent(h -> {
			if (!selected) {
				// No time modified as an IQualityModifier entity could return a different time modifier each call
				h.setAction(ActionType.NOTHING, this.drawTime);
				h.setAiming(false);
				return;
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
			case TOGGLE_MELEE: this.endToggleMelee(firearm, shooter);
			}
		});
	}
	
	public boolean needsCycle(ItemStack stack) { return this.needsCycle; }
	
	protected abstract void endCycle(ItemStack firearm, LivingEntity shooter);
	
	protected abstract void reload(ItemStack firearm, LivingEntity shooter);
	
	/** For starting repeated reload animations (e.g. tube loading) */
	protected void actuallyStartReloading(ItemStack firearm, LivingEntity shooter) {}
	
	protected void doNothing(ItemStack firearm, LivingEntity shooter) {}
	
	public static void tryReloadFirearm(ItemStack firearm, LivingEntity shooter) {
		if (shooter.level.isClientSide) return;
		Item item = firearm.getItem();
		if (!(item instanceof FirearmItem)) return;
		FirearmItem firearmItem = (FirearmItem) item;
		
		getDataHandler(firearm).ifPresent(h -> {
			if (h.isFinishedAction() && !h.isFull()) firearmItem.startReload(firearm, shooter);
		});
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		QualityItem.appendHoverTextStatic(stack, world, tooltip, flag);
		PartItem.appendHoverTextStatic(stack, world, tooltip, flag);
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}
	
	@Override
	public boolean canAttackBlock(BlockState state, World level, BlockPos pos, PlayerEntity player) {
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
		//return isAiming(stack) ? this.fovModifier : 1.0f;
		return 1.0f;
	}
	
	@Override
	public boolean shouldTransform(ItemStack stack, PlayerEntity player) {
		return getDataHandler(stack).map(h -> (h.getAction() == ActionType.NOTHING || h.isFinishedAction()) && h.isAiming()).orElse(false);
	}
	
	@Override
	public void transformMatrixStack(ItemStack itemStack, PlayerEntity player, MatrixStack matrixStack) {
		matrixStack.scale(1.0f, 1.0f, 0.5f);
	}
	
	/*
	 * ANIMATION METHODS
	 */
	
	protected <P extends IAnimatable> void soundListener(SoundKeyframeEvent<P> event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(event.sound));
		mc.player.playSound(sound, 1.0f, 1.0f);
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
		
		ItemStackTileEntityRenderer ister = this.getItemStackTileEntityRenderer();
		if (!(ister instanceof FirearmRenderer)) return;
		FirearmRenderer imrister = (FirearmRenderer) ister;
		
		for (List<String> tokens : instructionTokens) {
			String firstTok = tokens.get(0);
			if (firstTok.equals("set_hidden")) {
				String boneName = tokens.get(1);
				boolean hidden = Boolean.valueOf(tokens.get(2));
				imrister.setBoneVisibility(boneName, hidden);
			} else if (firstTok.equals("move")) {
				String boneName = tokens.get(1);
				float x = Float.valueOf(tokens.get(2));
				float y = Float.valueOf(tokens.get(3));
				float z = Float.valueOf(tokens.get(4));
				imrister.moveBone(boneName, x, y, z);
			}
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
		Vector3d entityPos = entity.position();
		entity.level.playLocalSound(entityPos.x, entityPos.y, entityPos.z, sound, SoundCategory.MASTER, 1.0f, 1.0f, true);
	}

	@Override
	public AnimationFactory getFactory() {
		return this.factory;
	}
	
	/*
	 * STATIC QUERY METHODS
	 */
	
	public static boolean isMeleeing(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::isMeleeing).orElse(false);
	}
	
	public static boolean isFinishedAction(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::isFinishedAction).orElse(false);
	}
	
	public static boolean isCycled(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::isCycled).orElse(false);
	}
	
	public static boolean isFired(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::isFired).orElse(false);
	}
	
	public static boolean isAiming(ItemStack stack) {
		return getDataHandler(stack).map(IFirearmItemDataHandler::isAiming).orElse(false);
	}
	
	public static ItemStack stackOf(Item item, float quality, int partCount, float weight) {
		return PartItem.stackOf(item, quality, partCount, weight);
	}
	
	protected static float getEffectivenessFromEntity(LivingEntity entity) {
		return entity instanceof IQualityModifier ? ((IQualityModifier) entity).getEffectiveness() : 1.0f;
	}
	
	protected static int getTimeModifiedByEntity(LivingEntity entity, int baseTime) {
		return MathHelper.ceil(getTimeModifier(entity) * (float) baseTime);
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
	
	public static CompoundNBT getCreativeData(ItemStack stack) {
		CompoundNBT tag = PartItem.getCreativeData(stack);
		getDataHandler(stack).ifPresent(h -> {
			
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundNBT nbt) {
		PartItem.readCreativeData(stack, nbt);
		getDataHandler(stack).ifPresent(h -> {
			
		});
	}

	public static class Properties {
		public boolean needsCycle = true;
		public int cooldownTime;
		public int cycleTime;
		public int drawTime;
		public int meleeToggleTime;
		public int projectileRange;
		public int reloadEndTime;
		public int reloadStartTime;
		public int reloadTime;
		public float baseDamage;
		public float fovModifier = 1.0f;
		public float headshotMultiplier;
		public float hipfireSpread;
		public float muzzleVelocity;
		public float spread;
		public Function<LivingEntity, Float> horizontalRecoilSupplier;
		public Function<LivingEntity, Float> verticalRecoilSupplier;
		public Predicate<ItemStack> ammoPredicate;
		
		public Properties needsCycle(boolean needsCycle) {
			this.needsCycle = needsCycle;
			return this;
		}
		
		public Properties cooldownTime(int cooldownTime) {
			this.cooldownTime = cooldownTime;
			return this;
		}
		
		public Properties cycleTime(int cycleTime) {
			this.cycleTime = cycleTime;
			return this;
		}
		
		public Properties drawTime(int drawTime) {
			this.drawTime = drawTime;
			return this;
		}
		
		public Properties meleeToggleTime(int meleeToggleTime) {
			this.meleeToggleTime = meleeToggleTime;
			return this;
		}
		
		public Properties projectileRange(int projectileRange) {
			this.projectileRange = projectileRange;
			return this;
		}
		
		public Properties reloadEndTime(int reloadEndTime) {
			this.reloadEndTime = reloadEndTime;
			return this;
		}
		
		public Properties reloadStartTime(int reloadStartTime) {
			this.reloadStartTime = reloadStartTime;
			return this;
		}
		
		public Properties reloadTime(int reloadTime) {
			this.reloadTime = reloadTime;
			return this;
		}
		
		public Properties baseDamage(float baseDamage) {
			this.baseDamage = baseDamage;
			return this;
		}
		
		public Properties fovModifier(float fovModifier) {
			this.fovModifier = fovModifier;
			return this;
		}
		
		public Properties headshotMultiplier(float headshotMultiplier) {
			this.headshotMultiplier = headshotMultiplier;
			return this;
		}
		
		public Properties hipfireSpread(float hipfireSpread) {
			this.hipfireSpread = hipfireSpread;
			return this;
		}
		
		public Properties muzzleVelocity(float muzzleVelocity) {
			this.muzzleVelocity = muzzleVelocity;
			return this;
		}
		
		public Properties spread(float spread) {
			this.spread = spread;
			return this;
		}
		
		public Properties horizontalRecoil(Function<LivingEntity, Float> horizontalRecoil) {
			this.horizontalRecoilSupplier = horizontalRecoil;
			return this;
		}
		
		public Properties verticalRecoil(Function<LivingEntity, Float> verticalRecoil) {
			this.verticalRecoilSupplier = verticalRecoil;
			return this;
		}
		
		public Properties ammoPredicate(Predicate<ItemStack> ammoPredicate) {
			this.ammoPredicate = ammoPredicate;
			return this;
		}
	}
	
	public static enum ActionType {
		NOTHING(0),
		CYCLING(1),
		RELOADING(2),
		START_RELOADING(3),
		TOGGLE_MELEE(4);
		
		private static final ActionType[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ActionType::getId)).toArray(sz -> new ActionType[sz]);
		
		private final int id;
		
		private ActionType(int id) {
			this.id = id;
		}
		
		public int getId() { return this.id; }
		public static ActionType fromId(int id) { return BY_ID[id]; }
	}
	
}