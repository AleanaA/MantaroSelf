package net.kodehawa.mantaroself.modules;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.kodehawa.mantaroself.modules.commands.Category;
import net.kodehawa.mantaroself.modules.commands.builders.SimpleCommandBuilder;

public class Commands {

	public static MessageEmbed helpEmbed(String name, String description, String usage) {
		String cmdname = Character.toUpperCase(name.charAt(0)) + name.substring(1) + " Command";
		return new EmbedBuilder()
			.setTitle(cmdname, null)
			.setDescription("\u200B")
			.addField("Description", description, false)
			.addField("Usage", usage, false)
			.build();
	}

	public static SimpleCommandBuilder newSimple(Category category) {
		return new SimpleCommandBuilder(category);
	}
}
