package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction.Type;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class BlockInteractionTask extends Behavior<NPCEntity> {

	private float breakProgress;
	private int lastBreakProgress;
	private int breakTicks;
	
	public BlockInteractionTask() {
		super(ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleTypeInit.BLOCK_INTERACTION.get(), MemoryStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT),
				1200);
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, NPCEntity entity) {
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
		if (interaction.action() == Type.PLACE_BLOCK || interaction.action() == Type.MODIFY_BLOCK) {
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
	protected void start(ServerLevel level, NPCEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		BlockInteraction interaction = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get()).get();
		BlockPos pos = interaction.pos().pos();
		int reachDistance = interaction.reachDistance();
		
		if (Vec3.atCenterOf(pos).closerThan(entity.position(), (double) reachDistance + 1.0d)) {
			this.breakProgress = 0.0f;
			this.lastBreakProgress = -1;
		} else {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 3.0f, Math.max(reachDistance, 2)));
		}
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, NPCEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION.get()) || brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get())) {
			return false;
		}
		return this.checkExtraStartConditions(level, entity);
	}
	
	@Override
	protected void tick(ServerLevel level, NPCEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		BlockInteraction interaction = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get()).get();
		BlockPos pos = interaction.pos().pos();
		int reachDistance = interaction.reachDistance();
		
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
		
		if (!Vec3.atCenterOf(pos).closerThan(entity.position(), (double) reachDistance + 1.0d)) {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 3.0f, Math.max(reachDistance, 2)));
			this.breakProgress = 0.0f;
			this.lastBreakProgress = -1;
			this.breakTicks = 0;
			return;
		}
		
		BlockState levelState = level.getBlockState(pos);
		SoundType stateSound = levelState.getSoundType();
		
		if (interaction.needsToBreakBlock(level, entity)) {
			InteractionHand useHand = entity.getUsedItemHand();
			boolean justStarted = this.breakProgress == 0.0f;
			this.breakProgress += this.getBlockBreakTime(entity, level, pos);
			
			++this.breakTicks;
			if (this.breakTicks % 4 == 0) {
				level.playSound(null, pos, stateSound.getHitSound(), SoundSource.NEUTRAL, stateSound.getVolume(), stateSound.getPitch());
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
					entity.broadcastBreakEvent(useHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
				});
				if (interaction.action() == Type.BREAK_BLOCK) {
					entity.getBrain().eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
					if (!justStarted) brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 6);
				}
				level.destroyBlock(pos, canHarvest, entity);
				level.destroyBlockProgress(entity.getId(), pos, -1);
			}
		} else {
			interaction.executeBlockActionIfPossible(level, entity);
			entity.getBrain().eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
			brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 6);
		}
	}
	
	private float getBlockBreakTime(NPCEntity entity, Level level, BlockPos pos) {
		ItemStack tool = entity.getItemInHand(entity.getUsedItemHand());
		BlockState state = level.getBlockState(pos);
		
		float baseSpeed = tool.getDestroySpeed(state);
		if (baseSpeed > 1.0f) {
			int effLvl = EnchantmentHelper.getBlockEfficiency(entity);
			if (effLvl > 0 && !tool.isEmpty()) {
				baseSpeed += (float)(effLvl * effLvl + 1);
			}
		}
		
		level.destroyBlockProgress(breakTicks, pos, lastBreakProgress);
		
		if (MobEffectUtil.hasDigSpeed(entity)) {
			baseSpeed *= 1.0f + (float)(MobEffectUtil.getDigSpeedAmplification(entity) + 1) * 0.2f;
		}
		if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			switch (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
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
