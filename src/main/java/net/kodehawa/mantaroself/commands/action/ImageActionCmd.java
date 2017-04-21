package net.kodehawa.mantaroself.commands.action;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.Category;
import net.kodehawa.mantaroself.modules.commands.NoArgsCommand;
import net.kodehawa.mantaroself.utils.cache.URLCache;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;

@Slf4j
public class ImageActionCmd extends NoArgsCommand {
	public static final URLCache CACHE = new URLCache(20);

	private final Color color;
	private final String desc;
	private final String format;
	private final String imageName;
	private final List<String> images;
	private final String name;

	public ImageActionCmd(String name, String desc, Color color, String imageName, String format, List<String> images) {
		super(Category.ACTION);
		this.name = name;
		this.desc = desc;
		this.color = color;
		this.imageName = imageName;
		this.format = format;
		this.images = images;
	}

	@Override
	protected void call(GuildMessageReceivedEvent event, String content) {
		String random = random(images);
		try {
			if (mentions(event).isEmpty()) {
				event.getChannel().sendMessage(EmoteReference.ERROR + "You need to mention a user").queue();
				return;
			}

			event.getChannel().sendFile(
				CACHE.getInput(random),
				imageName,
				new MessageBuilder()
					.append(String.format(format, mentions(event), event.getAuthor().getAsMention()))
					.build()
			).queue();
		} catch (Exception e) {
			event.getChannel().sendMessage(EmoteReference.ERROR + "I'd like to know what happened, but I couldn't send the image.").queue();
			log.error("Error while performing Action Command ``" + name + "``. The image ``" + random + "`` throwed an Exception.", e);
		}
	}

	@Override
	public MessageEmbed help(GuildMessageReceivedEvent event) {
		return helpEmbed(event, name)
			.setDescription(desc)
			.setColor(color)
			.build();
	}

	private String mentions(GuildMessageReceivedEvent event) {
		return event.getMessage().getMentionedUsers().stream().map(IMentionable::getAsMention).collect(Collectors.joining(" ")).trim();
	}
}
