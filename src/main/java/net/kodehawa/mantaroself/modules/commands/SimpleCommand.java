package net.kodehawa.mantaroself.modules.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.utils.StringUtils;

import static net.kodehawa.mantaroself.commands.info.CommandStatsManager.log;
import static net.kodehawa.mantaroself.utils.DiscordUtils.name;
import static net.kodehawa.mantaroself.utils.Utils.optional;

public interface SimpleCommand extends Command {

	void call(MessageReceivedEvent event, String content, String[] args);

	@Override
	default void run(MessageReceivedEvent event, String commandName, String content) {
		call(event, content, splitArgs(content));
		log(commandName);
	}

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
		event.getChannel().sendMessage(help(event)).queue();
	}

	default String[] splitArgs(String content) {
		return StringUtils.advancedSplitArgs(content, 0);
	}
}
