package rbasamoyai.industrialwarfare.common.entities;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Brain.BrainCodec;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCContainer;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entityai.NPCActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.NPCTasks;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.core.init.NPCProfessionInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.CNPCBrainDataSyncMessage;
import rbasamoyai.industrialwarfare.utils.IWInventoryUtils;

/*
 * Base NPC entity class for rbasamoyai's Industrial Warfare.
 */

public class NPCEntity extends CreatureEntity implements IWeaponRangedAttackMob {
	
	protected static final Supplier<List<MemoryModuleType<?>>> MEMORY_TYPES = () -> ImmutableList.of(
			MemoryModuleType.ATTACK_COOLING_DOWN,
			MemoryModuleType.ATTACK_TARGET,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
			MemoryModuleType.DOORS_TO_CLOSE,
			MemoryModuleType.HEARD_BELL_TIME,
			MemoryModuleType.HOME,
			MemoryModuleType.JOB_SITE,
			MemoryModuleType.LOOK_TARGET,
			MemoryModuleType.MEETING_POINT,
			MemoryModuleType.LIVING_ENTITIES,
			MemoryModuleType.VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.PATH,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleTypeInit.ACTIVITY_STATUS.get(),
			MemoryModuleTypeInit.CACHED_POS.get(),
			MemoryModuleTypeInit.COMPLAINT.get(),
			MemoryModuleTypeInit.CURRENT_ORDER.get(),
			MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(),
			MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get(),
			MemoryModuleTypeInit.JUMP_TO.get(),
			MemoryModuleTypeInit.ON_PATROL.get(),
			MemoryModuleTypeInit.STOP_EXECUTION.get(),
			MemoryModuleTypeInit.WAIT_FOR.get()
			);
	protected static final Supplier<List<SensorType<? extends Sensor<? super NPCEntity>>>> SENSOR_TYPES = () -> ImmutableList.of(
			SensorType.NEAREST_PLAYERS, 
			SensorType.NEAREST_BED,
			SensorType.NEAREST_LIVING_ENTITIES
			);
	
	public static final String TAG_WORKSTUFFS = "workstuffs";
	private static final String TAG_INVENTORY = "items";
	
	public static final int MAX_SLOTS_BEFORE_WARN = 54;
	
	protected final ItemStackHandler inventoryItemHandler;
	protected final EquipmentItemHandler equipmentItemHandler;

	private float effectiveness;
	private float timeModifier;
	
	public NPCEntity(EntityType<? extends NPCEntity> type, World worldIn) {
		this(type, worldIn, NPCProfessionInit.JOBLESS.get(), null, 5, true);
	}
	
	public NPCEntity(EntityType<? extends NPCEntity> type, World worldIn, NPCProfession profession, @Nullable PlayerEntity owner, int initialInventoryCount, boolean canWearEquipment) {
		super(type, worldIn);
		
		this.setCustomName(new StringTextComponent("Unnamed NPC"));
		
		this.getDataHandler().ifPresent(h -> {
			h.setCanWearEquipment(canWearEquipment);
			h.setOwner(owner == null ? PlayerIDTag.NO_OWNER : PlayerIDTag.of(owner));
			h.setProfession(profession);
		});
		
		this.inventoryItemHandler = new ItemStackHandler(initialInventoryCount);		
		this.equipmentItemHandler = new EquipmentItemHandler(this);
		
		((GroundPathNavigator) this.getNavigation()).setCanOpenDoors(true);
		
		this.getNextEffectiveness();
	}
	
	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return CreatureEntity.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0d)
				.add(Attributes.MOVEMENT_SPEED, 0.1d)
				.add(Attributes.ATTACK_DAMAGE, 2.0d)
				.add(Attributes.ATTACK_SPEED, 4.0d);
	}
	
	public ItemStackHandler getInventoryItemHandler() {
		return this.inventoryItemHandler;
	}
	
	public EquipmentItemHandler getEquipmentItemHandler() {
		return this.equipmentItemHandler;
	}
	
	public LazyOptional<INPCDataHandler> getDataHandler() {
		return this.getCapability(NPCDataCapability.NPC_DATA_CAPABILITY);
	}
	
	public void resizeInventoryItemHandler(int newSize) {
		if (this.inventoryItemHandler.getSlots() < newSize)
			IndustrialWarfare.LOGGER.warn("Shrinking inventory of NPC entity with UUID " + this.getUUID().toString() + ", this may cause data loss (inventory size went from " + this.inventoryItemHandler.getSlots() + " to " + newSize + ")");
		if (newSize > MAX_SLOTS_BEFORE_WARN)
			IndustrialWarfare.LOGGER.warn("The inventory of an NPC entity with UUID " + this.getUUID().toString() + " has been set past " + MAX_SLOTS_BEFORE_WARN + " slots (set to " + newSize + " slots), this may result in graphical glitches in the GUI.");
		this.inventoryItemHandler.setSize(newSize);
	}
	
	/**
	 * AI METHODS
	 */
	
	@Override
	protected BrainCodec<NPCEntity> brainProvider() {
		return Brain.provider(MEMORY_TYPES.get(), SENSOR_TYPES.get());
	}
	
	@Override
	protected Brain<?> makeBrain(Dynamic<?> input) {
		Brain<NPCEntity> brain = this.brainProvider().makeBrain(input);
		this.registerBrainGoals(brain);
		return brain;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Brain<NPCEntity> getBrain() {
		return (Brain<NPCEntity>) super.getBrain();
	}
	
	private void registerBrainGoals(Brain<NPCEntity> brain) {
		brain.addActivity(Activity.CORE, NPCTasks.getCorePackage());
		brain.addActivity(Activity.IDLE, NPCTasks.getIdlePackage());
		brain.addActivity(Activity.WORK, NPCTasks.getWorkPackage());
		brain.addActivity(Activity.FIGHT, NPCTasks.getFightPackage());
		brain.addActivity(Activity.REST, NPCTasks.getRestPackage());
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		
		NPCActivityStatus status;
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.ACTIVITY_STATUS.get())) {
			status = NPCActivityStatus.NO_ACTIVITY;
			brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), status);
		} else {
			status = brain.getMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get()).get();
		}
		
		switch (status) {
		case NO_ACTIVITY:
			brain.setActiveActivityIfPossible(Activity.IDLE);
			break;
		case WORKING:
			brain.setActiveActivityIfPossible(Activity.WORK);
			brain.eraseMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get());
			break;
		case FIGHTING:
			brain.setActiveActivityIfPossible(Activity.FIGHT);
			break;
		}
		
		brain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(this.level.dimension(), new BlockPos(0, 56, 10)));
		brain.setMemory(MemoryModuleType.HOME, GlobalPos.of(this.level.dimension(), new BlockPos(10, 55, 10)));
	}
	
	@Override
	protected void customServerAiStep() {
		Brain<NPCEntity> brain = this.getBrain();
		brain.tick((ServerWorld) this.level, this);
		
		if (this.level.getGameTime() % 20 == 0) {
			CNPCBrainDataSyncMessage msg = new CNPCBrainDataSyncMessage(this.getId(), brain.getMemory(MemoryModuleTypeInit.COMPLAINT.get()).orElse(NPCComplaintInit.CLEAR.get()), this.blockPosition()); 
			IWNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), msg);
		}
			
		super.customServerAiStep();
	}
	
	/*
	 * INTERFACING METHODS
	 */
	
	@Override
	protected ActionResultType mobInteract(PlayerEntity player, Hand handIn) {
		ActionResultType actionResultType = this.checkAndHandleImportantInteractions(player, handIn);
		if (actionResultType.consumesAction()) {
			return actionResultType;
		} else {
			if (!this.level.isClientSide && player instanceof ServerPlayerEntity) {
				IContainerProvider containerProvider = NPCContainer.getServerContainerProvider(this);
				INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(containerProvider, this.getCustomName());
				NetworkHooks.openGui((ServerPlayerEntity) player, namedProvider, buf -> {
					buf.writeVarInt(this.inventoryItemHandler.getSlots());
					buf.writeBoolean(this.getDataHandler().map(INPCDataHandler::canWearEquipment).orElse(false));
					// TODO: implement professions and write job id
				});
				return ActionResultType.SUCCESS;
			}
			return super.mobInteract(player, handIn);
		}
	}
	
	protected ActionResultType checkAndHandleImportantInteractions(PlayerEntity player, Hand handIn) {
		ItemStack handStack = player.getItemInHand(handIn);
		//Item handItem = handStack.getItem();
		return handStack.interactLivingEntity(player, this, handIn);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SOUL_ESCAPE;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundNBT tag) {
		super.addAdditionalSaveData(tag);
		
		tag.put(TAG_WORKSTUFFS, this.equipmentItemHandler.serializeNBT());
		tag.put(TAG_INVENTORY, this.inventoryItemHandler.serializeNBT());
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT tag) {
		super.readAdditionalSaveData(tag);
		
		this.equipmentItemHandler.deserializeNBT(tag);
		this.inventoryItemHandler.deserializeNBT(tag.getCompound(TAG_INVENTORY));
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	private static final BiFunction<IItemHandler, Integer, ItemStack> EXTRACT_ONE_ITEM = (handler, index) -> handler.extractItem(index, 1, false);
	
	@Override
	public ItemStack getProjectile(ItemStack weapon) {
		Item weaponItem = weapon.getItem();
		if (weaponItem instanceof ShootableItem) {
			Predicate<ItemStack> predicate = ((ShootableItem) weaponItem).getAllSupportedProjectiles();
			
			for (Hand hand : Hand.values()) {
				ItemStack handstack = this.getItemInHand(hand);
				if (predicate.test(handstack)) return handstack;
			}
			
			ItemStack inventorystack = IWInventoryUtils.iterateAndApplyIf(
					this.inventoryItemHandler,
					(handler, index) -> iterateDeeperIfContainer(
							handler,
							index,
							EXTRACT_ONE_ITEM,
							predicate,
							predicate,
							() -> ItemStack.EMPTY,
							EXTRACT_ONE_ITEM),
					predicate.or(s -> s.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()),
					predicate,
					() -> ItemStack.EMPTY);
			
			if (!inventorystack.isEmpty()) return inventorystack;
			
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean canFireProjectileWeapon(ShootableItem weapon) {
		return this.canUseRangedWeapon(new ItemStack(weapon));
	}
	
	@Override
	public void startReloading() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		this.getNextEffectiveness();
		this.getNextTimeModifier();
		
		if (weaponItem instanceof BowItem || weaponItem instanceof CrossbowItem) {
			this.startUsingItem(Hand.MAIN_HAND);
		}
		
		// TODO: gun item
	}
	
	@Override
	public boolean whileReloading() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		if (weaponItem instanceof BowItem) return false;
		
		if (weaponItem instanceof CrossbowItem) {
			float chargingFor = (float) this.getTicksUsingItem();
			if (chargingFor >= (float) CrossbowItem.getChargeDuration(weapon) * this.timeModifier) {
				this.releaseUsingItem();
				CrossbowItem.setCharged(weapon, true);
				return false;
			} else {
				return true;
			}
		}
		
		// TODO: gun item
		
		return false;
	}
	
	@Override
	public int getRangedAttackDelay() {
		return MathHelper.ceil(60.0f * this.timeModifier);
	}
	
	@Override
	public void performRangedAttack(LivingEntity target, float damage) {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		this.getNextEffectiveness();
		this.getNextTimeModifier();
		
		if (weaponItem instanceof BowItem) {
			this.shootUsingBow(target);
			weapon.hurtAndBreak(1, this, npc -> this.broadcastBreakEvent(this.getUsedItemHand()));
			this.stopUsingItem();
		} else if (weaponItem instanceof CrossbowItem) {
			this.shootUsingCrossbow(target);
			CrossbowItem.setCharged(weapon, false);
		}
		
		// TODO: gun item
		
	}
	
	private static final double ONE_THIRD = 1.0d / 3.0d;
	
	/**
	 * Code based on {@link net.minecraft.entity.monster.AbstractSkeletonEntity#performRangedAttack AbstractSkeletonEntity#performRangedAttack}
	 */
	private void shootUsingBow(LivingEntity target) {
		ItemStack bow = this.getItemInHand(ProjectileHelper.getWeaponHoldingHand(this, item -> item instanceof BowItem));
		ItemStack projectile = this.getProjectile(bow);
		AbstractArrowEntity arrow = ProjectileHelper.getMobArrow(this, projectile, BowItem.getPowerForTime(this.getTicksUsingItem()));
		Item mainhandItem = this.getMainHandItem().getItem();
		if (mainhandItem instanceof BowItem) {
			arrow = ((BowItem) mainhandItem).customArrow(arrow);
		}
		
		double dx = target.getX() - this.getX();
		double dz = target.getZ() - this.getZ();
		double dyOffset = MathHelper.sqrt(dx * dx + dz * dz);
		double dy = target.getY(ONE_THIRD) - arrow.getY() + dyOffset * 0.2d;
		float spread = 14.0f - 12.0f * this.effectiveness;
		arrow.shoot(dx, dy, dz, 1.6f, spread);
		
		this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
		
		this.level.addFreshEntity(arrow);
	}
	
	/**
	 * Code based on {@link net.minecraft.entity.ICrossbowUser#performCrossbowAttack ICrossbowUser#performCrossbowAttack}
	 */
	private void shootUsingCrossbow(LivingEntity target) {
		Hand hand = ProjectileHelper.getWeaponHoldingHand(this, item -> item instanceof CrossbowItem);
		ItemStack crossbow = this.getItemInHand(hand);
		if (this.isHolding(item -> item instanceof CrossbowItem)) {
			float spread = 14.0f - 12.0f * this.effectiveness;
			CrossbowItem.performShooting(this.level, this, hand, crossbow, 1.6f, spread);
		}
	}
	
	@Override
	public boolean cycleOrReload() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		// TODO: gun item
			
		return false;
	}
	
	@Override
	public void startCycling() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		this.getNextEffectiveness();
		this.getNextTimeModifier();
		
		// TODO: gun item
	}
	
	@Override
	public boolean whileCycling() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		// TODO: gun item
		
		return false;
	}
	
	@Override
	public boolean canDoRangedAttack() {
		ItemStack weapon = this.getMainHandItem();
		return this.canUseRangedWeapon(weapon) && this.hasAmmoForWeapon(weapon);
	}
	
	private boolean canUseRangedWeapon(ItemStack weapon) {
		return this.getDataHandler().map(h -> h.getProfession().getCombatUnit().canUseRangedWeapon(this, weapon)).orElse(false);
	}
	
	private boolean hasAmmoForWeapon(ItemStack weapon) {
		Item weaponItem = weapon.getItem();
		if (weaponItem instanceof ShootableItem) {
			Predicate<ItemStack> predicate = ((ShootableItem) weaponItem).getAllSupportedProjectiles();
			
			for (Hand hand : Hand.values()) {
				if (predicate.test(this.getItemInHand(hand))) return true;
			}
			
			return IWInventoryUtils.iterateAndApplyIf(
					this.inventoryItemHandler,
					(handler, index) -> iterateDeeperIfContainer(
							handler,
							index,
							(h, i) -> true,
							predicate,
							b -> b,
							() -> false,
							(h, i) -> true),
					predicate.or(s -> s.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()),
					b -> b,
					() -> false);
		}
		return false;
	}
	
	private float getNextEffectiveness() {
		this.effectiveness = this.getDataHandler()
				.map(h -> h.getProfession().getCombatUnit().getEffectiveness(this))
				.orElse(this.getRandom().nextFloat());
		return this.effectiveness;
	}
	
	private float getNextTimeModifier() {
		this.timeModifier = this.getDataHandler()
				.map(h -> h.getProfession().getCombatUnit().getTimeModifier(this))
				.orElse(this.getRandom().nextFloat());
		return this.timeModifier;
	}
	
	private static <T> T iterateDeeperIfContainer(
			IItemHandler handler,
			int index,
			BiFunction<IItemHandler, Integer, T> containerFunction,
			Predicate<ItemStack> containerPredicate,
			Predicate<T> containerResultPredicate,
			Supplier<T> orElseInContainer,
			BiFunction<IItemHandler, Integer, T> orElse) {
		ItemStack stack = handler.getStackInSlot(index);
		LazyOptional<IItemHandler> lzop = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		return lzop.isPresent()
				? IWInventoryUtils.iterateAndApplyIf(
						lzop.resolve().get(),
						containerFunction,
						containerPredicate,
						containerResultPredicate,
						orElseInContainer)
				: orElse.apply(handler, index);
	}
	
}
