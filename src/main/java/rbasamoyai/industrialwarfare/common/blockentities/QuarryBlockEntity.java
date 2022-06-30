package rbasamoyai.industrialwarfare.common.blockentities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import rbasamoyai.industrialwarfare.common.blocks.QuarryBlock;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class QuarryBlockEntity extends ResourceStationBlockEntity {

	public static final String TAG_Y_LEVEL = "yLevel";

	protected final BiMap<BlockPos, LivingEntity> currentTasks = HashBiMap.create();
	protected final Map<BlockPos, BlockInteraction> posCache = new LinkedHashMap<>();
	protected int currentYLevel;
	
	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		this(BlockEntityTypeInit.QUARRY.get(), pos, state);
	}
	
	public QuarryBlockEntity(BlockEntityType<? extends QuarryBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	public void setYLevel(int y) {
		this.currentYLevel = y;
		this.setChanged();
	}
	
	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt(TAG_Y_LEVEL, this.currentYLevel);
	}
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.currentYLevel = tag.getInt(TAG_Y_LEVEL);
	}
	
	@Override
	public void setRunning(boolean running) {
		if (this.isFinished()) {
			super.setRunning(false);
		} else {
			super.setRunning(running);
		}
	}
	
	protected void purgeEntries() {
		for (Map.Entry<BlockPos, LivingEntity> entry : this.currentTasks.entrySet()) {
			if (entry.getValue().isDeadOrDying()) {
				this.currentTasks.remove(entry.getKey());
			}
		}
	}
	
	@Override
	public void stopWorking(LivingEntity entity) {
		super.stopWorking(entity);
		this.currentTasks.inverse().remove(entity);
	}
	
	@Override
	@Nullable
	public BlockInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning()) {
			return null;
		}
		
		for (Map.Entry<BlockPos, LivingEntity> entry : this.currentTasks.entrySet()) {
			if (entry.getValue() == entity) {
				this.posCache.remove(entry.getKey());
				this.currentTasks.remove(entry.getKey());
				break;
			}
		}
		
		if (this.posCache.isEmpty()) {
			this.generateCache();
		}
		if (this.posCache.isEmpty()) {
			this.setYLevel(this.currentYLevel - 1);
			if (this.isFinished()) {
				this.setRunning(false);
				return null;
			}
			this.generateCache();
		}
		if (this.posCache.isEmpty()) {
			return null;
		}
		BlockInteraction interaction = null;
		Iterator<BlockInteraction> iter = this.posCache.values().iterator();
		while (iter.hasNext()) {
			BlockInteraction newInteraction = iter.next();
			if (!this.currentTasks.keySet().contains(newInteraction.pos().pos())) {
				interaction = newInteraction;
				break;
			}
		}
		if (interaction == null) return null;
		this.currentTasks.put(interaction.pos().pos(), entity);
		return interaction;
	}
	
	public static final int[] RAMP_START_I_TABLE	= new int[] { 00, 14, 02, 00, 00, 00, 14, 02, 00, 14, 02, 00, 00, 00, 14, 02 };
	public static final int[] RAMP_START_J_TABLE	= new int[] { 00, 00, 14, 02, 00, 14, 02, 00, 00, 00, 14, 02, 00, 14, 02, 00 };
	public static final int[] RAMP_END_I_TABLE		= new int[] { 14, 16, 16, 02, 02, 14, 16, 16, 14, 16, 16, 02, 02, 14, 16, 16 };
	public static final int[] RAMP_END_J_TABLE		= new int[] { 02, 14, 16, 16, 14, 16, 16, 02, 02, 14, 16, 16, 14, 16, 16, 02 };
	
	protected void generateCache() {
		if (this.level.isOutsideBuildHeight(this.currentYLevel)) {
			return;
		}
		
		int startX;
		int startZ;
		int signX;
		int signZ;
		
		Direction direction = this.getBlockState().getValue(QuarryBlock.FACING);	
		
		switch (direction) {
		case WEST:
			startX = this.worldPosition.getX() + 1;
			startZ = this.worldPosition.getZ() - 8;
			signX = 1;
			signZ = 1;
			break;
		case EAST:
			startX = this.worldPosition.getX() - 1;
			startZ = this.worldPosition.getZ() + 8;
			signX = -1;
			signZ = -1;
			break;
		case SOUTH:
			startX = this.worldPosition.getX() - 8;
			startZ = this.worldPosition.getZ() - 1;
			signX = 1;
			signZ = -1;
			break;
		default:
			startX = this.worldPosition.getX() + 8;
			startZ = this.worldPosition.getZ() + 1;
			signX = -1;
			signZ = 1;
			break;
		}
		
		int rampStartI = -1;
		int rampStartJ = -1;
		int rampEndI = -1;
		int rampEndJ = -1;
		
		int currentDepth = this.worldPosition.getY() - this.currentYLevel - 1;
		int rampSide = currentDepth < 0 ? -1 : currentDepth & 3;
		int valueIndex = rampSide | direction.get2DDataValue() << 2;
		
		if (valueIndex != -1) {
			rampStartI = RAMP_START_I_TABLE[valueIndex];
			rampStartJ = RAMP_START_J_TABLE[valueIndex];
			rampEndI = RAMP_END_I_TABLE[valueIndex];
			rampEndJ = RAMP_END_J_TABLE[valueIndex];
		}
		
		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				BlockPos pos = new BlockPos(startX + i * signX, this.currentYLevel, startZ + j * signZ);
				BlockState candidate = this.level.getBlockState(pos);
				
				if (candidate.is(IWBlockTags.IGNORE_WHEN_MINING) || candidate.getDestroySpeed(this.level, pos) < 0.0f) {
					continue;
				}
				
				if (rampStartI <= i && i < rampEndI && rampStartJ <= j && j < rampEndJ) {
					this.posCache.put(pos, BlockInteraction.placeBlockAtAs(
							GlobalPos.of(this.level.dimension(), pos),
							SupplyRequestPredicate.forItem(ItemInit.WORKER_SUPPORT.get(), IntBound.ANY),
							QuarryBlockEntity::placeSupportAction,
							QuarryBlockEntity::checkState));
				} else if (!candidate.isAir()) {
					this.posCache.put(pos, BlockInteraction.breakBlockAt(GlobalPos.of(this.level.dimension(), pos), 4));
				}
			}
		}
	}
	
	@Override
	protected void findItemsToPickUp() {
		this.itemsToPickUp.clear();
		int startX;
		int startZ;
		int signX;
		int signZ;
		
		Direction direction = this.getBlockState().getValue(QuarryBlock.FACING);	
		
		switch (direction) {
		case WEST:
			startX = this.worldPosition.getX() + 1;
			startZ = this.worldPosition.getZ() - 8;
			signX = 1;
			signZ = 1;
			break;
		case EAST:
			startX = this.worldPosition.getX() - 1;
			startZ = this.worldPosition.getZ() + 8;
			signX = -1;
			signZ = -1;
			break;
		case SOUTH:
			startX = this.worldPosition.getX() - 8;
			startZ = this.worldPosition.getZ() - 1;
			signX = 1;
			signZ = -1;
			break;
		default:
			startX = this.worldPosition.getX() + 8;
			startZ = this.worldPosition.getZ() + 1;
			signX = -1;
			signZ = 1;
			break;
		}
		
		BlockPos startPos = new BlockPos(startX, this.currentYLevel, startZ);
		BlockPos endPos = new BlockPos(startX + 16 * signX, this.currentYLevel + 2, startZ + 16 * signZ);
		AABB pickupArea = new AABB(startPos, endPos).inflate(1.0f);
		
		this.itemsToPickUp.addAll(this.level.getEntities(EntityType.ITEM, pickupArea, item -> {
			BlockPos pos = item.blockPosition();
			return item.level.getBlockState(pos).isAir() && item.level.getBlockState(pos.above()).isAir();
		}));
	}
	
	@Override
	public boolean isFinished() {
		return this.worldPosition.getY() - this.currentYLevel > 16 || this.currentYLevel <= 4 && (this.level.dimension() == Level.OVERWORLD || this.level.dimension() == Level.NETHER);
	}
	
	public static void placeSupportAction(Level level, BlockPos pos, LivingEntity entity) {
		level.setBlock(pos, BlockInit.WORKER_SUPPORT.get().defaultBlockState(), Block.UPDATE_ALL);
		SoundType stateSound = level.getBlockState(pos).getSoundType();
		level.playSound(null, pos, stateSound.getPlaceSound(), SoundSource.NEUTRAL, stateSound.getVolume(), stateSound.getPitch());
	}
	
	public static boolean checkState(Level level, BlockPos pos, LivingEntity entity) {
		return level.getBlockState(pos).getBlock() == BlockInit.WORKER_SUPPORT.get();
	}
	
	public static void serverTicker(Level level, BlockPos pos, BlockState state, QuarryBlockEntity quarry) {
		++quarry.clockTicks;
		if (quarry.clockTicks >= 20) {
			quarry.clockTicks = 0;
			quarry.purgeEntries();
		}
	}
	
}
