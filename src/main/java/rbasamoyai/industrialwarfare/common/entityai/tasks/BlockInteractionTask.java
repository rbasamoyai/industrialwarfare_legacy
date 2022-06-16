package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction.Type;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class BlockInteractionTask extends Task<NPCEntity> {

	private float breakProgress;
	private int lastBreakProgress;
	private int breakTicks;
	
	public BlockInteractionTask() {
		super(ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.BLOCK_INTERACTION.get(), MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), MemoryModuleStatus.VALUE_ABSENT),
				1200);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, NPCEntity entity) {
		BlockInteraction interaction = entity.getBrain().getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get()).get();
		if (level.dimension() != interaction.pos().dimension()) {
			entity.getBrain().eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
			return false;
		}
		
		BlockPos pos = interaction.pos().pos();
		BlockState stateAt = level.getBlockState(pos);
		
		if (interaction.needsToBreakBlock(level, entity) && (interaction.action() == Type.BREAK_BLOCK && stateAt.isAir() || stateAt.getDestroySpeed(level, pos) < 0.0f)) {
			entity.getBrain().eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
			return false;
		}
		if (interaction.action() == Type.PLACE_BLOCK) {
			if (interaction.checkState(level, entity)) {
				entity.getBrain().eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
				return false;
			}
			if (!entity.has(interaction.item()::matches)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void start(ServerWorld level, NPCEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		BlockInteraction interaction = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get()).get();
		BlockPos pos = interaction.pos().pos();
		int reachDistance = interaction.reachDistance();
		
		if (pos.closerThan(entity.position(), (double) reachDistance)) {
			this.breakProgress = 0.0f;
			this.lastBreakProgress = -1;
		} else {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 3.0f, Math.max(reachDistance, 2)));
		}
	}
	
	@Override
	protected boolean canStillUse(ServerWorld level, NPCEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION.get()) || brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get())) {
			return false;
		}
		return this.checkExtraStartConditions(level, entity);
	}
	
	@Override
	protected void tick(ServerWorld level, NPCEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		BlockInteraction interaction = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get()).get();
		BlockPos pos = interaction.pos().pos();
		int reachDistance = interaction.reachDistance();
		
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pos));
		
		if (!pos.closerThan(entity.position(), (double) reachDistance)) {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 3.0f, Math.max(reachDistance, 2)));
			this.breakProgress = 0.0f;
			this.lastBreakProgress = -1;
			this.breakTicks = 0;
			return;
		}
		
		Hand useHand = entity.getUsedItemHand();
		
		BlockState levelState = level.getBlockState(pos);
		SoundType stateSound = levelState.getSoundType();
		
		if (interaction.needsToBreakBlock(level, entity)) {
			boolean justStarted = this.breakProgress == 0.0f;
			this.breakProgress += this.getBlockBreakTime(entity, level, pos);
			
			++this.breakTicks;
			if (this.breakTicks % 4 == 0) {
				level.playSound(null, pos, stateSound.getHitSound(), SoundCategory.NEUTRAL, stateSound.getVolume(), stateSound.getPitch());
				entity.swing(useHand);
			}
			
			int i = (int)(this.breakProgress * 10.0f);
			if (i != this.lastBreakProgress) {
				level.destroyBlockProgress(entity.getId(), pos, i);
				this.lastBreakProgress = i;
			}
			
			if (this.breakProgress >= 1) {
				level.levelEvent(2001, pos, Block.getId(levelState));
				ItemStack tool = entity.getItemInHand(entity.getUsedItemHand());
				boolean canHarvest = !levelState.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(levelState);
				tool.hurtAndBreak(canHarvest ? 1 : 2, entity, e -> {
					entity.broadcastBreakEvent(useHand == Hand.MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND);
				});
				if (interaction.action() == Type.BREAK_BLOCK) {
					entity.getBrain().eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
					if (!justStarted) brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 6);
				}
				level.destroyBlock(pos, canHarvest, entity);		
			}
		} else {
			Hand opposite = useHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
			SupplyRequestPredicate pred = interaction.item();
			if (!pred.matches(entity.getItemInHand(opposite))) {
				ItemStack stack = entity.getMatching(pred::matches);
				ItemStack oldStack = entity.getItemInHand(useHand);
				entity.setItemInHand(useHand, stack.copy());
				stack.shrink(stack.getCount());
				ItemStackHandler inventory = entity.getInventoryItemHandler();
				for (int i = 0; i < inventory.getSlots(); ++i) {
					inventory.insertItem(i, oldStack, false);
				}
			}
			ItemStack useStack = entity.getItemInHand(opposite);
			useStack.shrink(1);
			interaction.executePlaceActionIfPossible(level, entity);
			entity.getBrain().eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
			brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 6);
		}
	}
	
	private float getBlockBreakTime(NPCEntity entity, World level, BlockPos pos) {
		ItemStack tool = entity.getItemInHand(entity.getUsedItemHand());
		BlockState state = level.getBlockState(pos);
		
		float baseSpeed = tool.getDestroySpeed(state);
		if (baseSpeed > 1.0f) {
			int effLvl = EnchantmentHelper.getBlockEfficiency(entity);
			if (effLvl > 0 && !tool.isEmpty()) {
				baseSpeed += (float)(effLvl * effLvl + 1);
			}
		}
		
		if (EffectUtils.hasDigSpeed(entity)) {
			baseSpeed *= 1.0f + (float)(EffectUtils.getDigSpeedAmplification(entity) + 1) * 0.2f;
		}
		if (entity.hasEffect(Effects.DIG_SLOWDOWN)) {
			switch (entity.getEffect(Effects.DIG_SLOWDOWN).getAmplifier()) {
			case 0:
				baseSpeed *= 0.3f;
				break;
			case 1:
				baseSpeed *= 0.09f;
				break;
			case 2:
				baseSpeed *= 0.0027f;
				break;
			default:
				baseSpeed *= 8.1e-4f;
			}
		}
		if (entity.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity)) {
			baseSpeed *= 0.2f;
		}
		if (!entity.isOnGround()) {
			baseSpeed *= 0.2f;
		}
		
		boolean flag = !state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state);
		
		return tool.getDestroySpeed(state) / state.getDestroySpeed(level, pos) / (flag ? 30.0f : 100.0f);
	}
	
}
