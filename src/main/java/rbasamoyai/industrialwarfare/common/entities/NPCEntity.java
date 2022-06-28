package rbasamoyai.industrialwarfare.common.entities;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCData;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCMenu;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.NPCTasks;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.common.items.ISpeedloadable;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem.ActionType;
import rbasamoyai.industrialwarfare.common.npccombatskill.NPCCombatSkill;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCCombatSkillInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.core.init.NPCProfessionInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.CNPCBrainDataSyncMessage;
import rbasamoyai.industrialwarfare.utils.ModInventoryUtils;

/*
 * Base NPC entity class for rbasamoyai's Industrial Warfare.
 */

public class NPCEntity extends PathfinderMob implements
		IWeaponRangedAttackMob,
		IQualityModifier,
		HasDiplomaticOwner,
		IItemPredicateSearch,
		MovesInFormation {
	
	/** Taken from {@link net.minecraft.entity.player.Player#POSES}. */
	private static final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder()
			.put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F))
			.put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F))
			.put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F))
			.put(Pose.CROUCHING, EntityDimensions.scalable(0.6F, 1.5F)).build();
	
	protected static final Supplier<List<MemoryModuleType<?>>> MEMORY_TYPES = () -> ImmutableList.of(
			MemoryModuleType.ANGRY_AT,
			MemoryModuleType.ATTACK_COOLING_DOWN,
			MemoryModuleType.ATTACK_TARGET,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
			MemoryModuleType.CELEBRATE_LOCATION,
			MemoryModuleType.DANCING,
			MemoryModuleType.DOORS_TO_CLOSE,
			MemoryModuleType.HEARD_BELL_TIME,
			MemoryModuleType.HOME,
			MemoryModuleType.INTERACTION_TARGET,
			MemoryModuleType.JOB_SITE,
			MemoryModuleType.LOOK_TARGET,
			MemoryModuleType.MEETING_POINT,
			MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
			MemoryModuleType.NEAREST_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.PATH,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleTypeInit.ACTIVITY_STATUS.get(),
			MemoryModuleTypeInit.BLOCK_INTERACTION.get(),
			MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(),
			MemoryModuleTypeInit.CACHED_POS.get(),
			MemoryModuleTypeInit.CAN_ATTACK.get(),
			MemoryModuleTypeInit.COMBAT_MODE.get(),
			MemoryModuleTypeInit.COMPLAINT.get(),
			MemoryModuleTypeInit.CURRENT_ORDER.get(),
			MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(),
			MemoryModuleTypeInit.DEFENDING_SELF.get(),
			MemoryModuleTypeInit.DEPOSITING_ITEMS.get(),
			MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get(),
			MemoryModuleTypeInit.FINISHED_ATTACKING.get(),
			MemoryModuleTypeInit.IN_COMMAND_GROUP.get(),
			MemoryModuleTypeInit.IN_FORMATION.get(),
			MemoryModuleTypeInit.JUMP_TO.get(),
			MemoryModuleTypeInit.ON_PATROL.get(),
			MemoryModuleTypeInit.PRECISE_POS.get(),
			MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get(),
			MemoryModuleTypeInit.SHOOTING_POS.get(),
			MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(),
			MemoryModuleTypeInit.STOP_EXECUTION.get(),
			MemoryModuleTypeInit.SUPPLY_REQUESTS.get(),
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
	
	protected int actionDelay;
	
	public NPCEntity(EntityType<? extends NPCEntity> type, Level worldIn) {
		this(type, worldIn, NPCProfessionInit.JOBLESS.get(), NPCCombatSkillInit.UNTRAINED.get(), null, 5, true);
	}
	
	public NPCEntity(EntityType<? extends NPCEntity> type, Level worldIn, NPCProfession profession, NPCCombatSkill combatSkill, @Nullable Player owner, int initialInventoryCount, boolean canWearEquipment) {
		super(type, worldIn);
		
		this.setCustomName(new TextComponent("Unnamed NPC"));
		
		this.getDataHandler().ifPresent(h -> {
			h.setCanWearEquipment(canWearEquipment);
			h.setOwner(owner == null ? PlayerIDTag.NO_OWNER : PlayerIDTag.of(owner));
			h.setProfession(profession);
			h.setCombatSkill(combatSkill);
		});
		
		this.inventoryItemHandler = new ItemStackHandler(initialInventoryCount);		
		this.equipmentItemHandler = new EquipmentItemHandler(this);
		
		((GroundPathNavigation) this.navigation).setCanOpenDoors(true);
		
		this.getNextEffectiveness();
		
		this.setPersistenceRequired();
		this.setCanPickUpLoot(true);
	}
	
	public static AttributeSupplier.Builder setAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0d)
				.add(Attributes.MOVEMENT_SPEED, 0.1d)
				.add(Attributes.ATTACK_DAMAGE, 2.0d)
				.add(Attributes.ATTACK_SPEED, 4.0d)
				.add(Attributes.FOLLOW_RANGE, 100.0d);
	}
	
	public ItemStackHandler getInventoryItemHandler() {
		return this.inventoryItemHandler;
	}
	
	public EquipmentItemHandler getEquipmentItemHandler() {
		this.equipmentItemHandler.update();
		return this.equipmentItemHandler;
	}
	
	public LazyOptional<INPCData> getDataHandler() {
		return this.getCapability(NPCDataCapability.INSTANCE);
	}
	
	public void resizeInventoryItemHandler(int newSize) {
		if (this.inventoryItemHandler.getSlots() < newSize)
			IndustrialWarfare.LOGGER.warn("Shrinking inventory of NPC entity with UUID " + this.getUUID().toString() + ", this may cause data loss (inventory size went from " + this.inventoryItemHandler.getSlots() + " to " + newSize + ")");
		if (newSize > MAX_SLOTS_BEFORE_WARN)
			IndustrialWarfare.LOGGER.warn("The inventory of an NPC entity with UUID " + this.getUUID().toString() + " has been set past " + MAX_SLOTS_BEFORE_WARN + " slots (set to " + newSize + " slots), this may result in graphical glitches in the GUI.");
		this.inventoryItemHandler.setSize(newSize);
	}
	
	/*
	 * AI METHODS
	 */
	
	@Override
	protected Brain.Provider<NPCEntity> brainProvider() {
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
		
		ActivityStatus status;
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.ACTIVITY_STATUS.get())) {
			status = ActivityStatus.NO_ACTIVITY;
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
		
		brain.setMemory(MemoryModuleType.HOME, GlobalPos.of(this.level.dimension(), new BlockPos(10, 55, 10)));
	}
	
	@Override
	protected void customServerAiStep() {
		Brain<NPCEntity> brain = this.getBrain();
		
		brain.tick((ServerLevel) this.level, this);
		
		if (this.level.getGameTime() % 20 == 0) {
			CNPCBrainDataSyncMessage msg = new CNPCBrainDataSyncMessage(this.getId(), brain.getMemory(MemoryModuleTypeInit.COMPLAINT.get()).orElse(NPCComplaintInit.CLEAR.get())); 
			IWNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), msg);
		}
		
		if (brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get())) {
			int cooldown = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get()).get();
			cooldown -= 1;
			if (cooldown <= 0) {
				brain.eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get());
			} else {
				brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), cooldown);
			}
		}
		
		if (brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
			ItemEntity item = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
			if (item == null || item.isRemoved() || item.getItem().isEmpty()) {
				brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
			}
		}
		
		super.customServerAiStep();
	}
	
	protected void tickInventory() {
		for (int i = 0; i < this.inventoryItemHandler.getSlots(); ++i) {
			if (!this.inventoryItemHandler.getStackInSlot(i).isEmpty()) {
				this.inventoryItemHandler.getStackInSlot(i).inventoryTick(this.level, this, i, false);
			}
		}
		
		for (int i = 0; i < this.equipmentItemHandler.getSlots(); ++i) {
			if (!this.equipmentItemHandler.getStackInSlot(i).isEmpty()) {
				this.equipmentItemHandler.getStackInSlot(i).inventoryTick(this.level, this, i, i == EquipmentItemHandler.MAINHAND_INDEX);
			}
		}
	}
	
	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return POSES.containsKey(pose) ? POSES.get(pose) : super.getDimensions(pose);
	}
	
	/*
	 * FORMATION METHODS
	 */
	
	@Override
	public int getFormationRank() {
		NPCCombatSkill skill = this.getDataHandler().map(INPCData::getCombatSkill).orElse(NPCCombatSkillInit.UNTRAINED.get());
		if (skill == NPCCombatSkillInit.UNTRAINED.get()) return 0;
		
		boolean isRanged = this.canUseRangedWeapon(this.getMainHandItem());
		return isRanged ? 0 : 0;
	}
	
	@Override
	public boolean isLowLevelUnit() {
		return true;
	}
	
	/*
	 * INTERFACING METHODS
	 */
	
	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand handIn) {
		InteractionResult actionResultType = this.checkAndHandleImportantInteractions(player, handIn);
		if (actionResultType.consumesAction()) {
			return actionResultType;
		} else {
			if (!this.level.isClientSide && player instanceof ServerPlayer) {
				MenuConstructor containerProvider = NPCMenu.getServerContainerProvider(this);
				MenuProvider namedProvider = new SimpleMenuProvider(containerProvider, this.getCustomName());
				NetworkHooks.openGui((ServerPlayer) player, namedProvider, buf -> {
					buf.writeVarInt(this.inventoryItemHandler.getSlots());
					buf.writeBoolean(this.getDataHandler().map(INPCData::canWearEquipment).orElse(false));
					// TODO: write additional npc info
				});
				return InteractionResult.SUCCESS;
			}
			return super.mobInteract(player, handIn);
		}
	}
	
	protected InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand handIn) {
		ItemStack handStack = player.getItemInHand(handIn);
		return handStack.interactLivingEntity(player, this, handIn);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SOUL_ESCAPE;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		
		tag.put(TAG_WORKSTUFFS, this.equipmentItemHandler.serializeNBT());
		tag.put(TAG_INVENTORY, this.inventoryItemHandler.serializeNBT());
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		
		this.equipmentItemHandler.deserializeNBT(tag);
		this.inventoryItemHandler.deserializeNBT(tag.getCompound(TAG_INVENTORY));
	}
	
	@Override
	public void tick() {
		super.tick();
		if (!this.level.isClientSide) {
			this.tickInventory();
		}
	}
	
	@Override
	public PlayerIDTag getDiplomaticOwner() {
		return this.getDataHandler().map(INPCData::getOwner).orElse(PlayerIDTag.NO_OWNER);
	}
	
	/*
	 * RANGED ATTACK METHODS
	 */
	
	@Override
	public ItemStack getProjectile(ItemStack weapon) {
		Item weaponItem = weapon.getItem();
		Predicate<ItemStack> predicate = s -> false;
		
		if (weaponItem instanceof ProjectileWeaponItem) {
			predicate = predicate.or(((ProjectileWeaponItem) weaponItem).getAllSupportedProjectiles());
		}
		if (weaponItem instanceof ISpeedloadable && ((ISpeedloadable) weaponItem).canSpeedload(weapon)) {
			predicate = predicate.or(((ISpeedloadable) weaponItem).getSpeedloaderPredicate());
		}
		
		return this.getMatching(predicate);
	}
	
	@Override
	public ItemStack getMatching(Predicate<ItemStack> predicate) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack handstack = this.getItemInHand(hand);
			if (predicate.test(handstack)) return handstack;
		}
		
		return ModInventoryUtils.iterateAndApplyIf(
				this.inventoryItemHandler,
				(handler, index) -> iterateDeeperIfContainer(
						handler,
						index,
						(h, i) -> h.getStackInSlot(i),
						predicate,
						predicate,
						() -> ItemStack.EMPTY,
						(h, i) -> h.getStackInSlot(i)),
				predicate.or(s -> s.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()),
				predicate,
				() -> ItemStack.EMPTY);
	}
	
	@Override
	public boolean has(Predicate<ItemStack> predicate) {
		for (InteractionHand hand : InteractionHand.values()) {
			if (predicate.test(this.getItemInHand(hand))) return true;
		}
		
		return ModInventoryUtils.iterateAndApplyIf(
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
	
	@Override
	public boolean wantsToPickUp(ItemStack stack) {
		return true;
	}
	
	@Override
	protected void pickUpItem(ItemEntity entity) {
		super.pickUpItem(entity);
		if (!this.level.isClientSide) {
			ItemStack stack = entity.getItem();
			if (entity.isRemoved() || stack.isEmpty() || entity.hasPickUpDelay()) return;
			int sz = stack.getCount();
			for (int i = 0; i < this.inventoryItemHandler.getSlots(); ++i) {
				stack = this.inventoryItemHandler.insertItem(i, stack, false);
				if (stack.isEmpty()) {
					break;
				}
			}
			if (stack.getCount() < sz) {
				this.onItemPickup(entity);
				this.take(entity, sz);
				if (stack.isEmpty()) {
					entity.discard();
					stack.setCount(sz);
				}
			} else {
				this.brain.setMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), true);
			}
		}
	}
	
	@Override
	public void onItemPickup(ItemEntity item) {
		super.onItemPickup(item);
		this.equipmentItemHandler.update();
		if (this.brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)
			&& this.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get().equals(item)) {
			this.brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
		}
	}
	
	@Override
	public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon) {
		return this.canUseRangedWeapon(new ItemStack(weapon));
	}
	
	@Override
	public void startReloading() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		this.getNextEffectiveness();
		this.getNextTimeModifier();
		
		if (weaponItem instanceof BowItem || weaponItem instanceof CrossbowItem) {
			this.startUsingItem(InteractionHand.MAIN_HAND);
		} else if (weaponItem instanceof FirearmItem) {
			FirearmItem.tryReloadFirearm(weapon, this);
		}
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
		
		if (weaponItem instanceof FirearmItem) {
			return FirearmItem.getDataHandler(weapon).map(h -> {
				FirearmItem.ActionType action = h.getAction();
				
				boolean reloading = action == FirearmItem.ActionType.RELOADING || action == FirearmItem.ActionType.START_RELOADING;
				if (!h.hasAmmo() && !reloading) {
					FirearmItem.tryReloadFirearm(weapon, this);
					return true;
				}
				
				return (reloading || action == FirearmItem.ActionType.CYCLING) && !h.isFinishedAction();
			}).orElse(false);
		}
		
		return false;
	}
	
	@Override
	public boolean whileWaitingToAttack() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		if (weaponItem instanceof FirearmItem) {
			if (!FirearmItem.isCycled(weapon) && ((FirearmItem) weaponItem).needsCycle(weapon)) {
				this.actionDelay = 0;
				if (FirearmItem.isFinishedAction(weapon)) {
					this.swing(InteractionHand.MAIN_HAND);
				}
				return true;
			}
			
			if (!FirearmItem.isFinishedAction(weapon)) {
				this.actionDelay = 0;
				boolean flag = this.brain.getMemory(MemoryModuleTypeInit.CAN_ATTACK.get()).orElse(false);
				this.brain.setMemory(MemoryModuleTypeInit.CAN_ATTACK.get(), flag);
				return true;
			}
			
			if (this.actionDelay <= 0) {
				this.actionDelay = Mth.ceil(30.0f * this.timeModifier);
			}
			if (this.canAim() && !FirearmItem.isAiming(weapon)) {
				((FirearmItem) weaponItem).startAiming(weapon, this);
				this.startUsingItem(InteractionHand.MAIN_HAND);
				this.actionDelay += 10;
			}
		} else if (this.actionDelay <= 0) {
			this.actionDelay = Mth.ceil(60.0f * this.timeModifier);
		}
		
		--this.actionDelay;
		return this.actionDelay > 0;
	}
	
	private boolean canAim() {
		return true;
	}
	
	@Override
	public void performRangedAttack(LivingEntity target, float power) {
		if (target == null) return;
		
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		this.getNextEffectiveness();
		this.getNextTimeModifier();
		
		if (weaponItem instanceof BowItem) {
			Vec3 targetPos = target.position().add(0.0d, target.getBbHeight() * ONE_THIRD, 0.0d);
			this.shootUsingBow(targetPos);
			weapon.hurtAndBreak(1, this, npc -> this.broadcastBreakEvent(this.getUsedItemHand()));
			this.stopUsingItem();
		} else if (weaponItem instanceof CrossbowItem) {
			this.shootUsingCrossbow(target);
			CrossbowItem.setCharged(weapon, false);
		} else if (weaponItem instanceof FirearmItem) {
			this.shootUsingFirearm(target);
			if (this.brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())) {
				if (!this.brain.getMemory(MemoryModuleTypeInit.CAN_ATTACK.get()).orElse(true)) {
					this.brain.setMemory(MemoryModuleTypeInit.FINISHED_ATTACKING.get(), true);
				}
				this.brain.eraseMemory(MemoryModuleTypeInit.CAN_ATTACK.get());
			}
		}
	}
	
	@Override
	public void performRangedAttack(Position target, float damage) {
		if (target == null) return;
		
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		this.getNextEffectiveness();
		this.getNextTimeModifier();
		
		if (weaponItem instanceof BowItem) {
			this.shootUsingBow(target);
			weapon.hurtAndBreak(1, this, npc -> this.broadcastBreakEvent(this.getUsedItemHand()));
			this.stopUsingItem();
		} else if (weaponItem instanceof CrossbowItem) {
			this.shootUsingCrossbow(null);
			CrossbowItem.setCharged(weapon, false);
		} else if (weaponItem instanceof FirearmItem) {
			this.shootUsingFirearm(null);
			if (this.brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())) {
				if (!this.brain.getMemory(MemoryModuleTypeInit.CAN_ATTACK.get()).orElse(true)) {
					this.brain.setMemory(MemoryModuleTypeInit.FINISHED_ATTACKING.get(), true);
				}
				this.brain.eraseMemory(MemoryModuleTypeInit.CAN_ATTACK.get());
			}
		}
	}
	
	private static final double ONE_THIRD = 1.0d / 3.0d;
	private static final float ARROW_SPEED = 1.6f;
	
	/**
	 * Code based on {@link net.minecraft.entity.monster.AbstractSkeleton#performRangedAttack AbstractSkeleton#performRangedAttack}
	 */
	private void shootUsingBow(Position target) {
		ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, BowItem.class::isInstance));
		ItemStack projectile = this.getProjectile(bow).split(1);
		AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectile, BowItem.getPowerForTime(this.getTicksUsingItem()));
		Item mainhandItem = this.getMainHandItem().getItem();
		if (mainhandItem instanceof BowItem) {
			arrow = ((BowItem) mainhandItem).customArrow(arrow);
		}
		
		double dx = target.x() - this.getX();
		double dz = target.z() - this.getZ();
		double dyOffset = Math.sqrt(dx * dx + dz * dz);
		double dy = target.y() - arrow.getY() + dyOffset * 0.2d;
		float spread = 14.0f - 12.0f * this.effectiveness;
		arrow.shoot(dx, dy, dz, ARROW_SPEED, spread);
		
		this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
		
		this.level.addFreshEntity(arrow);
	}
	
	/**
	 * Code based on {@link net.minecraft.entity.ICrossbowUser#performCrossbowAttack ICrossbowUser#performCrossbowAttack}
	 */
	private void shootUsingCrossbow(LivingEntity target) {
		InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof CrossbowItem);
		ItemStack crossbow = this.getItemInHand(hand);
		if (this.isHolding(CrossbowItem.class::isInstance)) {
			float spread = 14.0f - 12.0f * this.effectiveness;
			CrossbowItem.performShooting(this.level, this, hand, crossbow, ARROW_SPEED, spread);
		}
	}
	
	private void shootUsingFirearm(LivingEntity target) {
		this.swing(InteractionHand.MAIN_HAND);
	}
	
	@Override
	public IWeaponRangedAttackMob.ShootingStatus getNextStatus() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		boolean hasAttackTarget = this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
		
		if (weaponItem instanceof BowItem && !hasAttackTarget) return ShootingStatus.READY_TO_FIRE;
		
		if (weaponItem instanceof CrossbowItem) {
			return CrossbowItem.isCharged(weapon) && CrossbowItem.containsChargedProjectile(weapon, weaponItem) ? ShootingStatus.READY_TO_FIRE : ShootingStatus.UNLOADED;
		}
		
		if (weaponItem instanceof FirearmItem) {
			return FirearmItem.getDataHandler(weapon).map(h -> {
				ActionType action = h.getAction();
				switch (action) {
				case START_RELOADING: return ShootingStatus.RELOADING;
				case RELOADING: return ShootingStatus.RELOADING;
				case CYCLING: return ShootingStatus.CYCLING;
				case NOTHING:
					if (h.hasAmmo()) {
						return ((FirearmItem) weaponItem).needsCycle(weapon) && h.isFired() ? ShootingStatus.CYCLING : ShootingStatus.READY_TO_FIRE;
					}
					if (h.isAiming() || !h.isFinishedAction()) {
						return ShootingStatus.FIRED;
					}
					return ShootingStatus.RELOADING;
				default: return ShootingStatus.FIRED;
				}
			}).orElse(ShootingStatus.FIRED);
		}
			
		return ShootingStatus.RELOADING;
	}
	
	@Override
	public boolean whileCoolingDown() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		if (weaponItem instanceof FirearmItem) {
			if (FirearmItem.isFinishedAction(weapon)) {
				if (FirearmItem.isAiming(weapon)) {
					((FirearmItem) weaponItem).stopAiming(weapon, this);
					this.stopUsingItem();
					this.actionDelay = 11;
				}
			} else {
				return true;
			}
			if (this.actionDelay <= 0) {
				brain.setMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(), true);
				return false;
			}
			--this.actionDelay;
			return true;
		}

		return false;
	}
	
	@Override
	public void startCycling() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		this.getNextEffectiveness();
		this.getNextTimeModifier();
		
		if (weaponItem instanceof FirearmItem) {
			this.swing(InteractionHand.MAIN_HAND);
		}
	}
	
	@Override
	public boolean whileCycling() {
		ItemStack weapon = this.getMainHandItem();
		Item weaponItem = weapon.getItem();
		
		if (weaponItem instanceof FirearmItem) {
			return FirearmItem.getDataHandler(weapon).map(h -> {
				FirearmItem.ActionType action = h.getAction();
				
				if (!h.isCycled() && action != FirearmItem.ActionType.CYCLING) {
					this.swing(InteractionHand.MAIN_HAND);
					return true;
				}
				
				return action == FirearmItem.ActionType.CYCLING && !h.isFinishedAction();
			}).orElse(false);
		}
		
		return false;
	}
	
	@Override
	public boolean canDoRangedAttack() {
		ItemStack weapon = this.getMainHandItem();
		if (!this.canUseRangedWeapon(weapon)) return false;
		
		Item weaponItem = weapon.getItem();
		return weaponItem instanceof ProjectileWeaponItem && this.has(((ProjectileWeaponItem) weaponItem).getAllSupportedProjectiles());
	}
	
	@Override
	public LivingEntity getTarget() {
		Brain<?> brain = this.getBrain();
		return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? brain.getMemory(MemoryModuleType.ATTACK_TARGET).get() : null;
	}
	
	@Override
	public void stopRangedAttack() {
		if (this.isUsingItem()) {
			this.stopUsingItem();
		}
		
		IWeaponRangedAttackMob.ShootingStatus status = this.getNextStatus();
		switch (status) {
		case CYCLING: this.startCycling(); break;
		case RELOADING: this.startReloading(); break;
		default: break;
		}
	}
	
	@Override
	public float getEffectiveness() {
		return this.effectiveness;
	}
	
	@Override
	public float getTimeModifier() {
		return this.timeModifier;
	}
	
	public boolean canUseRangedWeapon(ItemStack weapon) {
		return this.getDataHandler()
				.map(INPCData::getCombatSkill)
				.map(cs -> cs.canUseRangedWeapon(this, weapon))
				.orElse(false);
	}
	
	private float getNextEffectiveness() {
		this.effectiveness = this.getDataHandler()
				.map(INPCData::getCombatSkill)
				.map(cs -> cs.getEffectiveness(this))
				.orElse(this.getRandom().nextFloat());
		return this.effectiveness;
	}
	
	private float getNextTimeModifier() {
		this.timeModifier = this.getDataHandler()
				.map(INPCData::getCombatSkill)
				.map(cs -> cs.getTimeModifier(this))
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
				? ModInventoryUtils.iterateAndApplyIf(
						lzop.resolve().get(),
						containerFunction,
						containerPredicate,
						containerResultPredicate,
						orElseInContainer)
				: orElse.apply(handler, index);
	}
	
}
