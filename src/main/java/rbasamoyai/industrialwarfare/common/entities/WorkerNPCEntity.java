package rbasamoyai.industrialwarfare.common.entities;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;

/*
 * Worker NPC class that can be ordered to work at workstations. 
 */

public class WorkerNPCEntity extends NPCEntity {
	
	public static final int INITIAL_INVENTORY_COUNT = 5;
	
	public Block workstation;
	
	public WorkerNPCEntity(EntityType<? extends NPCEntity> type, World worldIn) {
		super(type, worldIn, "jobless", "Unnamed NPC", null, INITIAL_INVENTORY_COUNT, true);
	}
	
	public WorkerNPCEntity(World worldIn, String occupation, String name, @Nullable PlayerEntity player, int initialInventoryCount, boolean armorSlotsEnabled, Block workstation) {
		super(EntityTypeInit.WORKER_NPC, worldIn, occupation, name, player, initialInventoryCount, armorSlotsEnabled);
		this.workstation = workstation;
	}
	
}
