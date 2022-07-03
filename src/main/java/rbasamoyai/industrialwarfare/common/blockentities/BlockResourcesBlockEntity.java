package rbasamoyai.industrialwarfare.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;

public abstract class BlockResourcesBlockEntity extends ResourceStationBlockEntity {

	public BlockResourcesBlockEntity(BlockEntityType<? extends BlockResourcesBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	public abstract BlockInteraction getInteraction(LivingEntity entity);

}
