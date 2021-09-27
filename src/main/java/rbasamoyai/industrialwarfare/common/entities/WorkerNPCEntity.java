package rbasamoyai.industrialwarfare.common.entities;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCContainer;
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
	
	@Override
	protected ActionResultType mobInteract(PlayerEntity player, Hand handIn) {
		ActionResultType actionResultType = this.checkAndHandleImportantInteractions(player, handIn);
		if (actionResultType.consumesAction()) {
			return actionResultType;
		} else {
			if (!this.level.isClientSide && player instanceof ServerPlayerEntity) {
				IContainerProvider containerProvider = NPCContainer.getServerContainerProvider(this);
				INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(containerProvider,
						this.getCustomName().copy()
								.append(new StringTextComponent(" - "))
								.append(new TranslationTextComponent("entity." + IndustrialWarfare.MOD_ID + ".npc." + this.getDataHandler().map(handler -> handler.getOccupation()).orElse("jobless"))
										));
				NetworkHooks.openGui((ServerPlayerEntity) player, namedProvider, buf -> {
					buf.writeInt(this.inventoryItemHandler.getSlots());
					buf.writeBoolean(this.getDataHandler().map(handler -> handler.getCanWearEquipment()).orElse(false));
				});
				return ActionResultType.CONSUME;
			}
			return super.mobInteract(player, handIn);
		}
	}
	
}
