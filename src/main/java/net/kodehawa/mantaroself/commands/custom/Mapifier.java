package net.kodehawa.mantaroself.commands.custom;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static net.kodehawa.mantaroself.utils.DiscordUtils.*;
import static net.kodehawa.mantaroself.utils.StringUtils.splitArgs;
import static net.kodehawa.mantaroself.utils.Utils.iterate;
import static org.apache.commons.lang3.StringUtils.capitalize;

public class Mapifier {
	private static final Pattern CUSTOM_COMMAND_PATTERN = Pattern.compile("\\$\\([A-Za-z0-9.]+?\\)");

	public static String dynamicResolve(String string, Map<String, String> dynamicMap) {
		if (!string.contains("$(")) return string;

		Set<String> skipIfIterated = new HashSet<>();
		for (String key : iterate(CUSTOM_COMMAND_PATTERN, string)) {
			if (skipIfIterated.contains(key)) continue;
			String mapKey = key.substring(2, key.length() - 1);
			string = string.replace(key, dynamicMap.getOrDefault(mapKey, mapKey));
			if (!string.contains("$(")) break;
			skipIfIterated.add(key);
		}

		return string;
	}

	public static void map(String prefix, Map<String, String> map, Guild guild) {
		map.put(prefix, guild.getName());
		prefix = prefix + ".";
		map.put(prefix + "name", guild.getName());
		map(prefix + "owner", map, guild.getOwner().getUser(), guild.getOwner());
		map.put(prefix + "region", guild.getRegion().getName());
		map(prefix + "publicChannel", map, guild.getPublicChannel());
		//map(prefix + "me", map, guild.getSelfMember());
	}

	public static void map(String prefix, Map<String, String> map, User author, Member member) {
		map.put(prefix, author.getAsMention());
		prefix = prefix + ".";
		map.put(prefix + "username", author.getName());
		map.put(prefix + "discriminator", author.getDiscriminator());
		map.put(prefix + "name", name(author, member));
		Game game = game(author, member);
		map.put(prefix + "game", game != null ? game.getName() : "None");
		OnlineStatus status = status(author, member);
		map.put(prefix + "status", capitalize((status == null ? OnlineStatus.UNKNOWN : status).getKey().toLowerCase()));
		map.put(prefix + "mention", author.getAsMention());
	}

	public static void map(String prefix, Map<String, String> map, MessageReceivedEvent event) {
		map.put(prefix, event.getAuthor().getAsMention() + "@" + mention(event.getChannel()));
		prefix = prefix + ".";
		map(prefix + "channel", map, event.getChannel());
		if (event.getGuild() != null) map(prefix + "guild", map, event.getGuild());
		if (event.getGroup() != null) map(prefix + "group", map, event.getGroup());
		map(prefix + "me", map, event.getJDA().getSelfUser(), event.getGuild().getSelfMember());
		map(prefix + "author", map, event.getAuthor(), event.getMember());
		map(prefix + "message", map, event.getMessage());
	}

	public static void map(String prefix, Map<String, String> map, Group group) {
		String name = name(group);
		map.put(prefix, name);
		prefix = prefix + ".";
		map.put(prefix + "name", name);
		map(prefix + "owner", map, group.getOwner(), null);
	}

	public static void map(String prefix, Map<String, String> map, Message message) {
		map.put(prefix, splitArgs(message.getRawContent(), 2)[1]);
		prefix = prefix + ".";
		map.put(prefix + "raw", splitArgs(message.getRawContent(), 2)[1]);
		map.put(prefix + "textual", splitArgs(message.getContent(), 2)[1]);
		map.put(prefix + "stripped", splitArgs(message.getStrippedContent(), 2)[1]);
	}

	public static void map(String prefix, Map<String, String> map, MessageChannel channel) {
		String mention = mention(channel);
		map.put(prefix, mention);
		prefix = prefix + ".";
		map.put(prefix + "mention", mention);
		map.put(prefix + "name", name(channel));
		map.put(prefix + "id", channel.getId());
		map.put(prefix + "topic", channel.getType().isGuild() ? ((TextChannel) channel).getTopic() : "unknown");
	}

}
