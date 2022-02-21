package rbasamoyai.industrialwarfare.common.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
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
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IHasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.NPCActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitClusterFinder;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.LineFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class WhistleItem extends Item implements IHighlighterItem {

	private static final String TAG_SELECTED_UNITS = "selectedUnits";
	private static final String TAG_TICKS_TO_UPDATE = "ticksToUpdate";
	private static final String TAG_CURRENT_MODE = "currentMode";
	private static final String TAG_CONTROLLED_LEADERS = "controlledLeaders";
	
	private static final int MAX_SELECTABLE_UNITS = 64;
	private static final String TRANSLATION_TEXT_KEY = "gui." + IndustrialWarfare.MOD_ID + ".text.";
	private static final ITextComponent CANNOT_SELECT_FULL = new TranslationTextComponent(TRANSLATION_TEXT_KEY + "cannot_select_full", MAX_SELECTABLE_UNITS).withStyle(TextFormatting.RED);
	private static final ITextComponent STOPPED_UNITS = new TranslationTextComponent(TRANSLATION_TEXT_KEY + "stopped_units");
	private static final String COMBAT_MODE_KEY = TRANSLATION_TEXT_KEY + "combat_mode.";
	
	private static final AttributeModifier REACH_MODIFIER = new AttributeModifier(UUID.fromString("c31ab93e-802d-435c-b386-84610ebcfd74"), "Reach modifier", 16, AttributeModifier.Operation.ADDITION);
	
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
		
		CompoundNBT nbt = stack.getOrCreateTag();
		PlayerIDTag owner = PlayerIDTag.of(player);
		ListNBT selectedUnits = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : selectedUnits) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(tag));
			if (!isValidUnit(e, owner)) continue;
			CreatureEntity unit = (CreatureEntity) e;
			
			Brain<?> brain = unit.getBrain();
			if (checkMemoryForMovement(brain)) {
				brain.eraseMemory(MemoryModuleType.MEETING_POINT);
				brain.eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
			}
			if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)) {
				brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
			}
		}
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : controlledLeaders) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(tag));
			if (e != null) e.kill();
		}
		
		player.displayClientMessage(STOPPED_UNITS, true);
		
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
		
		if (!nbt.contains(TAG_SELECTED_UNITS, Constants.NBT.TAG_LIST)) return ActionResultType.FAIL;
		ListNBT selectedUnitsUUIDs = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		if (selectedUnitsUUIDs.isEmpty()) return ActionResultType.PASS;
		PlayerIDTag owner = PlayerIDTag.of(player);
		
		List<CreatureEntity> selectedUnits = new ArrayList<>();
		for (int i = 0; i < selectedUnitsUUIDs.size(); ++i) {
			UUID unitUuid = NBTUtil.loadUUID(selectedUnitsUUIDs.get(i));
			Entity e = slevel.getEntity(unitUuid);
			if (!isValidUnit(e, owner)) continue;
			selectedUnits.add((CreatureEntity) e);
		}
		List<List<CreatureEntity>> spatialClusters = (new UnitClusterFinder(3, 5.0f)).findClusters(selectedUnits);
		if (spatialClusters.isEmpty()) return ActionResultType.FAIL;
		
		UUID looseUnitsUUID = MathHelper.createInsecureUUID(RNG);
		for (CreatureEntity looseUnit : spatialClusters.remove(spatialClusters.size() - 1)) {
			assignToLooseUnits(looseUnit, looseUnitsUUID, useContext);
		}
		
		Vector3d precisePos = useContext.getClickLocation();
		BlockPos blockPos = useContext.getClickedPos();
		boolean shouldBePrecise = useContext.getClickedFace() == Direction.UP;
		
		Function<Integer, UnitFormation> formationSupplier = getFormationProvider(stack, slevel);
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : controlledLeaders) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(tag));
			if (e != null) e.kill();
		}
		controlledLeaders.clear();
		
		for (List<CreatureEntity> cluster : spatialClusters) {
			Map<Integer, List<Tuple<List<CreatureEntity>, UnitFormation>>> formationsByRank = new HashMap<>();
			// Making low-level formations
			for (CreatureEntity unit : cluster) {
				if (!(unit instanceof IMovesInFormation)) {
					assignToLooseUnits(unit, looseUnitsUUID, useContext);
					continue;
				}
				IMovesInFormation formationUnit = (IMovesInFormation) unit;
				int formationRank = formationUnit.getFormationRank();
				if (!formationsByRank.containsKey(formationRank)) {
					List<CreatureEntity> entityList = Util.make(new ArrayList<>(), list -> list.add(unit));
					UnitFormation formation = formationSupplier.apply(formationRank);
					formationsByRank.put(formationRank, Util.make(new ArrayList<>(), list -> list.add(new Tuple<>(entityList, formation))));
				}
				List<Tuple<List<CreatureEntity>, UnitFormation>> formations = formationsByRank.get(formationRank);
				Tuple<List<CreatureEntity>, UnitFormation> topFormation = formations.get(formations.size() - 1);
				
				if (topFormation.getB().addEntity(unit)) {
					topFormation.getA().add(unit);
				} else {
					UnitFormation formation = formationSupplier.apply(formationRank);
					if (formation.addEntity(unit)) {
						List<CreatureEntity> entityList = Util.make(new ArrayList<>(), list -> list.add(unit));
						formations.add(new Tuple<>(entityList, formation));
					} else {
						// This really should not happen
						assignToLooseUnits(unit, looseUnitsUUID, useContext);
					}
				}
			}
			// TODO: top-level formation formation
			List<Tuple<List<CreatureEntity>, UnitFormation>> sortedFormations =
					formationsByRank.entrySet()
					.stream()
					.sorted(Comparator.comparingInt(Entry::getKey))
					.map(Entry::getValue)
					.flatMap(List::stream)
					.collect(Collectors.toList());
			
			//List<FormationLeaderEntity> leaders =
			
			// Spawning leaders
			sortedFormations
			.stream()
			.forEach(f -> {
				List<CreatureEntity> formationUnits = f.getA();
				if (formationUnits.isEmpty()) return;
				FormationLeaderEntity leader = new FormationLeaderEntity(EntityTypeInit.FORMATION_LEADER.get(), slevel, f.getB());
				
				Brain<?> leaderBrain = leader.getBrain();
				leader.setOwner(owner);
				leaderBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(slevel.dimension(), blockPos));
				if (shouldBePrecise) leaderBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);
				
				Vector3d centroid =
						formationUnits
						.stream()
						.map(Entity::position)
						.reduce((a, b) -> a.add(b))
						.map(v -> v.scale(1.0d / (double) formationUnits.size()))
						.get();
				
				CreatureEntity closestToCentroid = null;
				for (CreatureEntity unit : formationUnits) {
					if (closestToCentroid == null || centroid.distanceToSqr(unit.position()) < centroid.distanceToSqr(closestToCentroid.position())) {
						closestToCentroid = unit;
					}
				}
				Vector3d pos = closestToCentroid.position();
				
				leader.setPos(pos.x, pos.y, pos.z);
				leader.yRot = (float) -MathHelper.wrapDegrees(Math.toDegrees(MathHelper.atan2(precisePos.x - pos.x, precisePos.z - pos.z)));
				leader.setState(UnitFormation.State.FORMING);
				
				slevel.addFreshEntity(leader);
				UUID leaderUUID = leader.getUUID();
				controlledLeaders.add(NBTUtil.createUUID(leaderUUID));
				
				formationUnits
				.stream()
				.map(CreatureEntity::getBrain)
				.filter(b -> b.checkMemory(MemoryModuleTypeInit.IN_FORMATION.get(), MemoryModuleStatus.REGISTERED))
				.forEach(b -> b.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leaderUUID));
			});
		}
		
		nbt.put(TAG_CONTROLLED_LEADERS, controlledLeaders);
		
		return ActionResultType.CONSUME;
	}
	
	private static void assignToLooseUnits(CreatureEntity entity, UUID groupUUID, ItemUseContext useContext) {
		Brain<?> brain = entity.getBrain();
		if (!checkMemoryForMovement(brain) || !brain.checkMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.REGISTERED)) return;
		brain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(useContext.getLevel().dimension(), useContext.getClickedPos()));
		brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), groupUUID);
		if (useContext.getClickedFace() == Direction.UP) brain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), useContext.getClickLocation());
	}
	
	private static Function<Integer, UnitFormation> getFormationProvider(ItemStack stack, World level) {
		// TODO: more complex formation supplier
		return rank -> new LineFormation(level, rank, 10, 3);
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
		ListNBT selectedUnits = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		if (isValidUnit(entity, owner)) {
			UUID uuid = entity.getUUID();
			for (int i = 0; i < selectedUnits.size(); ++i) {
				if (!uuid.equals(NBTUtil.loadUUID(selectedUnits.get(i)))) continue;
				selectedUnits.remove(i);
				return ActionResultType.CONSUME;
			}
			if (selectedUnits.size() >= MAX_SELECTABLE_UNITS) {
				player.displayClientMessage(CANNOT_SELECT_FULL, true);
				return ActionResultType.FAIL;
			}
			selectedUnits.add(NBTUtil.createUUID(uuid));
			return ActionResultType.CONSUME;
		}
		
		CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
		if (selectedUnits.isEmpty() || mode == CombatMode.DONT_ATTACK) return ActionResultType.CONSUME;
		
		// Unlike passive targeting, directly targeting a unit will not take
		// anything into consideration other than if they are an ally.
		if (entity instanceof IHasDiplomaticOwner) {
			PlayerIDTag targetOwner = ((IHasDiplomaticOwner) entity).getDiplomaticOwner();
			if (owner.equals(targetOwner)) return ActionResultType.PASS;
			DiplomacySaveData saveData = DiplomacySaveData.get(slevel);
			DiplomaticStatus status = saveData.getDiplomaticStatus(owner, targetOwner);
			if (status == DiplomaticStatus.ALLY) return ActionResultType.CONSUME;
		}
		
		for (INBT tag : selectedUnits) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(tag));
			if (!isValidUnit(e, owner)) continue;
			CreatureEntity unit = (CreatureEntity) e;
			Brain<?> brain = unit.getBrain();
			
			if (checkMemoryForAction(brain)
				&& brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)
				&& brain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) {
				
				brain.setMemory(MemoryModuleType.ATTACK_TARGET, entity);
				brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), NPCActivityStatus.FIGHTING);
				brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
				brain.setActiveActivityIfPossible(Activity.FIGHT);
			}
		}
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : controlledLeaders) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(tag));
			if (!(e instanceof FormationLeaderEntity)) continue;
			FormationLeaderEntity leader = (FormationLeaderEntity) e;
			Brain<?> brain = leader.getBrain();
			
			if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryModuleStatus.REGISTERED)) {
				brain.setMemory(MemoryModuleType.ATTACK_TARGET, entity);
				brain.eraseMemory(MemoryModuleType.WALK_TARGET);
				brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
				
				if (brain.hasMemoryValue(MemoryModuleType.MEETING_POINT)) {
					brain.eraseMemory(MemoryModuleType.MEETING_POINT);
				}
				
				if (brain.hasMemoryValue(MemoryModuleTypeInit.PRECISE_POS.get())) {
					brain.eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
				}
			}
		}
		
		return ActionResultType.CONSUME;
	}
	
	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity.level.isClientSide) {
			if (entity instanceof PlayerEntity && !((PlayerEntity) entity).getCooldowns().isOnCooldown(this)) {
				((PlayerEntity) entity).playSound(SoundEvents.NOTE_BLOCK_BASS, 1.0f, 0.0f);
			}
			return true;
		}
		ServerWorld slevel = (ServerWorld) entity.level;
		CompoundNBT nbt = stack.getOrCreateTag();
		
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			if (player.getCooldowns().isOnCooldown(this)) return true;
			
			PlayerIDTag ownerTag = PlayerIDTag.of(player);
			if (player.isCrouching()) {
				nbt.remove(TAG_SELECTED_UNITS);
			} else {
				CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE)).next();
				nbt.putInt(TAG_CURRENT_MODE, mode.getId());
				
				ListNBT selectedUnits = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
				for (INBT tag : selectedUnits) {
					UUID uuid = NBTUtil.loadUUID(tag);
					Entity e = slevel.getEntity(uuid);
					if (!isValidUnit(e, ownerTag)) continue;
					CreatureEntity unit = (CreatureEntity) e;
					Brain<?> brain = unit.getBrain();
					if (!checkMemoryForAction(brain)) continue;
					brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
				}

				player.getCooldowns().addCooldown(this, 10);
				player.displayClientMessage(new TranslationTextComponent(COMBAT_MODE_KEY + mode.toString()), true);
			}
		}
		return true;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		int t = nbt.getInt(TAG_TICKS_TO_UPDATE);
		if (++t >= 20) {
			t = 0;
			ListNBT selectedUnits = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
			List<Integer> removeIndices = new ArrayList<>(selectedUnits.size());
			PlayerIDTag owner = new PlayerIDTag(entity.getUUID(), true);
			for (int i = 0; i < selectedUnits.size(); ++i) {
				Entity unit = slevel.getEntity(NBTUtil.loadUUID(selectedUnits.get(i)));
				if (!isValidUnit(unit, owner)) {
					removeIndices.add(i);
				}
			}
			for (int i = removeIndices.size() - 1; i >= 0; --i) {
				selectedUnits.remove(removeIndices.get(i).intValue());
			}
		}
		nbt.putInt(TAG_TICKS_TO_UPDATE, t);
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
		if (entity == null || !(entity instanceof IHasDiplomaticOwner || entity instanceof CreatureEntity)) return false;
		IHasDiplomaticOwner ownedEntity = (IHasDiplomaticOwner) entity;
		return entity.isAlive() && ownedEntity.getDiplomaticOwner().equals(owner);
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
		SelectionRendering.renderSelectionCircle(entity, matrixstack, buf);
	}
	
}
