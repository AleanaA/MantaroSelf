package net.kodehawa.mantaroself.modules.commands.base;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Interface used for handling commands within the bot.
 */
public interface Command {
	/**
	 * The Command's {@link Category}
	 *
	 * @return a Nullable {@link Category}. Null means that the command should be hidden from Help.
	 */
	Category category();

	/**
	 * Embed to be used on help command
	 *
	 * @param event the event that triggered the help
	 * @return a Nullable {@link MessageEmbed}
	 */
	MessageEmbed help(MessageReceivedEvent event);

	/**
	 * Invokes the command to be executed.
	 *
	 * @param event       the event that triggered the command
	 * @param commandName the command name that was used
	 * @param content     the arguments of the command
	 */
	void run(MessageReceivedEvent event, String commandName, String content);
}
