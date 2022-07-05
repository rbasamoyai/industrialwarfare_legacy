package rbasamoyai.industrialwarfare.common.blockentities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;

public class FarmingPlotBlockEntity extends BlockResourcesBlockEntity implements ConfigurableBounds {
	
	private final BiMap<BlockPos, LivingEntity> currentTasks = HashBiMap.create();
	private final Map<BlockPos, BlockInteraction> posCache = new LinkedHashMap<>();
	
	private BlockPos startingCorner;
	private BlockPos endingCorner;
	
	private int searchCooldown = 0;
	
	public FarmingPlotBlockEntity(BlockPos pos, BlockState state) {
		this(BlockEntityTypeInit.FARMING_PLOT.get(), pos, state);
	}
	
	public FarmingPlotBlockEntity(BlockEntityType<? extends FarmingPlotBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (this.startingCorner != null && this.endingCorner != null) {
			CompoundTag start = new CompoundTag();
			start.putInt("x", this.startingCorner.getX());
			start.putInt("y", this.startingCorner.getY());
			start.putInt("z", this.startingCorner.getZ());
			tag.put("startingCorner", start);
			CompoundTag end = new CompoundTag();
			end.putInt("x", this.endingCorner.getX());
			end.putInt("y", this.endingCorner.getY());
			end.putInt("z", this.endingCorner.getZ());
			tag.put("endingCorner", end);
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		if (nbt.contains("startingCorner", Tag.TAG_COMPOUND) && nbt.contains("endingCorner", Tag.TAG_COMPOUND)) {
			CompoundTag start = nbt.getCompound("startingCorner");
			BlockPos startPos = new BlockPos(start.getInt("x"), start.getInt("y"), start.getInt("z"));
			CompoundTag end = nbt.getCompound("endingCorner");
			BlockPos endPos = new BlockPos(end.getInt("x"), end.getInt("y"), end.getInt("z"));
			this.trySettingBounds(null, null, startPos, endPos);
		}
	}
	
	@Override
	public void trySettingBounds(Player player, ItemStack stack, BlockPos pos1, BlockPos pos2) {
		if (pos1 == null || pos2 == null) {
			if (player != null) {
				
			}
			return;
		}
		AABB bounds = new AABB(pos1, pos2.offset(1, 1, 1));
		if (bounds.getXsize() > 32 || bounds.getYsize() > 5 || bounds.getZsize() > 32) {
			if (player != null) {
				player.displayClientMessage(new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".too_large", 32, 5, 32, bounds.getXsize(), bounds.getYsize(), bounds.getZsize()).withStyle(ChatFormatting.RED), true);
			}
			return;
		}
		if (!bounds.contains(Vec3.atCenterOf(this.worldPosition))) {
			if (player != null) {
				player.displayClientMessage(new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".must_contain_block", this.worldPosition.toShortString()).withStyle(ChatFormatting.RED), true);
			}
			return;
		}
		this.startingCorner = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
		this.endingCorner = new BlockPos(bounds.maxX - 1.0d, bounds.maxY - 1.0d, bounds.maxZ - 1.0d);
		if (player != null) {
			player.displayClientMessage(new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".set_bounds", this.worldPosition.toShortString(), this.startingCorner.toShortString(), this.endingCorner.toShortString()), true);
		}
		this.setChanged();
	}

	@Override
	public BlockInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning() || !this.hasCornersSetUp()) {
			return null;
		}
		
		BlockPos workingPos = this.currentTasks.inverse().get(entity);
		if (workingPos != null) {
			this.posCache.remove(workingPos);
			this.currentTasks.remove(workingPos);
		}
		
		if (this.posCache.isEmpty() && this.searchCooldown <= 0) {
			this.generateCache();
			this.searchCooldown = 300;
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
	
	private void generateCache() {
		if (!this.hasCornersSetUp()) return;
		for (BlockPos mpos : BlockPos.betweenClosed(this.startingCorner, this.endingCorner)) {
			BlockPos pos = mpos.immutable();
			BlockPos abovePos = pos.above();
			BlockState groundState = this.level.getBlockState(pos);
			BlockState aboveState = this.level.getBlockState(abovePos);
			
			if (groundState.is(rbasamoyai.industrialwarfare.common.ModTags.Blocks.CLEARABLES)) {
				this.posCache.put(pos, BlockInteraction.breakBlockAt(GlobalPos.of(this.level.dimension(), pos), 4));
			} else if (aboveState.is(rbasamoyai.industrialwarfare.common.ModTags.Blocks.CLEARABLES)) {
				this.posCache.put(abovePos, BlockInteraction.breakBlockAt(GlobalPos.of(this.level.dimension(), abovePos), 4));
			}
		}
		if (!this.posCache.isEmpty()) return;
		
		for (BlockPos mpos : BlockPos.betweenClosed(this.startingCorner, this.endingCorner)) {
			BlockPos pos = mpos.immutable();
			BlockPos abovePos = pos.above();
			BlockState groundState = this.level.getBlockState(pos);
			BlockState aboveState = this.level.getBlockState(abovePos);
			
			if (groundState.is(BlockTags.DIRT) && aboveState.isAir()) {
				this.posCache.put(pos, BlockInteraction.modifyBlockAt(
						GlobalPos.of(this.level.dimension(), pos),
						SupplyRequestPredicate.forTool(ToolActions.HOE_TILL, Tiers.WOOD),
						this::tillDirt,
						this::isTilled));
			} else if (groundState.is(Blocks.FARMLAND) && aboveState.isAir()) {
				this.posCache.put(abovePos, BlockInteraction.placeBlockAtAs(
						GlobalPos.of(this.level.dimension(), abovePos),
						SupplyRequestPredicate.forItem(Tags.Items.SEEDS, IntBound.ANY),
						this::plantSeeds,
						this::canPlant));
			} else if (groundState.is(BlockTags.CROPS) && this.canHarvest(pos)) {
				this.posCache.put(pos, BlockInteraction.modifyBlockAt(
						GlobalPos.of(this.level.dimension(), pos),
						SupplyRequestPredicate.ANY,
						this::harvestCrop,
						this::isHarvested));
			} else if (aboveState.is(BlockTags.CROPS) && this.canHarvest(abovePos)) {
				this.posCache.put(abovePos, BlockInteraction.modifyBlockAt(
						GlobalPos.of(this.level.dimension(), abovePos),
						SupplyRequestPredicate.ANY,
						this::harvestCrop,
						this::isHarvested));
			}
		}
	}
	
	private boolean canHarvest(BlockPos pos) {
		BlockState state = this.level.getBlockState(pos);
		return state.getBlock() instanceof CropBlock && ((CropBlock) state.getBlock()).isMaxAge(state);
	}
	
	private void plantSeeds(Level level, BlockPos pos, LivingEntity entity) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = entity.getItemInHand(hand);
			Item item = stack.getItem();
			if (stack.is(Tags.Items.SEEDS) && item instanceof BlockItem) {
				level.setBlockAndUpdate(pos, ((BlockItem) item).getBlock().defaultBlockState());
				stack.shrink(1);
				return;
			}
		}
	}
	
	private boolean canPlant(Level level, BlockPos pos, LivingEntity entity) {
		return level.getBlockState(pos).is(BlockTags.CROPS);
	}
	
	private void tillDirt(Level level, BlockPos pos, LivingEntity entity) {
		ItemStack stack = entity.getMainHandItem();
		if (stack.canPerformAction(ToolActions.HOE_TILL) && level.getBlockState(pos).is(BlockTags.DIRT)) {
			level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
			stack.hurtAndBreak(1, entity, e -> {
				e.broadcastBreakEvent(InteractionHand.MAIN_HAND);
			});
			level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
		}
	}
	
	private boolean isTilled(Level level, BlockPos pos, LivingEntity entity) {
		if (level.getBlockState(pos).is(Blocks.FARMLAND)) {
			this.currentTasks.inverse().remove(entity);
			return true;
		}
		return false;
	}
	
	private void harvestCrop(Level level, BlockPos pos, LivingEntity entity) {
		level.destroyBlock(pos, true, entity);
		this.plantSeeds(level, pos, entity);
	}
	
	private boolean isHarvested(Level level, BlockPos pos, LivingEntity entity) {
		BlockState state = level.getBlockState(pos);
		return state.getBlock() instanceof CropBlock && !((CropBlock) state.getBlock()).isMaxAge(state);
	}

	@Override
	protected void findItemsToPickUp() {
		if (!this.hasCornersSetUp()) return;
		AABB box = new AABB(this.startingCorner.immutable(), this.endingCorner.immutable().offset(1, 2, 1));
		box.inflate(1);
		this.itemsToPickUp.addAll(this.level.getEntities(EntityType.ITEM, box, item -> {
			BlockPos pos = item.blockPosition().immutable();
			if (!item.isOnGround()) return false;
			if (item.level.getBlockState(pos).is(Blocks.FARMLAND)) {
				return item.level.getBlockState(pos.above()).isPathfindable(item.level, pos.above(), PathComputationType.AIR)
					&& item.level.getBlockState(pos.offset(0, 2, 0)).isAir();
			}
			return item.level.getBlockState(pos).isPathfindable(item.level, pos.above(), PathComputationType.AIR)
				&& item.level.getBlockState(pos.above()).isAir();
		}));
	}
	
	@Override
	public void setRunning(boolean running) {
		super.setRunning(running);
		this.posCache.clear();
		this.searchCooldown = 0;
	}
	
	@Override
	public boolean isFinished() {
		return !this.hasCornersSetUp();
	}
	
	private boolean hasCornersSetUp() {
		return this.startingCorner != null && this.endingCorner != null;
	}

	@Override
	public AABB getBoxForRenderingCurrentBounds(ItemStack stack) {
		return this.startingCorner != null && this.endingCorner != null ? new AABB(this.startingCorner.immutable(), this.endingCorner.immutable().offset(1, 1, 1)) : null;
	}

	@Override public BlockPos startingCorner() { return this.startingCorner; }
	@Override public BlockPos endingCorner() { return this.endingCorner; }

	private void purgeEntries() {
		for (Iterator<Map.Entry<BlockPos, LivingEntity>> iter = this.currentTasks.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry<BlockPos, LivingEntity> entry = iter.next();
			if (entry.getValue().isDeadOrDying()) {
				iter.remove();
			}
		}
	}
	
	public static void serverTicker(Level pLevel, BlockPos pos, BlockState state, FarmingPlotBlockEntity farm) {
		++farm.clockTicks;
		if (farm.clockTicks >= 20) {
			farm.purgeEntries();
		}
		if (!farm.hasCornersSetUp()) {
			farm.setRunning(false);
		}
		
		if (farm.searchCooldown > 0) {
			--farm.searchCooldown;
		}
	}
	
}
