package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction.Type;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WorkAtCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.tileentities.QuarryTileEntity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class QuarrierProfession extends NPCProfession {
	
	@Override
	public boolean checkMemories(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		return brain.checkMemory(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_FAILED.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.COMPLAINT.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), MemoryModuleStatus.REGISTERED);
	}

	@Override
	public Optional<BlockPos> getWorkingArea(World level, BlockPos pos, NPCEntity npc) {
		return Optional.of(pos);
	}

	@Override
	public void work(World level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		BlockPos jobSitePos = order.getArgHolder(WorkAtCommand.POS_ARG_INDEX).getWrapper().getPos().get();
		TileEntity te = level.getBlockEntity(jobSitePos);
		
		if (!(te instanceof QuarryTileEntity)) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		QuarryTileEntity quarry = (QuarryTileEntity) te;	
		AxisAlignedBB jobSiteBox = new AxisAlignedBB(jobSitePos.offset(-1, 0, -1), jobSitePos.offset(2, 3, 2));
		
		if (!quarry.isRunning()) {
			if (!jobSiteBox.contains(npc.position())) {
				brain.setMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get(), true);
				if (npc.getNavigation().isDone()) {
					brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(jobSitePos, 3.0f, 1));
				}
			}
		}
		
		if (brain.hasMemoryValue(MemoryModuleTypeInit.DEPOSITING_ITEMS.get())) {
			brain.eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get());
			quarry.stopWorking(npc);
			
			if (!jobSiteBox.contains(npc.position())) {
				if (npc.getNavigation().isDone()) {
					brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(jobSitePos, 3.0f, 1));
				}
				return;
			}
			
			LazyOptional<IItemHandler> depositLzop = quarry.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
			depositLzop.ifPresent(h -> {
				boolean spaceFreedUp = false;
				ItemStackHandler inventory = npc.getInventoryItemHandler();
				for (int i = 0; i < inventory.getSlots(); ++i) {
					ItemStack stack = inventory.extractItem(i, inventory.getSlotLimit(i), false);
					int sz = stack.getCount();
					for (int j = 0; j < h.getSlots(); ++j) {
						stack = h.insertItem(j, stack, false);
						if (stack.isEmpty()) break;
					}
					if (sz < stack.getCount() || stack.isEmpty()) {
						spaceFreedUp = true;
					}
				}
				if (spaceFreedUp) {
					quarry.setChanged();
					brain.eraseMemory(MemoryModuleTypeInit.DEPOSITING_ITEMS.get());
					brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 20);
				}
			});
		} else if (brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION_FAILED.get())) {
			BlockInteraction interaction = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_FAILED.get()).get();
			BlockPos pos1 = interaction.pos().pos();
			BlockInteraction.Type action = interaction.action();
			Hand useHand = npc.getUsedItemHand();
			
			if (!jobSiteBox.contains(npc.position())) {
				if (npc.getNavigation().isDone()) {
					brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(jobSitePos, 3.0f, 1));
				}
				return;
			}
			
			if (interaction.needsToBreakBlock(level)) {
				BlockState state = level.getBlockState(pos1);
				ItemStack tool = npc.getItemInHand(useHand);
				
				if (state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state)) {
					LazyOptional<IItemHandler> suppliesLzop = quarry.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH);
					suppliesLzop.ifPresent(h -> {
						boolean gotCorrectTool = false;
						for (int i = 0; i < h.getSlots(); ++i) {
							ItemStack newTool = h.getStackInSlot(i);
							if (newTool.isCorrectToolForDrops(state)) {
								newTool = h.extractItem(i, h.getSlotLimit(i), false);
								h.insertItem(i, npc.getItemInHand(useHand), false);
								npc.setItemInHand(useHand, newTool);
								gotCorrectTool = true;
								break;
							}
						}
						if (gotCorrectTool) {
							brain.eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_FAILED.get());
							brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 10);
						} else {
							brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.WRONG_TOOL.get(), 200L);
						}
					});
				}
			} else if (action == Type.PLACE_BLOCK) {
				Hand opposite = useHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
				ItemStack useStack = npc.getItemInHand(opposite);
				if (interaction.item() != useStack.getItem()) {
					
					LazyOptional<IItemHandler> suppliesLzop = quarry.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH);
					suppliesLzop.ifPresent(h -> {
						boolean gotSupplies = false;
						for (int i = 0; i < h.getSlots(); ++i) {
							if (h.getStackInSlot(i).getItem() == interaction.item()) {
								ItemStack supply = h.extractItem(i, 64, false);
								h.insertItem(i, npc.getItemInHand(opposite), false);
								npc.setItemInHand(opposite, supply);
								gotSupplies = true;
								break;
							}
						}
						if (gotSupplies) {
							brain.eraseMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_FAILED.get());
							brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 10);
						} else {
							brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOT_ENOUGH_SUPPLIES.get(), 200L);
						}
					});
				}
			}
		} else if (!brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION.get()) && !brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get())) {
			brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get(), Optional.ofNullable(quarry.getInteraction(npc)));
		} else if (brain.hasMemoryValue(MemoryModuleTypeInit.BLOCK_INTERACTION.get())) {
			BlockInteraction interaction = brain.getMemory(MemoryModuleTypeInit.BLOCK_INTERACTION.get()).get();
			BlockPos pos1 = interaction.pos().pos();
			BlockInteraction.Type action = interaction.action();
			Hand useHand = npc.getUsedItemHand();
			
			if (interaction.needsToBreakBlock(level)) {
				BlockState state = level.getBlockState(pos1);
				ItemStack tool = npc.getItemInHand(useHand);
				
				if (state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state)) {
					brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 10);
					brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_FAILED.get(), interaction);
					quarry.stopWorking(npc);
				}
			} else if (action == Type.PLACE_BLOCK) {
				Hand opposite = useHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
				ItemStack useStack = npc.getItemInHand(opposite);
				if (interaction.item() != useStack.getItem()) {
					brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_COOLDOWN.get(), 10);
					brain.setMemory(MemoryModuleTypeInit.BLOCK_INTERACTION_FAILED.get(), interaction);
					quarry.stopWorking(npc);
				}
			}
		}
	}
	
	@Override
	public void stopWorking(World level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		TileEntity te = level.getBlockEntity(order.getWrappedArg(WorkAtCommand.POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO));
		if (!(te instanceof QuarryTileEntity)) return;
		((QuarryTileEntity) te).stopWorking(npc);
	}

}
