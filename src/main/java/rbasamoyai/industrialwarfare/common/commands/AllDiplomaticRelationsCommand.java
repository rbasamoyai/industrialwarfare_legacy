package rbasamoyai.industrialwarfare.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class AllDiplomaticRelationsCommand implements Command<CommandSource> {

	private static final AllDiplomaticRelationsCommand CMD = new AllDiplomaticRelationsCommand();

	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		return Commands.literal("alldiplomaticrelations")
				.requires(cs -> cs.hasPermission(2))
				.fork(null, null)
				.executes(CMD);
	}
	
	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		
		return 0;
	}
	
}
