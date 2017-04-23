package net.kodehawa.mantaroself.modules.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.base.AbstractCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.modules.commands.base.Command;

import java.util.Map;

import static net.kodehawa.mantaroself.utils.StringUtils.splitArgs;

public abstract class TreeCommand extends AbstractCommand {
	private final Map<String, Command> subCommands;

	public TreeCommand(Category category, Map<String, Command> subCommands) {
		super(category);
		this.subCommands = subCommands;
	}

	/**
	 * Handling for when the Sub-Command isn't found.
	 *
	 * @param event       the Event
	 * @param commandName the Name of the not-found command.
	 * @return a {@link Command} instance to delegate, or null to cancel any further processing.
	 */
	protected abstract Command onNotFound(MessageReceivedEvent event, String commandName);

	/**
	 * Invokes the command to be executed.
	 *
	 * @param event       the event that triggered the command
	 * @param commandName the command name that was used
	 * @param content     the arguments of the command
	 */
	@Override
	public void run(MessageReceivedEvent event, String commandName, String content) {
		String[] args = splitArgs(content, 2);

		Command command = subCommands.get(args[0]);
		if (command == null) command = onNotFound(event, args[0]);
		if (command == null) return;
		command.run(event, commandName + " " + args[0], args[1]);
	}

	public Map<String, Command> getSubCommands() {
		return subCommands;
	}
}
