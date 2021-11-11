package rbasamoyai.industrialwarfare.common.entities;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
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
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
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

/*
 * Base NPC entity class for rbasamoyai's Industrial Warfare.
 */

public class NPCEntity extends CreatureEntity implements IRangedAttackMob {
	
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

	public NPCEntity(EntityType<? extends NPCEntity> type, World worldIn) {
		this(type, worldIn, NPCProfessionInit.JOBLESS.get(), null, 5, false);
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

	@Override
	public void performRangedAttack(LivingEntity target, float damage) {
		
	}
	
}
