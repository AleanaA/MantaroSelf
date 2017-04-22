package net.kodehawa.mantaroself.modules;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.Command;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

	private final Map<String, Command> commands;

	public CommandRegistry(Map<String, Command> commands) {
		this.commands = Preconditions.checkNotNull(commands);
	}

	public CommandRegistry() {
		this(new HashMap<>());
	}

	public Map<String, Command> commands() {
		return commands;
	}

	public boolean process(MessageReceivedEvent event, String cmdname, String content) {
		Command cmd = commands.get(cmdname);
		if (cmd == null) return false;
		cmd.run(event, cmdname, content);
		return true;
	}

	public void register(String s, Command c) {
		commands.putIfAbsent(s, c);
	}
}
