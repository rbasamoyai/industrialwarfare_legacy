package rbasamoyai.industrialwarfare.core.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.ItemCountArgSelector;
import rbasamoyai.industrialwarfare.client.screen.taskscroll.StorageSideAccessArgSelector;
import rbasamoyai.industrialwarfare.common.taskscrollcmds.IWBaseTaskScrollMethods;
import rbasamoyai.industrialwarfare.common.taskscrollcmds.TaskScrollCommand;

@EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class TaskScrollCommandInit {

	public static final TaskScrollCommand MOVE_TO = null;
	public static final TaskScrollCommand TAKE_FROM = null;
	public static final TaskScrollCommand DEPOSIT_AT = null;
	
	private static final List<Function<Integer, ArgSelector<Byte>>> NO_ARGS = new ArrayList<>();
	private static final List<Function<Integer, ArgSelector<Byte>>> ITEM_TRANSFER_ARGS = Arrays.asList(StorageSideAccessArgSelector::new, ItemCountArgSelector::new);
	
	@SubscribeEvent
	public static void registerTaskScrollCommands(RegistryEvent.Register<TaskScrollCommand> event) {
		IForgeRegistry<TaskScrollCommand> registry = event.getRegistry();
		registry.register(new TaskScrollCommand(IWBaseTaskScrollMethods::moveTo, true, false, NO_ARGS).setRegistryName(IndustrialWarfare.MOD_ID, "move_to"));
		registry.register(new TaskScrollCommand(IWBaseTaskScrollMethods::takeFrom, true, true, ITEM_TRANSFER_ARGS).setRegistryName(IndustrialWarfare.MOD_ID, "take_from"));
		registry.register(new TaskScrollCommand(IWBaseTaskScrollMethods::depositTo, true, true, ITEM_TRANSFER_ARGS).setRegistryName(IndustrialWarfare.MOD_ID, "deposit_at"));
	}
	
}
