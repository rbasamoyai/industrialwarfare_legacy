package rbasamoyai.industrialwarfare.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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
import rbasamoyai.industrialwarfare.common.containers.whistle.WhistleContainer;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IHasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitClusterFinder;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.LineFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.PointFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation.Point;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
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
		ListNBT selectedUnitsUUIDs = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		if (selectedUnitsUUIDs.isEmpty()) return ActionResultType.CONSUME;
		
		Vector3d precisePos = useContext.getClickLocation();
		BlockPos blockPos = useContext.getClickedPos();
		boolean shouldBePrecise = useContext.getClickedFace() == Direction.UP;
		GlobalPos globPos = GlobalPos.of(slevel.dimension(), blockPos);
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		
		if (!nbt.contains(TAG_COMMAND_GROUP)) {
			nbt.putUUID(TAG_COMMAND_GROUP, MathHelper.createInsecureUUID(RNG));
		}
		UUID commandGroup = nbt.getUUID(TAG_COMMAND_GROUP);
		
		// Set new position
		for (INBT t : controlledLeaders) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(t));
			if (!isValidLeader(e)) continue;
			FormationLeaderEntity leader = (FormationLeaderEntity) e;
			
			Brain<?> leaderBrain = leader.getBrain();
			leaderBrain.setMemory(MemoryModuleType.MEETING_POINT, globPos);
			if (shouldBePrecise) leaderBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);
			leaderBrain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
			leaderBrain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
			leaderBrain.eraseMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get());
		}
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		
		List<CreatureEntity> looseUnits = new ArrayList<>();
		
		List<CreatureEntity> unitsToCluster = new ArrayList<>();
		
		for (INBT t : selectedUnitsUUIDs) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(t));
			if (!isValidUnit(e, owner)) continue;
			CreatureEntity unit = (CreatureEntity) e;
			
			Brain<?> unitBrain = unit.getBrain();
			
			if (!unitBrain.checkMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.REGISTERED)) {
				looseUnits.add(unit);
				continue;
			}
			
			if (!unitBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
				|| !unitBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(commandGroup)
				|| !unitBrain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())) {
				unitsToCluster.add(unit);
			}
			
			unitBrain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
		}
		
		List<List<CreatureEntity>> spatialClusters = (new UnitClusterFinder(3, 5.0f)).findClusters(unitsToCluster);
		if (spatialClusters.isEmpty()) return ActionResultType.FAIL;
		looseUnits.addAll(spatialClusters.remove(spatialClusters.size() - 1));
		
		for (List<CreatureEntity> cluster : spatialClusters) {
			if (cluster.isEmpty()) continue;
			
			Vector3d centroid =
					cluster
					.stream()
					.map(Entity::position)
					.reduce((a, b) -> a.add(b))
					.map(v -> v.scale(1.0d / (double) cluster.size()))
					.get();
			
			CreatureEntity closestToCentroid = null;
			for (CreatureEntity unit : cluster) {
				if (closestToCentroid == null || centroid.distanceToSqr(unit.position()) < centroid.distanceToSqr(closestToCentroid.position())) {
					closestToCentroid = unit;
				}
			}
			Vector3d pos = closestToCentroid.position();
			float facing = (float) -MathHelper.wrapDegrees(Math.toDegrees(MathHelper.atan2(precisePos.x - pos.x, precisePos.z - pos.z)));
			
			FormationLeaderEntity leader = this.spawnInnerFormationLeaders(slevel, pos, facing, this.getNewFormation(stack, 0), commandGroup, owner);
			Brain<?> leaderBrain = leader.getBrain();
			leaderBrain.setMemory(MemoryModuleType.MEETING_POINT, globPos);
			if (shouldBePrecise) leaderBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);
			UUID leaderUUID = leader.getUUID();
			controlledLeaders.add(NBTUtil.createUUID(leaderUUID));
			// TODO: unit class map
			
			// Making low-level formations
			for (CreatureEntity unit : cluster) {
				if (!(unit instanceof IMovesInFormation)) {
					looseUnits.add(unit);
					continue;
				}
				IMovesInFormation formationUnit = (IMovesInFormation) unit;
				int formationRank = formationUnit.getFormationRank();
				
				if (!leader.addEntity(unit)) {
					FormationLeaderEntity restore = leader;
					leader = this.spawnInnerFormationLeaders(slevel, pos, facing, this.getNewFormation(stack, 0), commandGroup, owner);
					if (!leader.addEntity(unit)) { // Should not happen under any circumstances
						leader.remove();
						leader = restore;
						looseUnits.add(unit);
					} else {
						leaderBrain = leader.getBrain();
						leaderBrain.setMemory(MemoryModuleType.MEETING_POINT, globPos);
						if (shouldBePrecise) leaderBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);
						leaderUUID = leader.getUUID();
						controlledLeaders.add(NBTUtil.createUUID(leaderUUID));
					}
				}
			}
		}
		
		for (CreatureEntity unit : looseUnits) {
			Brain<?> brain = unit.getBrain();
			if (!checkMemoryForMovement(brain)) continue;
			brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
			brain.setMemory(MemoryModuleType.MEETING_POINT, globPos);
			if (shouldBePrecise) brain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);
		}
		
		nbt.put(TAG_CONTROLLED_LEADERS, controlledLeaders);
		
		return ActionResultType.CONSUME;
	}
	
	private FormationLeaderEntity spawnFormationLeader(World level, Vector3d pos, float facing, UnitFormation formation) {
		FormationLeaderEntity leader = new FormationLeaderEntity(EntityTypeInit.FORMATION_LEADER.get(), level, formation);
		leader.setPos(pos.x, pos.y, pos.z);
		leader.yRot = facing;
		leader.setState(UnitFormation.State.FORMED);
		level.addFreshEntity(leader);
		return leader;
	}
	
	private FormationLeaderEntity spawnInnerFormationLeaders(World level, Vector3d pos, float facing, UnitFormation topFormation, UUID commandGroup, PlayerIDTag owner) {
		FormationLeaderEntity leader = this.spawnFormationLeader(level, pos, facing, topFormation);
		leader.setOwner(owner);
		Brain<?> brain = leader.getBrain();
		brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
		
		for (UnitFormation innerFormation : topFormation.getInnerFormations()) {
			leader.addEntity(this.spawnInnerFormationLeaders(level, pos, facing, innerFormation, commandGroup, owner));
		}
		return leader;
	}
	
	private UnitFormation getNewFormation(ItemStack stack, int rank) {
		return new PointFormation.Builder()
				.addRegularPoint(new Point(0, 0), 0)
				.addFormationPoint(new Point(0, -2), new LineFormation(0, 10, 3))
				.build();
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
		
		if (!nbt.contains(TAG_COMMAND_GROUP)) {
			nbt.putUUID(TAG_COMMAND_GROUP, MathHelper.createInsecureUUID(RNG));
		}
		UUID commandGroupUUID = nbt.getUUID(TAG_COMMAND_GROUP);
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		if (isValidUnit(entity, owner)) {
			UUID uuid = entity.getUUID();
			for (int i = 0; i < selectedUnits.size(); ++i) {
				if (!uuid.equals(NBTUtil.loadUUID(selectedUnits.get(i)))) continue;
				selectedUnits.remove(i);
				Brain<?> unitBrain = entity.getBrain();
				if (unitBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
					&& unitBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(commandGroupUUID)) {
					unitBrain.eraseMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get());
				}
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
			
			if (brain.checkMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.REGISTERED)) {
				brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroupUUID);
			}
			
			if (checkMemoryForAction(brain)
				&& brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)
				&& !brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())) {
				
				brain.setMemory(MemoryModuleType.ATTACK_TARGET, Optional.of(entity));
				brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.FIGHTING);
				brain.setActiveActivityIfPossible(Activity.FIGHT);
			}
		}
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : controlledLeaders) {
			Entity e = slevel.getEntity(NBTUtil.loadUUID(tag));
			if (!(e instanceof FormationLeaderEntity)) continue;
			FormationLeaderEntity leader = (FormationLeaderEntity) e;
			Brain<?> brain = leader.getBrain();
			
			if (checkMemoryForEngagement(brain)) {
				brain.setMemory(MemoryModuleType.ATTACK_TARGET, entity);
				brain.eraseMemory(MemoryModuleType.WALK_TARGET);
				brain.eraseMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get());
				
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
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : controlledLeaders) {
			Entity e = level.getEntity(NBTUtil.loadUUID(tag));
			if (e != null) e.kill();
		}
		
		player.getCooldowns().addCooldown(this, 10);
		player.displayClientMessage(STOPPED_UNITS, true);
	}
	
	public void removeUnits(ServerWorld level, ItemStack stack, PlayerEntity player) {
		CompoundNBT nbt = stack.getOrCreateTag();
		ListNBT selectedUnits = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		List<Integer> removeIndices = new ArrayList<>(selectedUnits.size());
		PlayerIDTag owner = new PlayerIDTag(player.getUUID(), true);
		for (int i = 0; i < selectedUnits.size(); ++i) {
			Entity unit = level.getEntity(NBTUtil.loadUUID(selectedUnits.get(i)));
			if (!isValidUnit(unit, owner)) {
				removeIndices.add(i);
			}
		}
		for (int i = removeIndices.size() - 1; i >= 0; --i) {
			selectedUnits.remove(removeIndices.get(i).intValue());
		}
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		removeIndices.clear();
		for (int i = 0; i < controlledLeaders.size(); ++i) {
			Entity leader = level.getEntity(NBTUtil.loadUUID(controlledLeaders.get(i)));
			if (!isValidLeader(leader)) {
				removeIndices.add(i);
			}
		}
		for (int i = removeIndices.size() - 1; i >= 0; --i) {
			controlledLeaders.remove(removeIndices.get(i).intValue());
		}
	}
	
	public void updateStance(ServerWorld level, ItemStack stack, PlayerEntity player) {
		CompoundNBT nbt = stack.getOrCreateTag();
		
		CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
		nbt.putInt(TAG_CURRENT_MODE, mode.getId());
		
		PlayerIDTag ownerTag = PlayerIDTag.of(player);
		
		ListNBT selectedUnits = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : selectedUnits) {
			UUID uuid = NBTUtil.loadUUID(tag);
			Entity e = level.getEntity(uuid);
			if (!isValidUnit(e, ownerTag)) continue;
			CreatureEntity unit = (CreatureEntity) e;
			Brain<?> brain = unit.getBrain();
			if (!checkMemoryForAction(brain)) continue;
			brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), mode == CombatMode.DONT_ATTACK ? ActivityStatus.NO_ACTIVITY : ActivityStatus.FIGHTING);
			brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
			brain.setActiveActivityIfPossible(mode == CombatMode.DONT_ATTACK ? Activity.IDLE : Activity.FIGHT);
		}
		
		ListNBT controlledLeaders = nbt.getList(TAG_CONTROLLED_LEADERS, Constants.NBT.TAG_INT_ARRAY);
		for (INBT tag : controlledLeaders) {
			Entity e = level.getEntity(NBTUtil.loadUUID(tag));
			if (!(e instanceof FormationLeaderEntity)) continue;
			FormationLeaderEntity leader = (FormationLeaderEntity) e;
			Brain<?> brain = leader.getBrain();
			
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
		}
		
		player.getCooldowns().addCooldown(this, 10);
		player.displayClientMessage(new TranslationTextComponent(COMBAT_MODE_KEY + mode.toString()), true);
	}
	
	//private void formUpEntities
	
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
	
	private static boolean checkMemoryForEngagement(Brain<?> brain) {
		return brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryModuleStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), MemoryModuleStatus.REGISTERED);
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
	public boolean canOpen(ItemStack stack) {
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
		
		CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
		
		ResourceLocation typeLoc = nbt.contains(TAG_FORMATION_TYPE)
				? new ResourceLocation(nbt.getString(TAG_FORMATION_TYPE))
				: IWModRegistries.UNIT_FORMATION_TYPES.getDefaultKey(); 
		
		buf
		.writeVarInt(mode.getId())
		.writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES, typeLoc);
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
	
}
