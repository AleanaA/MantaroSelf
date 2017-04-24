package net.kodehawa.mantaroself.commands.custom;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static net.kodehawa.mantaroself.utils.Utils.optional;

public class EmbedJSON {
	public static class EmbedField {
		public boolean inline;
		public String name, value;
	}

	public String author, authorImg, authorUrl;
	public String color;
	public String description;
	public List<EmbedField> fields = new ArrayList<>();
	public String footer, footerUrl;
	public String image;
	public String thumbnail;
	public String title, titleUrl;

	public MessageEmbed gen(MessageReceivedEvent event) {
		EmbedBuilder embed = new EmbedBuilder();
		if (title != null) embed.setTitle(title, titleUrl);
		if (description != null) embed.setDescription(description);
		if (author != null) embed.setAuthor(author, authorUrl, authorImg);
		if (footer != null) embed.setFooter(footer, footerUrl);
		if (image != null) embed.setImage(image);
		if (thumbnail != null) embed.setThumbnail(thumbnail);
		if (color != null) {
			Color c = null;
			try {
				c = (Color) Color.class.getField(color).get(null);
			} catch (Exception ignored) {
				String colorLower = color.toLowerCase();
				if (colorLower.equals("member")) {
					c = optional(event.getMember()).map(Member::getColor).orElse(null);
				} else if (colorLower.matches("#?(0x)?[0123456789abcdef]{1,6}")) {
					try {
						c = Color.decode(colorLower.startsWith("0x") ? colorLower : "0x" + colorLower);
					} catch (Exception ignored2) {}
				}
			}
			if (c != null) embed.setColor(c);
		}

		fields.forEach(f -> {
			if (f == null) {
				embed.addBlankField(false);
			} else if (f.value == null) {
				embed.addBlankField(f.inline);
			} else {
				embed.addField(f.name == null ? "" : f.name, f.value, f.inline);
			}
		});

		return embed.build();
	}

}
