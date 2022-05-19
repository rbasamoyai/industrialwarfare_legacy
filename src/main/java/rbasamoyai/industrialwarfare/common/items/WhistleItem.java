package rbasamoyai.industrialwarfare.common.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.rendering.SelectionRendering;
import rbasamoyai.industrialwarfare.common.containers.WhistleContainer;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IHasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitClusterFinder;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.common.entityai.navigation.PosWrapper;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class WhistleItem extends Item implements
		IHighlighterItem,
		IItemWithScreen,
		IRenderOverlay {

	public static final String TAG_SELECTED_UNITS = "selectedUnits";
	public static final String TAG_TICKS_TO_UPDATE = "ticksToUpdate";
	public static final String TAG_CURRENT_MODE = "currentMode";
	public static final String TAG_CONTROLLED_LEADERS = "controlledLeaders";
	public static final String TAG_COMMAND_GROUP = "commandGroup";
	public static final String TAG_FORMATION_TYPE = "formationType";
	public static final String TAG_DIRTY = "dirty";
	public static final String TAG_FORMATION_CATEGORIES = "categories";
	public static final String TAG_INTERVAL = "interval";
	public static final String TAG_CATEGORY_TYPE = "categoryType";
	public static final String TAG_ATTACK_TYPE = "attackType";
	public static final String TAG_UPDATE_FORMATION = "updateFormation";
	
	private static final int MAX_SELECTABLE_UNITS = 128;
	private static final String TRANSLATION_TEXT_KEY = "gui." + IndustrialWarfare.MOD_ID + ".text.";
	private static final ITextComponent CANNOT_SELECT_FULL = new TranslationTextComponent(TRANSLATION_TEXT_KEY + "cannot_select_full", MAX_SELECTABLE_UNITS).withStyle(TextFormatting.RED);
	
	private static final AttributeModifier REACH_MODIFIER = new AttributeModifier(UUID.fromString("c31ab93e-802d-435c-b386-84610ebcfd74"), "industrialwarfare.item.whistle.reach_modifier", 16, AttributeModifier.Operation.ADDITION);
	
	private static final Random RNG = new Random();
	
	public WhistleItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_GENERAL));
	}
	
	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			player.playSound(SoundEvents.ANVIL_PLACE, 1.0f, 1.0f);
			return ActionResult.pass(stack);
		}
		ServerWorld slevel = (ServerWorld) level;
		
		this.stopUnits(slevel, stack, player);
		
		return ActionResult.consume(stack);
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext useContext) {
		PlayerEntity player = useContext.getPlayer();
		World level = useContext.getLevel();
		if (level.isClientSide) {
			player.playSound(SoundEvents.WOOL_PLACE, 1.0f, 0.0f);
			return ActionResultType.SUCCESS;
		}
		ServerWorld slevel = (ServerWorld) level;
		
		ItemStack stack = useContext.getItemInHand();
		CompoundNBT nbt = stack.getOrCreateTag();
		
		if (!nbt.contains(TAG_SELECTED_UNITS, Constants.NBT.TAG_LIST)) return ActionResultType.CONSUME;
		ListNBT unitUUIDs = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		if (unitUUIDs.isEmpty()) return ActionResultType.CONSUME;
		
		Vector3d precisePos = useContext.getClickLocation();
		BlockPos blockPos = useContext.getClickedPos();
		boolean shouldBePrecise = useContext.getClickedFace() == Direction.UP;
		GlobalPos globPos = GlobalPos.of(slevel.dimension(), blockPos);
		
		ListNBT leaderUUIDs = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		
		if (!nbt.contains(TAG_COMMAND_GROUP)) {
			nbt.putUUID(TAG_COMMAND_GROUP, MathHelper.createInsecureUUID(RNG));
		}
		UUID commandGroup = nbt.getUUID(TAG_COMMAND_GROUP);
		
		// Set new position
		List<FormationLeaderEntity> leaders =
				leaderUUIDs
				.stream()
				.map(NBTUtil::loadUUID)
				.map(slevel::getEntity)
				.filter(WhistleItem::isValidLeader)
				.map(FormationLeaderEntity.class::cast)
				.collect(Collectors.toList());
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		
		List<CreatureEntity> unitsToFormUp =
				unitUUIDs
				.stream()
				.map(NBTUtil::loadUUID)
				.map(slevel::getEntity)
				.filter(e -> isValidUnit(e, owner))
				.map(CreatureEntity.class::cast)
				.collect(Collectors.toList());
		
		for (CreatureEntity unit : unitsToFormUp) {
			Brain<?> brain = unit.getBrain();
			if (brain.checkMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.REGISTERED)) {
				brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
			}
		}
		
		List<CreatureEntity> unitsToMove = this.formUpEntities(unitsToFormUp, stack, player, commandGroup, leaders);
		
		leaderUUIDs.clear();
		
		for (CreatureEntity unit : unitsToMove) {
			Brain<?> brain = unit.getBrain();
			if (!checkMemoryForMovement(brain)) continue;
			brain.setMemory(MemoryModuleType.MEETING_POINT, globPos);
			
			if (unit.position().closerThan(precisePos, 0.5d)) {
				unit.getLookControl().setLookAt(precisePos);
			} else if (shouldBePrecise) {
				brain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);			
			}
			
			if (brain.checkMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.REGISTERED)) {
				brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
			}
			
			brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
			brain.eraseMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get());
			brain.setMemory(MemoryModuleType.MEETING_POINT, globPos);
			if (brain.hasMemoryValue(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get())) {
				brain.eraseMemory(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get());
			}
			
			if (unit instanceof FormationLeaderEntity) {
				leaderUUIDs.add(NBTUtil.createUUID(unit.getUUID()));
				((FormationLeaderEntity) unit).updateOrderTime();
			}
		}
		nbt.put(TAG_CONTROLLED_LEADERS, leaderUUIDs);
		
		return ActionResultType.CONSUME;
	}
	
	private UnitFormation getNewFormation(ItemStack stack, int rank) {
		CompoundNBT nbt = stack.getOrCreateTag();	
		if (!nbt.contains(TAG_FORMATION_TYPE, Constants.NBT.TAG_STRING)) {
			nbt.putString(TAG_FORMATION_TYPE, UnitFormationTypeInit.LINE_10W3D.get().getRegistryName().toString());
		}
		ResourceLocation typeLoc = new ResourceLocation(nbt.getString(TAG_FORMATION_TYPE));
		UnitFormationType<?> type = IWModRegistries.UNIT_FORMATION_TYPES.getValue(typeLoc);
		return type.getFormation(rank);
	}
	
	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity.isDeadOrDying()) return ActionResultType.FAIL;
		if (player.level.isClientSide) {
			player.playSound(SoundEvents.NOTE_BLOCK_SNARE, 1.0f, 0.0f);
			return ActionResultType.SUCCESS;
		}
		ServerWorld slevel = (ServerWorld) player.level;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		if (!nbt.contains(TAG_SELECTED_UNITS, Constants.NBT.TAG_LIST)) {
			nbt.put(TAG_SELECTED_UNITS, new ListNBT());
		}
		ListNBT unitUUIDs = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		
		if (!nbt.contains(TAG_COMMAND_GROUP)) {
			nbt.putUUID(TAG_COMMAND_GROUP, MathHelper.createInsecureUUID(RNG));
		}
		UUID commandGroup = nbt.getUUID(TAG_COMMAND_GROUP);
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		if (isValidUnit(entity, owner)) {
			UUID uuid = entity.getUUID();
			for (int i = 0; i < unitUUIDs.size(); ++i) {
				if (!uuid.equals(NBTUtil.loadUUID(unitUUIDs.get(i)))) continue;
				unitUUIDs.remove(i);
				Brain<?> unitBrain = entity.getBrain();
				if (unitBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
					&& unitBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(commandGroup)) {
					unitBrain.eraseMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get());
				}
				return ActionResultType.CONSUME;
			}
			if (unitUUIDs.size() >= MAX_SELECTABLE_UNITS) {
				player.displayClientMessage(CANNOT_SELECT_FULL, true);
				return ActionResultType.CONSUME;
			}
			unitUUIDs.add(NBTUtil.createUUID(uuid));
			return ActionResultType.CONSUME;
		}
		
		CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
		if (unitUUIDs.isEmpty() || mode == CombatMode.DONT_ATTACK) return ActionResultType.CONSUME;
		
		// Unlike passive targeting, directly targeting a unit will not take
		// anything into consideration other than if they are an ally.
		if (entity instanceof IHasDiplomaticOwner) {
			PlayerIDTag targetOwner = ((IHasDiplomaticOwner) entity).getDiplomaticOwner();
			if (owner.equals(targetOwner)) return ActionResultType.PASS;
			DiplomacySaveData saveData = DiplomacySaveData.get(slevel);
			DiplomaticStatus status = saveData.getDiplomaticStatus(owner, targetOwner);
			if (status == DiplomaticStatus.ALLY) return ActionResultType.CONSUME;
		}
		
		ListNBT leaderUUIDs = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		
		List<FormationLeaderEntity> leaders =
				leaderUUIDs
				.stream()
				.map(NBTUtil::loadUUID)
				.map(slevel::getEntity)
				.filter(WhistleItem::isValidLeader)
				.map(FormationLeaderEntity.class::cast)
				.collect(Collectors.toList());
		
		List<CreatureEntity> unitsToFormUp =
				unitUUIDs
				.stream()
				.map(NBTUtil::loadUUID)
				.map(slevel::getEntity)
				.filter(e -> isValidUnit(e, owner))
				.map(CreatureEntity.class::cast)
				.collect(Collectors.toList());
		
		for (CreatureEntity unit : unitsToFormUp) {
			Brain<?> brain = unit.getBrain();
			if (brain.checkMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.REGISTERED)) {
				brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
			}
		}
		
		List<CreatureEntity> unitsToMove = this.formUpEntities(unitsToFormUp, stack, player, commandGroup, leaders);
		
		leaderUUIDs.clear();
		
		for (CreatureEntity unit : unitsToMove) {
			Brain<?> brain = unit.getBrain();
			if (!brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)) {
				continue;
			}
			
			brain.setMemory(MemoryModuleType.ATTACK_TARGET, entity);
			brain.eraseMemory(MemoryModuleType.MEETING_POINT);
			brain.eraseMemory(MemoryModuleType.WALK_TARGET);
			
			if (brain.checkMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), MemoryModuleStatus.REGISTERED)) {
				brain.eraseMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get());
			}
			if (brain.hasMemoryValue(MemoryModuleTypeInit.PRECISE_POS.get())) {
				brain.eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
			}
			if (brain.hasMemoryValue(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get())) {
				brain.eraseMemory(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get());
			}
			
			if (unit instanceof FormationLeaderEntity) {
				leaderUUIDs.add(NBTUtil.createUUID(unit.getUUID()));
				((FormationLeaderEntity) unit).updateOrderTime();
			}
		}
		nbt.put(TAG_CONTROLLED_LEADERS, leaderUUIDs);
		
		return ActionResultType.CONSUME;
	}
	
	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		CompoundNBT nbt = stack.getOrCreateTag();
		
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			if (player.getCooldowns().isOnCooldown(this)) return true;
			
			if (player.isCrouching()) {
				if (player.level.isClientSide) {
					player.playSound(SoundEvents.NOTE_BLOCK_BASS, 1.0f, 0.0f);
					return true;
				}
				nbt.remove(TAG_SELECTED_UNITS);
				this.stopUnits((ServerWorld) player.level, stack, player);
				player.getCooldowns().addCooldown(this, 10);
			}
		}
		
		return true;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
		if (level.isClientSide || !(entity instanceof PlayerEntity)) return;
		ServerWorld slevel = (ServerWorld) level;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		int t = nbt.getInt(TAG_TICKS_TO_UPDATE);
		boolean dirty = nbt.getBoolean(TAG_DIRTY);
		if (++t >= 20 || dirty) {
			t = 0;
			nbt.remove(TAG_DIRTY);
			
			this.removeUnits(slevel, stack, (PlayerEntity) entity);
			if (dirty) {
				
			}
			
			
		}
		nbt.putInt(TAG_TICKS_TO_UPDATE, t);
	}
	
	public void stopUnits(ServerWorld level, ItemStack stack, PlayerEntity player) {
		CompoundNBT nbt = stack.getOrCreateTag();
		PlayerIDTag owner = PlayerIDTag.of(player);
		ListNBT selectedUnits = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : selectedUnits) {
			Entity e = level.getEntity(NBTUtil.loadUUID(tag));
			if (!isValidUnit(e, owner)) continue;
			CreatureEntity unit = (CreatureEntity) e;
			
			Brain<?> brain = unit.getBrain();
			if (brain.hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
				brain.eraseMemory(MemoryModuleType.MEETING_POINT);
			}
			if (brain.hasMemoryValue(MemoryModuleTypeInit.PRECISE_POS.get())) {
				brain.eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
			}
			if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
				brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
			}
			if (brain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) {
				brain.eraseMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get());
			}
			if (brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())) {
				brain.eraseMemory(MemoryModuleTypeInit.IN_FORMATION.get());
			}
		}
		
		nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY)
		.stream()
		.map(NBTUtil::loadUUID)
		.map(level::getEntity)
		.forEach(Entity::kill);
		
		player.getCooldowns().addCooldown(this, 10);
	}
	
	public void removeUnits(ServerWorld level, ItemStack stack, PlayerEntity player) {
		CompoundNBT nbt = stack.getOrCreateTag();
		PlayerIDTag owner = new PlayerIDTag(player.getUUID(), true);
		ListNBT unitUUIDs = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		nbt.put(TAG_SELECTED_UNITS,
				unitUUIDs
				.stream()
				.map(NBTUtil::loadUUID)
				.map(level::getEntity)
				.filter(e -> isValidUnit(e, owner))
				.map(Entity::getUUID)
				.map(NBTUtil::createUUID)
				.collect(Collectors.toCollection(ListNBT::new)));
		
		ListNBT leaderUUIDs = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		nbt.put(TAG_CONTROLLED_LEADERS,
				leaderUUIDs
				.stream()
				.map(NBTUtil::loadUUID)
				.map(level::getEntity)
				.filter(WhistleItem::isValidLeader)
				.map(Entity::getUUID)
				.map(NBTUtil::createUUID)
				.collect(Collectors.toCollection(ListNBT::new)));
	}
	
	public void updateStance(ServerWorld level, ItemStack stack, PlayerEntity player) {
		player.getCooldowns().addCooldown(this, 10);
		
		CompoundNBT nbt = stack.getOrCreateTag();
		
		CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
		Interval interval = Interval.fromId(nbt.getInt(TAG_INTERVAL));
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		if (!nbt.contains(TAG_COMMAND_GROUP)) {
			nbt.putUUID(TAG_COMMAND_GROUP, MathHelper.createInsecureUUID(RNG));
		}
		UUID commandGroup = nbt.getUUID(TAG_COMMAND_GROUP);
		
		ListNBT unitUUIDs = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : unitUUIDs) {
			UUID uuid = NBTUtil.loadUUID(tag);
			Entity e = level.getEntity(uuid);
			if (!isValidUnit(e, owner)) continue;
			CreatureEntity unit = (CreatureEntity) e;
			Brain<?> brain = unit.getBrain();
			if (!checkMemoryForAction(brain)) continue;
			brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), mode == CombatMode.DONT_ATTACK ? ActivityStatus.NO_ACTIVITY : ActivityStatus.FIGHTING);
			brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
			brain.setActiveActivityIfPossible(mode == CombatMode.DONT_ATTACK ? Activity.IDLE : Activity.FIGHT);
		}
		
		UnitFormationType<?> currentFormation = IWModRegistries.UNIT_FORMATION_TYPES.getValue(new ResourceLocation(nbt.getString(TAG_FORMATION_TYPE)));
		CompoundNBT formationCategories = nbt.getCompound(TAG_FORMATION_CATEGORIES);
		CompoundNBT catTag = formationCategories.getCompound(currentFormation.getCategory().getTag());
		ResourceLocation attackTypeLoc = new ResourceLocation(catTag.getString(TAG_ATTACK_TYPE));
		FormationAttackType attackType = IWModRegistries.FORMATION_ATTACK_TYPES.getValue(attackTypeLoc);
		
		boolean updateFormation = nbt.contains(TAG_UPDATE_FORMATION);
		
		ListNBT leaderUUIDs = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		if (updateFormation) {
			nbt.remove(TAG_UPDATE_FORMATION);
			
			List<FormationLeaderEntity> leaders =
					leaderUUIDs
					.stream()
					.map(NBTUtil::loadUUID)
					.map(level::getEntity)
					.filter(WhistleItem::isValidLeader)
					.map(FormationLeaderEntity.class::cast)
					.collect(Collectors.toList());
			
			List<CreatureEntity> unitsToFormUp =
					unitUUIDs
					.stream()
					.map(NBTUtil::loadUUID)
					.map(level::getEntity)
					.filter(e -> isValidUnit(e, owner))
					.map(CreatureEntity.class::cast)
					.collect(Collectors.toList());
			
			if (unitsToFormUp.isEmpty()) return;
			
			for (CreatureEntity unit : unitsToFormUp) {
				Brain<?> brain = unit.getBrain();
				if (brain.checkMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.REGISTERED)) {
					brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
				}
			}
			
			Optional<Vector3d> precisePos = Optional.empty();
			Optional<GlobalPos> targetPos = Optional.empty();
			Optional<LivingEntity> attackTarget = Optional.empty();
			Optional<Boolean> engaging = Optional.empty();
			
			if (!leaders.isEmpty()) {
				FormationLeaderEntity leader = leaders.get(0);
				Brain<?> leaderBrain = leader.getBrain();
				
				precisePos = leaderBrain.getMemory(MemoryModuleTypeInit.PRECISE_POS.get());
				attackTarget = leaderBrain.getMemory(MemoryModuleType.ATTACK_TARGET);
				targetPos = leaderBrain.getMemory(MemoryModuleType.MEETING_POINT);
				engaging = leaderBrain.getMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get());
				leaders.forEach(Entity::kill);
				leaders.clear();
			}
			
			leaderUUIDs.clear();
			
			List<CreatureEntity> unitsToMove = this.formUpEntities(unitsToFormUp, stack, player, commandGroup, leaders);
			for (CreatureEntity unit : unitsToMove) {
				Brain<?> brain = unit.getBrain();
				
				if (attackTarget.isPresent()) {
					if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)) {
						brain.setMemory(MemoryModuleType.ATTACK_TARGET, attackTarget);
						if (brain.checkMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), MemoryModuleStatus.REGISTERED)) {
							brain.setMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), engaging);
						}
					}
				} else {
					if (precisePos.isPresent()
						&& unit.position().closerThan(precisePos.get(), 1.5d)
						&& brain.checkMemory(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED)) {
						brain.setMemory(MemoryModuleType.LOOK_TARGET, new PosWrapper(precisePos.get()));
					} else {
						brain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);
					}
					brain.setMemory(MemoryModuleType.MEETING_POINT, targetPos);
				}
				
				if (unit.getType() == EntityTypeInit.FORMATION_LEADER.get()) {
					leaderUUIDs.add(NBTUtil.createUUID(unit.getUUID()));
				}
				brain.eraseMemory(MemoryModuleTypeInit.FINISHED_ATTACKING.get());
			}
			
			nbt.put(TAG_CONTROLLED_LEADERS, leaderUUIDs);
		}
		
		for (INBT tag : leaderUUIDs) {
			Entity e = level.getEntity(NBTUtil.loadUUID(tag));
			if (!(e instanceof FormationLeaderEntity)) continue;
			FormationLeaderEntity leader = (FormationLeaderEntity) e;
			
			Brain<?> brain = leader.getBrain();
			
			leader.setAttackInterval(interval);
			leader.setAttackType(attackType);
			
			if (brain.checkMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryModuleStatus.REGISTERED)) {
				brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
			}
			if (mode == CombatMode.DONT_ATTACK) {
				if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
					brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
				}
				if (brain.hasMemoryValue(MemoryModuleTypeInit.ENGAGING_COMPLETED.get())) {
					brain.eraseMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get());
				}
			}
			brain.setActiveActivityIfPossible(mode == CombatMode.DONT_ATTACK ? Activity.IDLE : Activity.FIGHT);
			leader.updateOrderTime();
		}
	}
	
	private List<CreatureEntity> formUpEntities(List<CreatureEntity> selectedUnits, ItemStack stack,
			PlayerEntity player, UUID commandGroup, List<FormationLeaderEntity> controlledLeaders) {
		ServerWorld slevel = (ServerWorld) player.level;
		PlayerIDTag owner = PlayerIDTag.of(player);
		
		List<CreatureEntity> unitsToMove = new ArrayList<>();
		unitsToMove.addAll(controlledLeaders);
		
		List<CreatureEntity> unitsToCluster = new ArrayList<>();
		
		boolean flag = this.getNewFormation(stack, 0).getType().getCategory() == FormationCategory.NO_FORMATION;
		
		for (Entity e : selectedUnits) {
			if (!isValidUnit(e, owner)) continue;
			CreatureEntity unit = (CreatureEntity) e;
			
			Brain<?> unitBrain = unit.getBrain();
			
			if (flag || !(unit instanceof IMovesInFormation) || !unitBrain.checkMemory(MemoryModuleTypeInit.IN_FORMATION.get(), MemoryModuleStatus.REGISTERED)) {
				unitsToMove.add(unit);
				continue;
			}
			if (unitBrain.getMemory(MemoryModuleTypeInit.IN_FORMATION.get()).map(l -> !controlledLeaders.stream().anyMatch(f -> f.hasMatchingFormationLeader(l))).orElse(true)) {
				unitsToCluster.add(unit);
			}
		}
		
		if (flag) return unitsToMove;
		
		unitsToCluster =
				unitsToCluster
				.stream()
				.map(u -> (CreatureEntity & IMovesInFormation) u)
				.filter(u -> !this.addToNearbyLeadersIfPossible(u, slevel))
				.collect(Collectors.toList());
		
		List<List<CreatureEntity>> spatialClusters = (new UnitClusterFinder(3, 5.0f)).findClusters(unitsToCluster);
		if (spatialClusters.isEmpty()) {
			return selectedUnits;
		}
		unitsToMove.addAll(spatialClusters.remove(spatialClusters.size() - 1));
		
		for (List<CreatureEntity> cluster : spatialClusters) {
			if (cluster.isEmpty()) continue;
			
			Vector3d centroid =
					cluster
					.stream()
					.map(Entity::position)
					.reduce(Vector3d::add)
					.map(v -> v.scale(1.0d / (double) cluster.size()))
					.get();
			
			Vector3d pos =
					cluster
					.stream()
					.map(Entity::position)
					.reduce((a, b) -> centroid.distanceToSqr(a) < centroid.distanceToSqr(b) ? a : b)
					.get();
			
			FormationLeaderEntity leader = this.getNewFormation(stack, 0).spawnInnerFormationLeaders(slevel, pos, commandGroup, owner);
			unitsToMove.add(leader);
			// TODO: unit class map
			
			// Making formations and chaining them
			for (CreatureEntity unit : cluster) {
				IMovesInFormation formationUnit = (IMovesInFormation) unit;
				int formationRank = formationUnit.getFormationRank();
				
				if (!leader.addEntity((CreatureEntity & IMovesInFormation) unit)) {
					FormationLeaderEntity restore = leader;
					leader = this.getNewFormation(stack, formationRank).spawnInnerFormationLeaders(slevel, pos, commandGroup, owner);
					if (leader.addEntity((CreatureEntity & IMovesInFormation) unit)) {
						restore.setFollower(leader);
					} else {
						// Should not happen under any circumstances
						leader.kill();
						leader = restore;
						unitsToMove.add(unit);
					}
				}
			}
		}
		return unitsToMove;
	}
	
	private <E extends CreatureEntity & IMovesInFormation> boolean addToNearbyLeadersIfPossible(E unit, World level) {
		Brain<?> unitBrain = unit.getBrain();
		if (!unitBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) {
			return false;
		}
		UUID unitCommandGroup = unitBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		
		AxisAlignedBB box = unit.getBoundingBox().inflate(16.0d);
		List<FormationLeaderEntity> potentialLeaders = level.getEntitiesOfClass(FormationLeaderEntity.class, box,
				l -> l.isAlive() && l.getBrain().getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).map(unitCommandGroup::equals).orElse(false));
		
		return potentialLeaders.stream().anyMatch(l -> l.addEntity(unit));
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}
	
	@Override
	public boolean canAttackBlock(BlockState state, World level, BlockPos pos, PlayerEntity player) {
		return false;
	}
	
	private static boolean isValidUnit(Entity entity, PlayerIDTag owner) {
		if (entity == null || !(entity instanceof IHasDiplomaticOwner && entity instanceof CreatureEntity)) return false;
		IHasDiplomaticOwner ownedEntity = (IHasDiplomaticOwner) entity;
		return entity.isAlive() && ownedEntity.getDiplomaticOwner().equals(owner);
	}
	
	private static boolean isValidLeader(Entity entity) {
		return entity != null && entity.isAlive() && entity instanceof FormationLeaderEntity;
	}
	
	private static boolean checkMemoryForAction(Brain<?> brain) {
		return brain.checkMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryModuleStatus.REGISTERED);
	}
	
	private static boolean checkMemoryForMovement(Brain<?> brain) {
		return brain.checkMemory(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.PRECISE_POS.get(), MemoryModuleStatus.REGISTERED);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		return slot == EquipmentSlotType.MAINHAND ? ImmutableMultimap.of(ForgeMod.REACH_DISTANCE.get(), REACH_MODIFIER) : super.getAttributeModifiers(slot, stack);
	}
	
	@Override
	public boolean shouldHighlightEntity(ItemStack stack, Entity entity) {
		CompoundNBT nbt = stack.getOrCreateTag();
		ListNBT unitList = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		return unitList.contains(NBTUtil.createUUID(entity.getUUID())); 
	}
	
	@Override
	public void renderHighlight(Entity entity, ItemStack item, MatrixStack matrixstack, IRenderTypeBuffer buf) {
		if (entity.isPassenger()) {
			Entity vehicle = entity.getVehicle();
			matrixstack.pushPose();
			matrixstack.translate(0.0d, vehicle.getY() - entity.getY(), 0.0d);
			SelectionRendering.renderSelectionCircle(vehicle, matrixstack, buf);
			matrixstack.popPose();
		} else {
			SelectionRendering.renderSelectionCircle(entity, matrixstack, buf);
		}
	}
	
	@Override
	public boolean canOpenScreen(ItemStack stack) {
		return true;
	}
	
	private static final ITextComponent WHISTLE_TAB_TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".whistle_tab");
	
	@Override
	public INamedContainerProvider getItemContainerProvider(ItemStack stack) {
		return new SimpleNamedContainerProvider(WhistleContainer.getServerContainerProvider(stack), WHISTLE_TAB_TITLE);
	}
	
	@Override
	public void writeContainerInfo(PacketBuffer buf, ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
			
		ResourceLocation typeLoc = nbt.contains(TAG_FORMATION_TYPE)
				? new ResourceLocation(nbt.getString(TAG_FORMATION_TYPE))
				: UnitFormationTypeInit.LINE_10W3D.get().getRegistryName();
		
		buf
		.writeVarInt(nbt.getInt(TAG_INTERVAL))
		.writeVarInt(nbt.getInt(TAG_CURRENT_MODE))
		.writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES, typeLoc);
		
		CompoundNBT formationCategories = nbt.getCompound(TAG_FORMATION_CATEGORIES);
		buf.writeVarInt(FormationCategory.values().length);
		for (FormationCategory cat : FormationCategory.values()) {
			if (!formationCategories.contains(cat.getTag(), Constants.NBT.TAG_COMPOUND)) {
				CompoundNBT newCatTag = new CompoundNBT();
				newCatTag.putString(TAG_CATEGORY_TYPE, cat.getDefaultType().getRegistryName().toString());
				newCatTag.putString(TAG_ATTACK_TYPE, IWModRegistries.FORMATION_ATTACK_TYPES.getDefaultKey().toString());
				formationCategories.put(cat.getTag(), newCatTag);
			}
			CompoundNBT catTag = formationCategories.getCompound(cat.getTag());
			ResourceLocation catLoc = new ResourceLocation(catTag.getString(TAG_CATEGORY_TYPE));
			ResourceLocation attackTypeLoc = new ResourceLocation(catTag.getString(TAG_ATTACK_TYPE));
			
			buf.writeVarInt(cat.getId());
			buf.writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES, catLoc);
			buf.writeRegistryIdUnsafe(IWModRegistries.FORMATION_ATTACK_TYPES, attackTypeLoc);
		}
	}
	
	private static final ResourceLocation WHISTLE_TAB_LOCATION = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/whistle_grid.png");
	private static final int TAB_WIDTH = 150;
	private static final int TAB_HEIGHT = 104;
	private static final int RIGHT_HAND_TAB_TEX_Y = 0;
	private static final int LEFT_HAND_TAB_TEX_Y = 104;
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderOverlay(MatrixStack stack, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		TextureManager texManager = mc.getTextureManager();
		MainWindow window = mc.getWindow();
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		boolean flag = mc.options.mainHand == HandSide.LEFT;
		
		stack.pushPose();
		
		texManager.bind(WHISTLE_TAB_LOCATION);
		
		int posX = window.getGuiScaledWidth() / 2;
		if (flag) {
			posX -= TAB_WIDTH + 100; 
		} else {
			posX += 100;
		}
		
		int posY = window.getGuiScaledHeight() - 21;// * scale;
		
		int texY = flag ? LEFT_HAND_TAB_TEX_Y : RIGHT_HAND_TAB_TEX_Y;
		mc.gui.blit(stack, posX, posY, 0, texY, TAB_WIDTH, TAB_HEIGHT);
		
		ItemRenderer itemRenderer = mc.getItemRenderer();
		int itemX = posX;
		if (flag) {
			itemX += TAB_WIDTH - 31;
		} else {
			itemX += 15;
		}
		int itemY = window.getGuiScaledHeight() - 17;
		itemRenderer.renderGuiItem(new ItemStack(this), itemX, itemY);
		
		stack.popPose();
	}
	
	public static enum FormationCategory {
		LINE("line", UnitFormationTypeInit.LINE_10W3D::get, 0),
		COLUMN("column", UnitFormationTypeInit.COLUMN_4W10D::get, 1),
		NO_FORMATION("no_formation", UnitFormationTypeInit.NO_FORMATION::get, 2),
		NO_WHISTLE("no_whistle", UnitFormationTypeInit.NO_FORMATION::get, 3);
		
		private final String tag;
		private final Supplier<UnitFormationType<?>> defaultType;
		private final int id;
		
		private static final FormationCategory[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(FormationCategory::getId)).toArray(sz -> new FormationCategory[sz]);
		
		private FormationCategory(String tag, Supplier<UnitFormationType<?>> defaultType, int id) {
			this.tag = tag;
			this.defaultType = defaultType;
			this.id = id;
		}
		
		public String getTag() { return this.tag; }
		public UnitFormationType<?> getDefaultType() { return this.defaultType.get(); }
		public int getId() { return this.id; }
		public static FormationCategory fromId(int id) { return 0 <= id && id < BY_ID.length ? BY_ID[id] : NO_FORMATION; }
		
		@Override public String toString() { return this.getTag(); }
	}
	
	public static enum Interval {
		T_1S(0, 20, "1"),
		T_2S(1, 40, "2"),
		T_3S(2, 60, "3"),
		T_4S(3, 80, "4"),
		T_5S(4, 100, "5"),
		T_10S(5, 200, "10"),
		T_15S(6, 300, "15"),
		T_20S(7, 400, "20"),
		T_30S(8, 600, "30");
		
		private final int id;
		private final int time;
		private final String timeString;
		
		private static final Interval[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Interval::getId)).toArray(sz -> new Interval[sz]);
		
		private Interval(int id, int time, String timeString) {
			this.id = id;
			this.time = time;
			this.timeString = timeString;
		}
		
		public int getTime() { return this.time; }		
		public int getId() { return this.id; }
		public static Interval fromId(int id) { return 0 <= id && id < BY_ID.length ? BY_ID[id] : T_1S; }
		public Interval next() { return fromId(this.id + 1); }
		
		@Override
		public String toString() {
			return this.timeString;
		}
	}
	
}
