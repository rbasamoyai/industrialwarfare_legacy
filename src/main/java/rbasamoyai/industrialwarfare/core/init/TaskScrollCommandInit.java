package rbasamoyai.industrialwarfare.core.init;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.DepositAtCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.MoveToCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TakeFromCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;

@EventBusSubscriber(modid = IndustrialWarfare.MOD_ID, bus = Bus.MOD)
@ObjectHolder(IndustrialWarfare.MOD_ID)
public class TaskScrollCommandInit {

	public static final TaskScrollCommand MOVE_TO = null;
	public static final TaskScrollCommand TAKE_FROM = null;
	public static final TaskScrollCommand DEPOSIT_AT = null;
	
	@SubscribeEvent
	public static void registerTaskScrollCommands(RegistryEvent.Register<TaskScrollCommand> event) {
		event.getRegistry().registerAll(new TaskScrollCommand[] {
				new MoveToCommand(),
				new TakeFromCommand(),
				new DepositAtCommand()
		});
	}
	
}
