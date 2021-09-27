package rbasamoyai.industrialwarfare.core.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tileentities.NormalWorkstationTileEntity;
import rbasamoyai.industrialwarfare.common.tileentities.TaskScrollShelfTileEntity;

@Mod.EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class TileEntityTypeInit {

	public static final TileEntityType<?> ASSEMBLER_WORKSTATION = null;
	public static final TileEntityType<?> TASK_SCROLL_SHELF = null;
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
		registry.register(TileEntityType.Builder.of(NormalWorkstationTileEntity::assemblerTE, BlockInit.ASSEMBLER_WORKSTATION).build(null).setRegistryName(IndustrialWarfare.MOD_ID, "assembler_workstation"));
		registry.register(TileEntityType.Builder.of(() -> new TaskScrollShelfTileEntity(), BlockInit.TASK_SCROLL_SHELF).build(null).setRegistryName(IndustrialWarfare.MOD_ID, "task_scroll_shelf"));
	}

}
