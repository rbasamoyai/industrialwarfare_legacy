package rbasamoyai.industrialwarfare.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import net.minecraft.block.BlockState;
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
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.NPCActivityStatus;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class WhistleItem extends Item implements IHighlighterItem {

	public static final String TAG_SELECTED_UNITS = "selectedUnits";
	public static final String TAG_TICKS_TO_UPDATE = "ticksToUpdate";
	public static final String TAG_CURRENT_MODE = "currentMode";
	
	private static final int MAX_SELECTABLE_UNITS = 64;
	private static final String TRANSLATION_TEXT_KEY = "gui." + IndustrialWarfare.MOD_ID + ".text.";
	private static final ITextComponent CANNOT_SELECT_FULL = new TranslationTextComponent(TRANSLATION_TEXT_KEY + "cannot_select_full", MAX_SELECTABLE_UNITS).withStyle(TextFormatting.RED);
	private static final String COMBAT_MODE_KEY = TRANSLATION_TEXT_KEY + "combat_mode.";
	
	private static final AttributeModifier REACH_MODIFIER = new AttributeModifier(UUID.fromString("c31ab93e-802d-435c-b386-84610ebcfd74"), "Reach modifier", 16, AttributeModifier.Operation.ADDITION);
	
	private static final Random RNG = new Random();
	
	public WhistleItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_GENERAL));
	}
	
	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) return ActionResult.pass(stack);
		ServerWorld slevel = (ServerWorld) level;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		if (CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE)) != CombatMode.DONT_ATTACK) return ActionResult.consume(stack);
		
		ListNBT unitList = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		
		boolean flag = false;
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		for (int i = 0; i < unitList.size(); ++i) {
			UUID unitUuid = NBTUtil.loadUUID(unitList.get(i));
			Entity e = slevel.getEntity(unitUuid);
			if (!isValidUnit(e, owner)) continue;
			NPCEntity unit = (NPCEntity) e;
			
			Brain<?> brain = unit.getBrain();
			
			if (checkMemoryForCombat(brain)) {
				brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
				brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), NPCActivityStatus.NO_ACTIVITY);
				brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), CombatMode.DONT_ATTACK);
				brain.setActiveActivityIfPossible(Activity.IDLE);
				flag = true;
			}
		}
		
		return flag ? ActionResult.consume(stack) : ActionResult.pass(stack);
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext useContext) {
		PlayerEntity player = useContext.getPlayer();
		if (player.level.isClientSide) {
			player.playSound(SoundEvents.WOOL_PLACE, 1.0f, 0.0f);
			return ActionResultType.PASS;
		}
		ServerWorld slevel = (ServerWorld) player.level;
		
		BlockPos pos = useContext.getClickedPos();
		Vector3d precisePos = useContext.getClickLocation();
		boolean shouldBePrecise = useContext.getClickedFace() == Direction.UP;
		ItemStack stack = useContext.getItemInHand();
		CompoundNBT nbt = stack.getOrCreateTag();
		
		if (!nbt.contains(TAG_SELECTED_UNITS, Constants.NBT.TAG_LIST)) return ActionResultType.FAIL;
		ListNBT unitList = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		
		boolean flag = false;
		CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE)); 
		PlayerIDTag owner = PlayerIDTag.of(player);
		
		UUID groupUuid = MathHelper.createInsecureUUID(RNG);
		
		for (int i = 0; i < unitList.size(); ++i) {
			UUID unitUuid = NBTUtil.loadUUID(unitList.get(i));
			Entity e = slevel.getEntity(unitUuid);
			if (!isValidUnit(e, owner)) continue;		
			NPCEntity unit = (NPCEntity) e;
			
			// TODO: formations
			
			Brain<?> brain = unit.getBrain();
			
			if (!checkMemoryForCombat(brain)) continue;
			brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
			brain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(unit.level.dimension(), pos));
			if (shouldBePrecise) brain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), precisePos);
			brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
			brain.setMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get(), true);
			brain.setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), groupUuid);
			
			flag = true;
		}
		
		return flag ? ActionResultType.CONSUME : ActionResultType.PASS;
	}
	
	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (!entity.isAlive()) return ActionResultType.FAIL;
		if (player.level.isClientSide) {
			player.playSound(SoundEvents.NOTE_BLOCK_SNARE, 1.0f, 0.0f);
			return ActionResultType.SUCCESS;
		}
		ServerWorld slevel = (ServerWorld) player.level;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		if (!nbt.contains(TAG_SELECTED_UNITS, Constants.NBT.TAG_LIST)) {
			nbt.put(TAG_SELECTED_UNITS, new ListNBT());
		}
		ListNBT unitList = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
		
		PlayerIDTag owner = PlayerIDTag.of(player);
		if (isValidUnit(entity, owner)) {
			UUID uuid = entity.getUUID();
			for (int i = 0; i < unitList.size(); ++i) {
				UUID uuid1 = NBTUtil.loadUUID(unitList.get(i));
				if (!uuid.equals(uuid1)) continue;
				unitList.remove(i);
				return ActionResultType.CONSUME;
			}
			
			if (unitList.size() >= MAX_SELECTABLE_UNITS) {
				player.displayClientMessage(CANNOT_SELECT_FULL, true);
				return ActionResultType.FAIL;
			}
			
			unitList.add(NBTUtil.createUUID(uuid));
			return ActionResultType.CONSUME;
		}
		
		CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE));
		if (unitList.isEmpty() || mode == CombatMode.DONT_ATTACK) return ActionResultType.PASS;
		
		if (entity instanceof NPCEntity && ((NPCEntity) entity).getDataHandler().map(h -> {
				DiplomacySaveData diploSaveData = DiplomacySaveData.get(slevel);
				return diploSaveData.getDiplomaticStatus(owner, h.getOwner()) == DiplomaticStatus.ALLY;
			}).orElse(false)) return ActionResultType.PASS;
		
		boolean flag = false;
		for (int i = 0; i < unitList.size(); ++i) {
			Entity unit = slevel.getEntity(NBTUtil.loadUUID(unitList.get(i)));
			if (!isValidUnit(unit, owner)) continue;
			LivingEntity leUnit = (LivingEntity) unit;
			Brain<?> brain = leUnit.getBrain();
			if (checkMemoryForCombat(brain)) {
				brain.eraseMemory(MemoryModuleType.MEETING_POINT);
				brain.eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
				brain.setMemory(MemoryModuleType.ATTACK_TARGET, entity);
				brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), NPCActivityStatus.FIGHTING);
				brain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), mode);
				brain.setActiveActivityIfPossible(Activity.FIGHT);
				flag = true;
			}
		}
		
		return flag ? ActionResultType.CONSUME : ActionResultType.PASS;
	}
	
	private static Set<MemoryModuleType<?>> CHECK_FOR = null;
	
	private static boolean checkMemoryForCombat(Brain<?> brain) {
		if (CHECK_FOR == null) {
			CHECK_FOR = ImmutableSet.of(
					MemoryModuleType.ATTACK_TARGET,
					MemoryModuleType.MEETING_POINT,
					MemoryModuleTypeInit.ACTIVITY_STATUS.get(),
					MemoryModuleTypeInit.COMBAT_MODE.get(),
					MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get(),
					MemoryModuleTypeInit.IN_COMMAND_GROUP.get(),
					MemoryModuleTypeInit.PRECISE_POS.get()
					);
		}
		
		for (MemoryModuleType<?> memory : CHECK_FOR) {
			if (!brain.checkMemory(memory, MemoryModuleStatus.REGISTERED)) return false;
		}
		return true;
	}
	
	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity.level.isClientSide) {
			if (entity instanceof PlayerEntity) {
				((PlayerEntity) entity).playSound(SoundEvents.NOTE_BLOCK_BASS, 1.0f, 0.0f);
			}
			return true;
		}
		
		CompoundNBT nbt = stack.getOrCreateTag();
		
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			if (player.isCrouching()) {
				nbt.remove(TAG_SELECTED_UNITS);
			} else {
				CombatMode mode = CombatMode.fromId(nbt.getInt(TAG_CURRENT_MODE)).next();
				nbt.putInt(TAG_CURRENT_MODE, mode.getId());
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
			ListNBT unitList = nbt.getList(TAG_SELECTED_UNITS, Constants.NBT.TAG_INT_ARRAY);
			List<Integer> removeIndices = new ArrayList<>(unitList.size());
			PlayerIDTag owner = new PlayerIDTag(entity.getUUID(), true);
			for (int i = 0; i < unitList.size(); ++i) {
				Entity unit = slevel.getEntity(NBTUtil.loadUUID(unitList.get(i)));
				if (!isValidUnit(unit, owner)) {
					removeIndices.add(i);
				}
			}
			for (int i = removeIndices.size() - 1; i >= 0; --i) {
				unitList.remove(removeIndices.get(i).intValue());
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
		if (entity == null || !(entity instanceof NPCEntity)) return false;
		NPCEntity npc = (NPCEntity) entity;
		return npc.isAlive() && npc.getDataHandler().map(h -> h.getOwner().equals(owner)).orElse(false);
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
	
}
