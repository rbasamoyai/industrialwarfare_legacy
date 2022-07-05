package rbasamoyai.industrialwarfare.common.blockentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import rbasamoyai.industrialwarfare.common.entityai.MobInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate.IntBound;
import rbasamoyai.industrialwarfare.core.init.BlockEntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class LivestockPenBlockEntity extends MobResourcesBlockEntity {

	private final BiMap<Mob, LivingEntity> currentTasks = HashBiMap.create();
	private final Map<Mob, MobInteraction> mobCache = new LinkedHashMap<>();
	
	private int minimumLivestock = 2;
	
	private int searchCooldown = 0;
	private long timeToNextCull;
	
	public LivestockPenBlockEntity(BlockPos pos, BlockState state) {
		this(BlockEntityTypeInit.LIVESTOCK_PEN.get(), pos, state);
	}
	
	public LivestockPenBlockEntity(BlockEntityType<? extends LivestockPenBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("minimumLivestock", this.getMinimumLivestock());
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.setMinimumLivestock(nbt.getInt("minimumLivestock"));
	}
	
	@Override
	public void setRunning(boolean running) {
		super.setRunning(running);
		this.mobCache.clear();
	}
	
	@Override
	public MobInteraction getInteraction(LivingEntity entity) {
		if (!this.isRunning()) {
			return null;
		}
		
		Mob workingMob = this.currentTasks.inverse().get(entity);
		if (workingMob != null) {
			this.mobCache.remove(workingMob);
			this.currentTasks.remove(workingMob);
		}
		
		if (this.mobCache.isEmpty() && this.searchCooldown <= 0) {
			this.generateCache();
			this.searchCooldown = 300;
		}
		if (this.mobCache.isEmpty()) {
			return null;
		}
		MobInteraction interaction = null;
		Iterator<MobInteraction> iter = this.mobCache.values().iterator();
		while (iter.hasNext()) {
			MobInteraction newInteraction = iter.next();
			if (!this.currentTasks.keySet().contains(newInteraction.mob())) {
				interaction = newInteraction;
				break;
			}
		}
		if (interaction == null) return null;
		this.currentTasks.put(interaction.mob(), entity);
		return interaction;
	}
	
	private void generateCache() {
		AABB box = new AABB(this.worldPosition.offset(-8, -2, -8), this.worldPosition.offset(9, 3, 9));
		List<Animal> animals = this.level.getEntitiesOfClass(Animal.class, box);
		boolean canCull = this.timeToNextCull <= 0;
		
		Map<EntityType<?>, Integer> animalCount = new HashMap<>();
		List<Animal> unculledAnimals = new ArrayList<>();
		
		for (Animal animal : animals) {
			int count = animalCount.compute(animal.getType(), (t, c) -> c == null ? 1 : c + 1);
			if (animal.isBaby()) continue;
			if (canCull && count > this.minimumLivestock) {
				this.mobCache.put(animal, MobInteraction.killMob(animal, SupplyRequestPredicate.ANY));
			} else {
				unculledAnimals.add(animal);
			}
		}
		
		Map<EntityType<?>, Integer> breedableAnimals = new HashMap<>();
		for (Animal animal : unculledAnimals) {
			breedableAnimals.compute(animal.getType(), (t, c) -> {
				int base = c == null ? 0 : c;
				return this.canFeedAnimal(animal) ? base + 1 : base;
			});
		}
		
		Set<EntityType<?>> breedableTypes = new HashSet<>();
		for (EntityType<?> type : animalCount.keySet()) {
			if (animalCount.get(type) <= this.minimumLivestock && breedableAnimals.compute(type, (t, c) -> c == null ? 0 : c) >= 2) {
				breedableTypes.add(type);
			}
		}
		
		for (Animal animal : unculledAnimals) {
			EntityType<?> type = animal.getType();
			if (breedableTypes.contains(type) && this.canFeedAnimal(animal)) {
				this.mobCache.put(animal, MobInteraction.useItemOnMob(
						animal,
						SupplyRequestPredicate.forItem(ItemInit.ANIMAL_FEED.get(), IntBound.atLeast(1)),
						this::feedMob,
						this::canFeedMob));
				if (breedableAnimals.compute(type, (t, c) -> c == null || c <= 0 ? 0 : c - 1) <= 0) {
					breedableTypes.remove(type);
				}
			} else {
				MobInteraction interaction = this.getInteractionForAnimal(animal);
				if (interaction != null) this.mobCache.put(animal, interaction);
			}
		}
		
		if (canCull) {
			this.timeToNextCull = 3000L;
		}
	}
	
	private MobInteraction getInteractionForAnimal(Animal animal) {
		EntityType<?> type = animal.getType();
		if (this.canShearAnimal(animal, null)) {
			return MobInteraction.useItemOnMob(animal, SupplyRequestPredicate.forItem(Items.SHEARS, IntBound.atLeast(1)), this::shearAnimal, this::canShearAnimal);
		}
		if (type == EntityType.COW) {
			SupplyRequestPredicate predicate = SupplyRequestPredicate.forItem(Items.BUCKET, IntBound.atLeast(1));
			return this.hasSupply(predicate) ? MobInteraction.useItemOnMob(animal, predicate, this::milkCow, this::canMilkCow) : null;
		}
		if (type == EntityType.MOOSHROOM) {
			SupplyRequestPredicate predicate = SupplyRequestPredicate.forItem(Items.BOWL, IntBound.atLeast(1));
			return this.hasSupply(predicate) ? MobInteraction.useItemOnMob(animal, predicate, this::milkMooshroom, this::canMilkMooshroom) : null;
		}
		return null;
	}
	
	private boolean hasSupply(SupplyRequestPredicate predicate) {
		for (int i = 0; i < this.supplies.getSlots(); ++i) {
			if (predicate.matches(this.supplies.getStackInSlot(i))) {
				return true;
			}
		}
		return false;
	}
	
	private void shearAnimal(Mob target, LivingEntity actor) {
		ItemStack stack = actor.getMainHandItem();
		if (stack.is(Items.SHEARS) && target instanceof IForgeShearable) {
			actor.swing(InteractionHand.MAIN_HAND);
			int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);
			List<ItemStack> drops = ((IForgeShearable) target).onSheared(null, stack, target.level, target.blockPosition(), fortune);
			
			drops.forEach(s -> {
				actor.spawnAtLocation(s, 1.0f);
			});
			
			stack.hurtAndBreak(1, actor, e -> {
				e.broadcastBreakEvent(InteractionHand.MAIN_HAND);
			});
		}
	}
	
	private boolean canShearAnimal(Mob target, LivingEntity actor) {
		return target instanceof IForgeShearable && ((IForgeShearable) target).isShearable(new ItemStack(Items.SHEARS), target.level, target.blockPosition());
	}
	
	private void milkCow(Mob target, LivingEntity actor) {
		ItemStack stack = actor.getMainHandItem();
		if (target.getType() == EntityType.COW && stack.is(Items.BUCKET)) {
			actor.swing(InteractionHand.MAIN_HAND);
			stack.shrink(1);
			actor.spawnAtLocation(new ItemStack(Items.MILK_BUCKET), 1.0f);
		}
	}
	
	private boolean canMilkCow(Mob target, LivingEntity actor) {
		return target.getType() == EntityType.COW;
	}
	
	private void milkMooshroom(Mob target, LivingEntity actor) {
		ItemStack stack = actor.getMainHandItem();
		if (target.getType() == EntityType.MOOSHROOM && stack.is(Items.BOWL)) {
			actor.swing(InteractionHand.MAIN_HAND);
			stack.shrink(1);
			actor.spawnAtLocation(new ItemStack(Items.MILK_BUCKET), 1.0f);
		}
	}
	
	private boolean canMilkMooshroom(Mob target, LivingEntity actor) {
		return target.getType() == EntityType.MOOSHROOM;
	}
	
	private void feedMob(Mob target, LivingEntity actor) {
		if (actor.getMainHandItem().is(ItemInit.ANIMAL_FEED.get())) {
			actor.swing(InteractionHand.MAIN_HAND);
			((Animal) target).setInLove(null);
			target.gameEvent(GameEvent.MOB_INTERACT, target.eyeBlockPosition());
			actor.getMainHandItem().shrink(1);
		}
	}
	
	private boolean canFeedMob(Mob target, LivingEntity actor) {
		return target instanceof Animal && this.canFeedAnimal((Animal) target);
	}
	
	private boolean canFeedAnimal(Animal animal) {
		return animal.getAge() == 0 && animal.canFallInLove();
	}

	@Override
	protected void findItemsToPickUp() {
		AABB box = new AABB(this.worldPosition.offset(-8, -2, -8), this.worldPosition.offset(9, 3, 9));
		this.itemsToPickUp.addAll(this.level.getEntities(EntityType.ITEM, box, item -> {
			BlockPos pos = item.blockPosition().immutable();
			if (!item.isOnGround()) return false;
			return item.level.getBlockState(pos).isPathfindable(item.level, pos.above(), PathComputationType.AIR)
				&& item.level.getBlockState(pos.above()).isAir();
		}));
	}
	
	public void setMinimumLivestock(int minimumLivestock) { this.minimumLivestock = Math.max(minimumLivestock, 2); }
	public int getMinimumLivestock() { return this.minimumLivestock; }
	
	private void purgeEntries() {
		for (Iterator<Map.Entry<Mob, LivingEntity>> iter = this.currentTasks.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry<Mob, LivingEntity> e = iter.next();
			if (!e.getKey().isAlive() || !e.getValue().isAlive()) {
				iter.remove();
				this.mobCache.remove(e.getKey());
			}
		}
	}
	
	public static void serverTicker(Level level, BlockPos pos, BlockState state, LivestockPenBlockEntity pen) {
		++pen.clockTicks;
		if (pen.clockTicks >= 20) {
			pen.clockTicks = 0;
			
			pen.purgeEntries();
		}
		
		if (pen.timeToNextCull > 0) {
			--pen.timeToNextCull;
		}
		
		if (pen.searchCooldown > 0) {
			--pen.searchCooldown;
		}
	}

}
