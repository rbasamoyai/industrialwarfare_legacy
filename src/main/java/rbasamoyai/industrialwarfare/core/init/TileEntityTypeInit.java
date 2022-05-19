package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tileentities.MatchCoilTileEntity;
import rbasamoyai.industrialwarfare.common.tileentities.NormalWorkstationTileEntity;
import rbasamoyai.industrialwarfare.common.tileentities.TaskScrollShelfTileEntity;

public class TileEntityTypeInit {

	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, IndustrialWarfare.MOD_ID);
	
	public static final RegistryObject<TileEntityType<NormalWorkstationTileEntity>> ASSEMBLER_WORKSTATION = TILE_ENTITY_TYPES.register("assembler_workstation",
			() -> TileEntityType.Builder.of(NormalWorkstationTileEntity::assemblerTE, BlockInit.ASSEMBLER_WORKSTATION.get()).build(null));
	
	public static final RegistryObject<TileEntityType<MatchCoilTileEntity>> MATCH_COIL = TILE_ENTITY_TYPES.register("match_coil",
			() -> TileEntityType.Builder.of(MatchCoilTileEntity::new, BlockInit.MATCH_COIL.get()).build(null));
	
	public static final RegistryObject<TileEntityType<TaskScrollShelfTileEntity>> TASK_SCROLL_SHELF = TILE_ENTITY_TYPES.register("task_scroll_shelf",
			() -> TileEntityType.Builder.of(TaskScrollShelfTileEntity::new, BlockInit.TASK_SCROLL_SHELF.get()).build(null));

}
