package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction.Type;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WorkAtCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.tileentities.ResourceStationTileEntity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class ResourceGatheringProfession extends NPCProfession {
	
	private final List<SupplyRequestPredicate> requiredItems;
	private final Set<Block> workingAreas;
	
	public ResourceGatheringProfession(List<SupplyRequestPredicate> requiredItems, Block workingArea) {
		this(requiredItems, ImmutableSet.of(workingArea));
	}
	
	public ResourceGatheringProfession(List<SupplyRequestPredicate> requiredItems, Set<Block> workingAreas) {
		this.requiredItems = requiredItems;
		this.workingAreas = workingAreas;
	}
	
	@Override
	public boolean checkMemories(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		return brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.COMPLAINT.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get(), MemoryModuleStatus.REGISTERED);
	}

	@Override
	public Optional<BlockPos> getWorkingArea(World level, BlockPos pos, NPCEntity npc) {
		if (!this.workingAreas.contains(level.getBlockState(pos).getBlock())) {
			return Optional.empty();
		}
		List<BlockPos> positions = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
		return positions.stream()
					.filter(p -> level.loadedAndEntityCanStandOn(p.below(), npc))
					.filter(p -> noCollision(level, p, npc))
					.sorted((pa, pb) -> Double.compare(pa.distSqr(npc.blockPosition()), pb.distSqr(npc.blockPosition())))
					.findFirst();
	}
	
	private static boolean noCollision(World level, BlockPos pos, NPCEntity npc) {
		return level.noCollision(
				npc.getBoundingBox()
				.move(Vector3d.ZERO.subtract(npc.position()))
				.move(pos)
				.move(0.5d, 0.0d, 0.5d));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void work(World level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		BlockPos jobSitePos = order.getArgHolder(WorkAtCommand.POS_ARG_INDEX).getWrapper().getPos().get();
		TileEntity te = level.getBlockEntity(jobSitePos);
		
		if (!(te instanceof ResourceStationTileEntity)) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		ResourceStationTileEntity resourceStation = (ResourceStationTileEntity) te;	
		AxisAlignedBB jobSiteBox = new AxisAlignedBB(jobSitePos.offset(-1, 0, -1), jobSitePos.offset(2, 3, 2));
		
		if (!resourceStation.isRunning()) {
			brain.setMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), true);
		}
		
		if (brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).map(List::isEmpty).orElse(true)) {
			this.populateRequestsWithRequiredItems(npc, resourceStation);
		}		
		
		if (brain.hasMemoryValue(MemoryModuleTypeInit.DEPOSITING_ITEMS.get())) {
			brain.eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
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
				brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 20);
			} else {
				brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_DEPOSIT_ITEM.get(), 200L);
			}
		} else if (brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).map(l -> !l.isEmpty()).orElse(false)) {
			brain.eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
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
		} else if (!brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION.get()) && !brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get())) {
			BlockInteraction interaction = resourceStation.getInteraction(npc);
			brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get(), Optional.ofNullable(interaction));
			if (interaction == null) {
				if (!brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
					ItemEntity item = resourceStation.getItemToPickup(npc);
					if (item != null && !item.removed && !item.getItem().isEmpty()) {
						brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, item);
					} else {
						brain.setMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), true);
					}
				}
			}
		} else if (brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION.get())) {
			BlockInteraction interaction = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get()).get();
			BlockPos pos1 = interaction.pos().pos();
			BlockInteraction.Type action = interaction.action();
			Hand useHand = npc.getUsedItemHand();
			
			if (interaction.needsToBreakBlock(level, npc)) {
				BlockState state = level.getBlockState(pos1);
				ItemStack tool = npc.getItemInHand(useHand);
				SupplyRequestPredicate predicate = SupplyRequestPredicate.canBreak(state);
				
				if (!this.canBreakBlockWith(state, tool, npc) && !this.switchItem(npc, predicate, useHand)) {
					brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 10);
					resourceStation.stopWorking(npc);
					
					if (brain.hasMemoryValue(MemoryModuleTypeInit.SUPPLY_REQUESTS.get())) {
						brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).get().add(predicate);
					} else {
						brain.setMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get(), Util.make(new ArrayList<>(), list -> list.add(predicate)));
					}
				}
			} else if (action == Type.PLACE_BLOCK) {
				Hand opposite = useHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
				ItemStack useStack = npc.getItemInHand(opposite);
				SupplyRequestPredicate predicate = interaction.item();
				if (!interaction.item().matches(useStack) && !this.switchItem(npc, predicate, opposite)) {
					resourceStation.stopWorking(npc);
					brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 10);
					
					if (brain.hasMemoryValue(MemoryModuleTypeInit.SUPPLY_REQUESTS.get())) {
						brain.getMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get()).get().add(predicate);
					} else {
						brain.setMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get(), Util.make(new ArrayList<>(), list -> list.add(predicate)));
					}
				}
			}
		}
	}
	
	@Override
	public void stopWorking(World level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		brain.eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
		brain.eraseMemory(MemoryModuleTypeInit.SUPPLY_REQUESTS.get());
		TileEntity te = level.getBlockEntity(order.getWrappedArg(WorkAtCommand.POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO));
		if (!(te instanceof ResourceStationTileEntity)) return;
		((ResourceStationTileEntity) te).stopWorking(npc);
	}
	
	private void populateRequestsWithRequiredItems(NPCEntity npc, ResourceStationTileEntity te) {
		List<SupplyRequestPredicate> allRequiredItems = new ArrayList<>();
		allRequiredItems.addAll(this.requiredItems);
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
	
	private boolean switchItem(NPCEntity npc, SupplyRequestPredicate predicate, Hand hand) {
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
	
	private boolean depositFromInventory(NPCEntity npc, ResourceStationTileEntity resourceStation) {
		ItemStackHandler buffer = resourceStation.getBuffer();
		ItemStackHandler inventory = npc.getInventoryItemHandler();
		boolean spaceFreedUp = false;
		List<SupplyRequestPredicate> requiredCopy = Util.make(new ArrayList<>(), list -> {
			list.addAll(this.requiredItems);
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
	
	protected boolean canBreakBlockWith(BlockState state, ItemStack stack, NPCEntity npc) {
		return !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state);
	}

}
