package net.kodehawa.mantaroself.commands.action;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.NoArgsCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.utils.cache.URLCache;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;
import static net.kodehawa.mantaroself.utils.DiscordUtils.usersMentioned;

@Slf4j
public class ImageActionCmd extends NoArgsCommand {
	public static final URLCache CACHE = new URLCache(20);

	private final String desc;
	private final String format;
	private final String imageName;
	private final List<String> images;
	private final String name;

	public ImageActionCmd(String name, String desc, String imageName, String format, List<String> images) {
		super(Category.ACTION);
		this.name = name;
		this.desc = desc;
		this.imageName = imageName;
		this.format = format;
		this.images = images;
	}

	@Override
	protected void call(MessageReceivedEvent event, String content) {
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
	public MessageEmbed help(MessageReceivedEvent event) {
		return helpEmbed(event, name)
			.setDescription(desc)
			.build();
	}

	private String mentions(MessageReceivedEvent event) {
		return usersMentioned(event.getMessage()).stream().map(IMentionable::getAsMention).collect(Collectors.joining(" ")).trim();
	}
}
