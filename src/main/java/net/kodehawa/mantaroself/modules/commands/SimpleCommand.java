package net.kodehawa.mantaroself.modules.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.base.AbstractCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.utils.StringUtils;

import static net.kodehawa.mantaroself.commands.info.CommandStatsManager.log;

public abstract class SimpleCommand extends AbstractCommand {
	public SimpleCommand(Category category) {
		super(category);
	}

	protected abstract void call(MessageReceivedEvent event, String content, String[] args);

	@Override
	public void run(MessageReceivedEvent event, String commandName, String content) {
		call(event, content, splitArgs(content));
		log(commandName);
	}

	protected String[] splitArgs(String content) {
		return StringUtils.advancedSplitArgs(content, 0);
	}
}
