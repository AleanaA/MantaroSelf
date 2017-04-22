package net.kodehawa.mantaroself.commands.custom;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.utils.DiscordUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static net.kodehawa.mantaroself.utils.DiscordUtils.name;
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

	public static void map(String prefix, Map<String, String> map, User author, Member member) {//TODO
		map.put(prefix, member.getAsMention());
		prefix = prefix + ".";
		map.put(prefix + "username", member.getUser().getName());
		map.put(prefix + "discriminator", member.getUser().getDiscriminator());
		map.put(prefix + "name", member.getEffectiveName());
		map.put(prefix + "game", member.getGame() != null ? member.getGame().getName() : "None");
		map.put(prefix + "status", capitalize(member.getOnlineStatus().getKey()));
		map.put(prefix + "mention", member.getAsMention());
	}

	public static void map(String prefix, Map<String, String> map, MessageReceivedEvent event) {
		map.put(prefix, event.getAuthor().getAsMention() + "@" + DiscordUtils.mention(event.getChannel()));
		prefix = prefix + ".";
		map(prefix + "channel", map, event.getChannel());
		map(prefix + "guild", map, event.getGuild()); //TODO NULLABLE
		map(prefix + "me", map, event.getJDA().getSelfUser(), event.getGuild().getSelfMember());
		map(prefix + "author", map, event.getAuthor(), event.getMember());
		map(prefix + "message", map, event.getMessage());
	}

	public static void map(String prefix, Map<String, String> map, Message message) {
		map.put(prefix, splitArgs(message.getRawContent(), 2)[1]);
		prefix = prefix + ".";
		map.put(prefix + "raw", splitArgs(message.getRawContent(), 2)[1]);
		map.put(prefix + "textual", splitArgs(message.getContent(), 2)[1]);
		map.put(prefix + "stripped", splitArgs(message.getStrippedContent(), 2)[1]);
	}

	public static void map(String prefix, Map<String, String> map, MessageChannel channel) {
		String mention = DiscordUtils.mention(channel);
		map.put(prefix, mention);
		prefix = prefix + ".";
		map.put(prefix + "mention", mention);
		map.put(prefix + "name", name(channel));
		map.put(prefix + "id", channel.getId());
		map.put(prefix + "topic", channel.getType().isGuild() ? ((TextChannel) channel).getTopic() : "unknown");
	}

}
