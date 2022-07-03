package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.blockentities.MobResourcesBlockEntity;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.MobInteraction;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WorkAtCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class MobInteractionProfession extends NPCProfession {

	private final Set<Block> workingAreas;
	
	public MobInteractionProfession(Block workingArea) {
		this(ImmutableSet.of(workingArea));
	}
	
	public MobInteractionProfession(Set<Block> workingAreas) {
		this.workingAreas = workingAreas;
	}
	
	@Override
	public boolean checkMemories(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		return brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleTypeInit.COMPLAINT.get(), MemoryStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), MemoryStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleTypeInit.MOB_INTERACTION.get(), MemoryStatus.REGISTERED)
				&& brain.checkMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get(), MemoryStatus.REGISTERED);
	}

	@Override
	public Optional<BlockPos> getWorkingArea(Level level, BlockPos pos, NPCEntity npc) {
		if (!this.workingAreas.contains(level.getBlockState(pos).getBlock())) {
			return Optional.empty();
		}
		
		List<BlockPos> positions = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
		return positions.stream()
					.filter(p -> level.loadedAndEntityCanStandOn(p.below(), npc))
					.filter(p -> noCollision(level, p, npc))
					.sorted((pa, pb) -> Double.compare(Vec3.atCenterOf(pa).distanceToSqr(npc.position()), Vec3.atCenterOf(pb).distanceToSqr(npc.position())))
					.findFirst();
	}
	
	private static boolean noCollision(Level level, BlockPos pos, NPCEntity npc) {
		return level.noCollision(
				npc.getBoundingBox()
				.move(Vec3.ZERO.subtract(npc.position()))
				.move(Vec3.atBottomCenterOf(pos)));
	}

	@Override
	public void work(Level level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		BlockPos jobSitePos = order.getArgHolder(WorkAtCommand.POS_ARG_INDEX).getWrapper().getPos().get();
		BlockEntity te = level.getBlockEntity(jobSitePos);
		
		if (!(te instanceof MobResourcesBlockEntity)) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		MobResourcesBlockEntity resourceStation = (MobResourcesBlockEntity) te;
		AABB jobSiteBox = new AABB(jobSitePos.offset(-1, 0, -1), jobSitePos.offset(2, 3, 2));
		
		if (!resourceStation.isRunning()) {
			brain.setMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), true);
		}
		
		if (brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).map(List::isEmpty).orElse(true)) {
			this.populateRequestsWithRequiredItems(npc, resourceStation);
		}		
		
		if (brain.hasMemoryValue(MemoryModuleTypeInit.DEPOSITING_ITEMS.get())) {
			brain.eraseMemory(MemoryModuleTypeInit.MOB_INTERACTION.get());
			resourceStation.stopWorking(npc);
			
			if (!jobSiteBox.contains(npc.position())) {
				if (npc.getNavigation().isDone()) {
					brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(jobSitePos, 3.0f, 1));
				}
				return;
			}
			
			if (this.depositFromInventory(npc, resourceStation)) {
				resourceStation.setChanged();
				brain.eraseMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get());
			} else {
				brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_DEPOSIT_ITEM.get(), 200L);
			}
		} else if (brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).map(l -> !l.isEmpty()).orElse(false)) {
			brain.eraseMemory(MemoryModuleTypeInit.MOB_INTERACTION.get());
			if (!jobSiteBox.contains(npc.position())) {
				if (npc.getNavigation().isDone()) {
					brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(jobSitePos, 3.0f, 1));
				}
				return;
			}
			
			List<SupplyRequestPredicate> supplyRequests = brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).get();
			
			this.depositFromInventory(npc, resourceStation);
			
			supplyRequests.removeIf(p -> npc.has(p::matches));
			
			ItemStackHandler supplies = resourceStation.getSupplies();
			for (int supplySlot = 0; supplySlot < supplies.getSlots(); ++supplySlot) {
				for (int requestNum = 0; requestNum < supplyRequests.size(); ++requestNum) {
					SupplyRequestPredicate predicate = supplyRequests.get(requestNum);
					if (!predicate.matches(supplies.getStackInSlot(supplySlot))) continue;
					
					int limit = predicate.getMaxCount(supplies.getSlotLimit(supplySlot));
					ItemStack removeStack = supplies.extractItem(supplySlot, limit, false);
					int oldSz = removeStack.getCount();
					
					ItemStackHandler inventory = npc.getInventoryItemHandler();
					for (int insert = 0; insert < inventory.getSlots(); ++insert) {
						removeStack = inventory.insertItem(insert, removeStack, false);
						if (removeStack.isEmpty()) break;
					}
					int newSz = removeStack.getCount();
					if (!removeStack.isEmpty()) {
						supplies.insertItem(supplySlot, removeStack, false);
					}
					if (oldSz != newSz) {
						resourceStation.removeRequest(npc, supplyRequests.remove(requestNum));
					}
					break;
				}
				if (supplyRequests.isEmpty()) {
					break;
				}
			}
			if (!supplyRequests.isEmpty()) {
				supplyRequests.forEach(r -> resourceStation.addRequest(npc, r));
				brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOT_ENOUGH_SUPPLIES.get(), 200L);
			}
		} else if (!brain.hasMemoryValue(MemoryModuleTypeInit.MOB_INTERACTION.get())) {
			MobInteraction interaction = resourceStation.getInteraction(npc);
			brain.setMemory(MemoryModuleTypeInit.MOB_INTERACTION.get(), Optional.ofNullable(interaction));
			if (interaction == null) {
				if (!brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
					ItemEntity item = resourceStation.getItemToPickup(npc);
					if (item != null && !item.isRemoved() && !item.getItem().isEmpty()) {
						brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, item);
					} else {
						brain.setMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), true);
					}
				}
			}
		} else {
			MobInteraction interaction = brain.getMemory(MemoryModuleTypeInit.MOB_INTERACTION.get()).get();
			SupplyRequestPredicate pred = interaction.item();
			ItemStack useStack = npc.getMainHandItem();
			if (!pred.matches(useStack) && !this.switchItem(npc, pred, InteractionHand.MAIN_HAND)) {
				resourceStation.stopWorking(npc);
				brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 10);
				
				if (brain.hasMemoryValue(MemoryModuleTypeInit.SUPPLY_REQUESTS.get())) {
					brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).get().add(pred);
				} else {
					brain.setMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get(), Util.make(new ArrayList<>(), list -> list.add(pred)));
				}
			}
		}
	}
	
	private void populateRequestsWithRequiredItems(NPCEntity npc, MobResourcesBlockEntity te) {
		List<SupplyRequestPredicate> allRequiredItems = new ArrayList<>();
		allRequiredItems.addAll(te.getExtraStock());
		
		for (SupplyRequestPredicate predicate : allRequiredItems) {
			if (predicate.matches(npc.getMainHandItem()) || predicate.matches(npc.getOffhandItem())) continue;
			ItemStackHandler inventory = npc.getInventoryItemHandler();
			boolean hasItem = false;
			for (int i = 0; i < inventory.getSlots(); ++i) {
				if (!predicate.matches(inventory.getStackInSlot(i))) continue;
				hasItem = true;
				break;
			}
			if (hasItem) continue;
			Brain<?> brain = npc.getBrain();
			if (brain.hasMemoryValue(MemoryModuleTypeInit.SUPPLY_REQUESTS.get())) {
				brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).get().add(predicate);
			} else {
				brain.setMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get(), Util.make(new ArrayList<>(), list -> list.add(predicate)));
			}
		}
	}
	
	private boolean switchItem(NPCEntity npc, SupplyRequestPredicate predicate, InteractionHand hand) {
		if (predicate.matches(npc.getItemInHand(hand))) return true;
		ItemStackHandler inventory = npc.getInventoryItemHandler();
		for (int i = 0; i < inventory.getSlots(); ++i) {
			if (predicate.matches(inventory.getStackInSlot(i))) {
				ItemStack switchItem = inventory.extractItem(i, inventory.getSlotLimit(i), false);
				inventory.insertItem(i, npc.getItemInHand(hand), false);
				npc.setItemInHand(hand, switchItem);
				return true;
			}
		}
		return false;
	}
	
	private boolean depositFromInventory(NPCEntity npc, MobResourcesBlockEntity resourceStation) {
		ItemStackHandler buffer = resourceStation.getBuffer();
		ItemStackHandler inventory = npc.getInventoryItemHandler();
		boolean spaceFreedUp = false;
		List<SupplyRequestPredicate> requiredCopy = Util.make(new ArrayList<>(), list -> {
			list.addAll(resourceStation.getExtraStock());
		});
		for (int i = 0; i < inventory.getSlots(); ++i) {
			ItemStack testStack = inventory.getStackInSlot(i);
			
			boolean flag = false;
			for (int j = 0; j < requiredCopy.size(); ++j) {
				if (requiredCopy.get(j).matches(testStack)) {
					flag = true;
					requiredCopy.remove(j);
					break;
				}
			}
			if (flag) continue;
			
			ItemStack stack = inventory.extractItem(i, inventory.getSlotLimit(i), false);
			int sz = stack.getCount();
			for (int j = 0; j < buffer.getSlots(); ++j) {
				stack = buffer.insertItem(j, stack, false);
				if (stack.isEmpty()) break;
			}
			if (sz < stack.getCount() || stack.isEmpty()) {
				spaceFreedUp = true;
			}
		}
		return spaceFreedUp;
	}

	@Override
	public void stopWorking(Level level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		
	}

}
