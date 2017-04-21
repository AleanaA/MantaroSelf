package net.kodehawa.mantaroself.data;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.*;

public class Data {
	public static class BotInfo {
		public String aboutDescription = "Hello, I'm a Mantaro-based Selfbot. Hello!";
		public String aboutTitle = "About this Selfbot";
		public List<String> shutdownQuotes = Arrays.asList(
			"*Shutting Down*",
			"*Goodbye!*"
		);
	}

	public static class Quote {
		private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		public static Quote quote(GuildMessageReceivedEvent event) {
			Quote quote = new Quote();
			quote.date = event.getMessage().getCreationTime().toInstant().toEpochMilli();

			quote.userId = event.getAuthor().getId();
			quote.guildId = event.getGuild().getId();
			quote.channelId = event.getChannel().getId();

			quote.userName = event.getMember().getEffectiveName();
			quote.userAvatar = event.getAuthor().getEffectiveAvatarUrl();
			quote.guildName = event.getGuild().getName();
			quote.guildAvatar = event.getGuild().getIconUrl();
			quote.channelName = event.getChannel().getName();
			quote.content = event.getMessage().getRawContent();

			return quote;
		}

		public long date;
		public String userId, guildId, channelId;
		public String userName, userAvatar, guildName, guildAvatar, channelName, content;

		public MessageEmbed embed(GuildMessageReceivedEvent event) {
			return new EmbedBuilder().setAuthor(userName + " said: ", null, guildAvatar)
				.setDescription("Quote made in server " + guildName + " in channel #" + channelName)
				.addField("Content", content, false)
				.setThumbnail(userAvatar)
				.setFooter("Date: " + FORMAT.format(date), null)
				.build();
		}
	}

	public BotInfo botInfo = new BotInfo();
	public Map<String, List<String>> custom = new HashMap<>();
	public List<Quote> quotes = new ArrayList<>();
}
