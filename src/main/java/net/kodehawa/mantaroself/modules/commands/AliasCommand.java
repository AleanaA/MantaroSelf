package net.kodehawa.mantaroself.modules.commands;

import lombok.Getter;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.modules.commands.base.Command;

@Getter
public class AliasCommand implements Command {
	private final Command command;
	private final String commandName;

	public AliasCommand(String commandName, Command command) {
		this.commandName = commandName;
		this.command = command;
	}

	@Override
	public Category category() {
		return null; //Alias Commands are hidden
	}

	@Override
	public MessageEmbed help(MessageReceivedEvent event) {
		return command.help(event);
	}

	@Override
	public void run(MessageReceivedEvent event, String ignored, String content) {
		command.run(event, commandName, content);
	}
}
