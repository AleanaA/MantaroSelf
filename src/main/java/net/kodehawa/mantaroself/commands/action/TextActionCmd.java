package net.kodehawa.mantaroself.commands.action;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.Category;
import net.kodehawa.mantaroself.modules.commands.NoArgsCommand;

import java.util.List;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;

public class TextActionCmd extends NoArgsCommand {
	private final String desc;
	private final String format;
	private final String name;
	private final List<String> strings;

	public TextActionCmd(String name, String desc, String format, List<String> strings) {
		super(Category.ACTION);
		this.name = name;
		this.desc = desc;
		this.format = format;
		this.strings = strings;
	}

	@Override
	protected void call(GuildMessageReceivedEvent event, String content) {
		event.getChannel().sendMessage(String.format(format, random(strings))).queue();
	}

	@Override
	public Category category() {
		return Category.ACTION;
	}

	@Override
	public MessageEmbed help(GuildMessageReceivedEvent event) {
		return helpEmbed(event, name)
			.setDescription(desc)
			.build();
	}
}
