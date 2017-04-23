package net.kodehawa.mantaroself.modules.commands.base;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import static net.kodehawa.mantaroself.utils.DiscordUtils.name;
import static net.kodehawa.mantaroself.utils.Utils.optional;

/**
 * "Assisted" version of the {@link Command} interface, providing some "common ground" for all Commands based on it.
 */
public interface AssistedCommand extends Command {
	default EmbedBuilder baseEmbed(MessageReceivedEvent event, String name) {
		return baseEmbed(event, name, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
	}

	default EmbedBuilder baseEmbed(MessageReceivedEvent event, String name, String image) {
		return new EmbedBuilder()
			.setAuthor(name, null, image)
			.setColor(optional(event.getMember()).map(Member::getColor).orElse(null))
			.setFooter("Requested by " + name(event), event.getAuthor().getEffectiveAvatarUrl());
	}

	default void doTimes(int times, Runnable runnable) {
		for (int i = 0; i < times; i++) runnable.run();
	}

	default EmbedBuilder helpEmbed(MessageReceivedEvent event, String name) {
		return baseEmbed(event, name);
	}

	default void onHelp(MessageReceivedEvent event) {
		MessageEmbed helpEmbed = help(event);

		if (helpEmbed == null) {
			event.getChannel().sendMessage(EmoteReference.ERROR + "There's no extended help set for this command.").queue();
			return;
		}

		event.getChannel().sendMessage(help(event)).queue();
	}
}
