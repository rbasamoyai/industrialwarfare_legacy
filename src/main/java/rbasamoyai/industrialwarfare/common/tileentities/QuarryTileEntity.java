package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.common.blocks.QuarryBlock;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.TileEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class QuarryTileEntity extends ResourceStationTileEntity {

	public static final String TAG_Y_LEVEL = "yLevel";

	protected int currentYLevel;
	
	public QuarryTileEntity() {
		this(TileEntityTypeInit.QUARRY.get());
	}
	
	public QuarryTileEntity(TileEntityType<? extends QuarryTileEntity> type) {
		super(type);
	}
	
	public void setYLevel(int y) {
		this.currentYLevel = y;
		this.setChanged();
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt.putInt(TAG_Y_LEVEL, this.currentYLevel);
		return super.save(nbt);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		this.currentYLevel = nbt.getInt(TAG_Y_LEVEL);
		super.load(state, nbt);
	}
	
	@Override
	public void setRunning(boolean running) {
		if (this.isFinished()) {
			super.setRunning(false);
		} else {
			super.setRunning(running);
		}
	}
	
	@Override
	@Nullable
	public BlockInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning) {
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
				this.isRunning = false;
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
	
	@SuppressWarnings("deprecation")
	protected void generateCache() {
		if (World.isOutsideBuildHeight(this.currentYLevel)) {
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
							new SupplyRequestPredicate(null, ItemInit.WORKER_SUPPORT.get(), IntBound.ANY, null, IntBound.ANY),
							QuarryTileEntity::placeSupportAction,
							QuarryTileEntity::checkState));
				} else if (!candidate.isAir()) {
					this.posCache.put(pos, BlockInteraction.breakBlockAt(GlobalPos.of(this.level.dimension(), pos), 4));
				}
			}
		}
	}
	
	@Override
	public boolean isFinished() {
		return this.worldPosition.getY() - this.currentYLevel > 16 || this.currentYLevel <= 4 && (this.level.dimension() == World.OVERWORLD || this.level.dimension() == World.NETHER);
	}
	
	public static void placeSupportAction(World level, BlockPos pos, LivingEntity entity) {
		level.setBlock(pos, BlockInit.WORKER_SUPPORT.get().defaultBlockState(), Constants.BlockFlags.DEFAULT);
		SoundType stateSound = level.getBlockState(pos).getSoundType();
		level.playSound(null, pos, stateSound.getPlaceSound(), SoundCategory.NEUTRAL, stateSound.getVolume(), stateSound.getPitch());
	}
	
	public static boolean checkState(World level, BlockPos pos, LivingEntity entity) {
		return level.getBlockState(pos).getBlock() == BlockInit.WORKER_SUPPORT.get();
	}
	
}
