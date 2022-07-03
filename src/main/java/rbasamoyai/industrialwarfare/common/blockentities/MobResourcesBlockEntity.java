package rbasamoyai.industrialwarfare.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.industrialwarfare.common.entityai.MobInteraction;

public abstract class MobResourcesBlockEntity extends ResourceStationBlockEntity {
	
	public MobResourcesBlockEntity(BlockEntityType<? extends MobResourcesBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	public abstract MobInteraction getInteraction(LivingEntity entity);
	
}
