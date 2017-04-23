package net.kodehawa.mantaroself.modules.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.base.AbstractCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;

import static net.kodehawa.mantaroself.commands.info.CommandStatsManager.log;

public abstract class NoArgsCommand extends AbstractCommand {
	public NoArgsCommand(Category category) {
		super(category);
	}

	protected abstract void call(MessageReceivedEvent event, String content);

	@Override
	public void run(MessageReceivedEvent event, String commandName, String content) {
		call(event, content);
		log(commandName);
	}
}