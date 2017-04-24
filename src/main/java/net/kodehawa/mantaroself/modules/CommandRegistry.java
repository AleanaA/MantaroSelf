package net.kodehawa.mantaroself.modules;

import com.google.common.base.Preconditions;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.AliasCommand;
import net.kodehawa.mantaroself.modules.commands.base.Command;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

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
		if (!cmd.permission().test(event.getMember())) {
			event.getChannel().sendMessage(EmoteReference.STOP + "You have no permissions to trigger this command").queue();
			return false;
		}

		cmd.run(event, cmdname, content);
		return true;
	}

	public void register(String s, Command c) {
		commands.putIfAbsent(s, c);
	}

	public void registerAlias(String c, String o) {
		Preconditions.checkArgument(commands.containsKey(o), "Command don't exists");
		register(c, new AliasCommand(o, commands.get(o)));
	}
}
