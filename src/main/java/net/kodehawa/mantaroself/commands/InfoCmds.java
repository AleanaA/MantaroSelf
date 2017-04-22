package net.kodehawa.mantaroself.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.kodehawa.mantaroself.MantaroInfo;
import net.kodehawa.mantaroself.MantaroSelf;
import net.kodehawa.mantaroself.commands.info.CommandStatsManager;
import net.kodehawa.mantaroself.commands.info.StatsHelper.CalculatedDoubleValues;
import net.kodehawa.mantaroself.commands.info.StatsHelper.CalculatedIntValues;
import net.kodehawa.mantaroself.core.CommandProcessor;
import net.kodehawa.mantaroself.core.listeners.command.CommandListener;
import net.kodehawa.mantaroself.data.MantaroData;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.Commands;
import net.kodehawa.mantaroself.modules.HasPostLoad;
import net.kodehawa.mantaroself.modules.RegisterCommand;
import net.kodehawa.mantaroself.modules.commands.Category;
import net.kodehawa.mantaroself.modules.commands.Command;
import net.kodehawa.mantaroself.utils.DiscordUtils;
import net.kodehawa.mantaroself.utils.RateLimiter;
import net.kodehawa.mantaroself.utils.Utils;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.kodehawa.mantaroself.MantaroSelf.prefix;
import static net.kodehawa.mantaroself.commands.info.AsyncInfoMonitor.*;
import static net.kodehawa.mantaroself.commands.info.HelpUtils.forType;
import static net.kodehawa.mantaroself.commands.info.StatsHelper.calculateDouble;
import static net.kodehawa.mantaroself.commands.info.StatsHelper.calculateInt;
import static net.kodehawa.mantaroself.data.MantaroData.data;
import static net.kodehawa.mantaroself.utils.DiscordUtils.usersMentioned;

@RegisterCommand.Class
public class InfoCmds implements HasPostLoad {
	public static Logger LOGGER = LoggerFactory.getLogger("InfoCmds");

	@RegisterCommand
	public static void about(CommandRegistry cr) {
		cr.register("about", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
//				if (!content.isEmpty() && args[0].equals("credits")) {
//					EmbedBuilder builder = new EmbedBuilder();
//					builder.setAuthor("Credits.", null, event.getJDA().getSelfUser().getAvatarUrl())
//						.setColor(Color.BLUE)
//						.setDescription("**Main developer**: Kodehawa#3457\n"
//							+ "**Developer**: AdrianTodt#0722\n" + "**Music**: Steven#6340\n" + "**Cross bot integration**: Natan#1289\n**Grammar corrections and development**: Adam#9261")
//						.addField("Special mentions",
//							"Thanks to DiscordBots, Carbonitex and DiscordBots.org for helping us with increasing the bot's visibility.", false)
//						.setFooter("Much thanks to them for helping make Mantaro better!", event.getJDA().getSelfUser().getAvatarUrl());
//					event.getChannel().sendMessage(builder.build()).queue();
//					return;
//				}

				List<Guild> guilds = MantaroSelf.getInstance().getGuilds();
				int guildCount = guilds.size();
				int usersCount = MantaroSelf.getInstance().getUsers().size();
				long onlineCount = guilds.stream()
					.flatMap(guild -> guild.getMembers().stream())
					.filter(user -> !user.getOnlineStatus().equals(OnlineStatus.OFFLINE))
					.map(member -> member.getUser().getId())
					.distinct()
					.count();
				int tcCount = MantaroSelf.getInstance().getTextChannels().size();
				int vcCount = MantaroSelf.getInstance().getVoiceChannels().size();
				long millis = ManagementFactory.getRuntimeMXBean().getUptime();
				long seconds = millis / 1000;
				long minutes = seconds / 60;
				long hours = minutes / 60;
				long days = hours / 24;

				event.getChannel().sendMessage(thiz.baseEmbed(event, data().get().botInfo.aboutTitle)
					.setDescription(data().get().botInfo.aboutDescription)
					.addField(
						"Information:",
						"**Selfbot Version**: " + MantaroInfo.VERSION + "\n" +
							"**Uptime**: " + String.format("%d:%02d:%02d:%02d", days, hours % 24, minutes % 60, seconds % 60) + "\n" +
							"**Threads**: " + Thread.activeCount() + "\n" +
							"**Servers**: " + guildCount + "\n" +
							"**Users (Online/Known)**: " + onlineCount + "/" + usersCount + "\n" +
							"**Text/Voice Channels**: " + tcCount + "/" + vcCount + "\n",
						false
					)
					.setFooter(String.format("Invite link: http://polr.me/mantaro (Commands this session: %s)", CommandListener.getCommandTotal()), event.getJDA().getSelfUser().getAvatarUrl())
					.build()
				).queue();
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "About Command")
				.addField("Description:", "Read info about Mantaro!", false)
				.addField("Information", prefix() + "about credits lists everyone who has helped on the bot's development", false)
				.setColor(Color.PINK)
				.build())
			.build());
	}

	@RegisterCommand
	public static void avatar(CommandRegistry cr) {
		cr.register("avatar", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
				if (!event.getMessage().getMentionedUsers().isEmpty()) {
					event.getChannel().sendMessage(String.format(EmoteReference.OK + "Avatar for: **%s**\n%s", event.getMessage().getMentionedUsers().get(0).getName(), event.getMessage().getMentionedUsers().get(0).getAvatarUrl())).queue();
					return;
				}
				event.getChannel().sendMessage(String.format("Avatar for: **%s**\n%s", event.getAuthor().getName(), event.getAuthor().getAvatarUrl())).queue();
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "Avatar")
				.setDescription("Get user avatar URLs")
				.addField("Usage",
					prefix() + "avatar - Get your avatar url" +
						"\n " + prefix() + "avatar <mention> - Get a user's avatar url.", false)
				.build())
			.build());
	}

	@RegisterCommand
	public static void guildinfo(CommandRegistry cr) {
		cr.register("guildinfo", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
				TextChannel channel = event.getTextChannel();

				if (channel == null) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "This command can only be issued from a guild!").queue();
					return;
				}

				Guild guild = channel.getGuild();

				String roles = guild.getRoles().stream()
					.filter(role -> !guild.getPublicRole().equals(role))
					.map(Role::getName)
					.collect(Collectors.joining(", "));

				if (roles.length() > 1024)
					roles = roles.substring(0, 1024 - 4) + "...";

				channel.sendMessage(new EmbedBuilder()
					.setAuthor("Server Information", null, guild.getIconUrl())
					.setColor(guild.getOwner().getColor() == null ? Color.ORANGE : guild.getOwner().getColor())
					.setDescription("Server information for " + guild.getName())
					.setThumbnail(guild.getIconUrl())
					.addField("Users (Online/Unique)", (int) guild.getMembers().stream().filter(u -> !u.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() + "/" + guild.getMembers().size(), true)
					.addField("Main Channel", guild.getPublicChannel().getAsMention(), true)
					.addField("Creation Date", guild.getCreationTime().format(DateTimeFormatter.ISO_DATE_TIME).replaceAll("[^0-9.:-]", " "), true)
					.addField("Voice/Text Channels", guild.getVoiceChannels().size() + "/" + guild.getTextChannels().size(), true)
					.addField("Owner", guild.getOwner().getUser().getName() + "#" + guild.getOwner().getUser().getDiscriminator(), true)
					.addField("Region", guild.getRegion() == null ? "Unknown." : guild.getRegion().getName(), true)
					.addField("Roles (" + guild.getRoles().size() + ")", roles, false)
					.setFooter("Server ID: " + String.valueOf(guild.getId()), null)
					.build()
				).queue();
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "Guild Info Command")
				.addField("Description:", "See your guild's current stats.\n*Can be only issued from a Guild.*", false)
				.setColor(event.getGuild().getOwner().getColor() == null ? Color.ORANGE : event.getGuild().getOwner().getColor())
				.build())
			.build());
	}

	@RegisterCommand
	public static void help(CommandRegistry cr) {
		Random r = new Random();
		List<String> jokes = Collections.unmodifiableList(Arrays.asList(
			"Yo damn I heard you like help, because you just issued the help command to get the help about the help command.",
			"Congratulations, you managed to use the help command.",
			"Helps you to help yourself.",
			"Help Inception.",
			"A help helping helping helping help."
		));

		cr.register("help", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
				if (content.isEmpty()) {
					String prefix = MantaroData.config().get().prefix();

					EmbedBuilder embed = thiz.baseEmbed(event, "Commands:")
						.setColor(Color.PINK)
						.setDescription("Command help. For extended usage please use " + String.format("%shelp <command>.", prefix))
						.setFooter(String.format("To check command usage, type %shelp <command> // -> Commands: " +
								CommandProcessor.REGISTRY.commands().entrySet().stream().filter(
									(command) -> !command.getValue().hidden()).count()
							, prefix), null);

					Arrays.stream(Category.values())
						.forEach(c -> embed.addField(c + " Commands:", forType(c), false));

					event.getChannel().sendMessage(embed.build()).queue();

				} else {
					Command command = CommandProcessor.REGISTRY.commands().get(content);

					if (command != null) {
						final MessageEmbed help = command.help(event);
						Optional.ofNullable(help).ifPresent((help1) -> event.getChannel().sendMessage(help1).queue());
						if (help == null)
							event.getChannel().sendMessage(EmoteReference.ERROR + "There's no extended help set for this command.").queue();
					} else {
						event.getChannel().sendMessage(EmoteReference.ERROR + "A command with this name doesn't exist").queue();
					}
				}
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "Help Command")
				.setColor(Color.PINK)
				.addField("Description:", jokes.get(r.nextInt(jokes.size())), false)
				.addField(
					"Usage:",
					"`" + prefix() + "help`: Return information about who issued the command.\n`" + prefix() + "help <command>`: Return information about the command specified.",
					false
				).build())
			.build());
	}

	@RegisterCommand
	public static void info(CommandRegistry cr) {
		cr.register("info", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
				List<Guild> guilds = MantaroSelf.getInstance().getGuilds();

				event.getChannel().sendMessage("```prolog\n"
					+ "---Selfbot Technical Information---\n\n"
					+ "Commands: " + CommandProcessor.REGISTRY.commands().entrySet().stream().filter((command) -> !command.getValue().hidden()).count() + "\n"
					+ "Bot Version: " + MantaroInfo.VERSION + "\n"
					+ "JDA Version: " + JDAInfo.VERSION + "\n"
					+ "API Responses: " + MantaroSelf.getInstance().getResponseTotal() + "\n"
					+ "CPU Usage: " + getVpsCPUUsage() + "%" + "\n"
					+ "CPU Cores: " + getAvailableProcessors() + "\n"
					+ "\n\n ------------------ \n\n"
					+ "Guilds: " + guilds.size() + "\n"
					+ "Users: " + guilds.stream().flatMap(guild -> guild.getMembers().stream()).map(user -> user.getUser().getId()).distinct().count() + "\n"
					+ "Threads: " + Thread.activeCount() + "\n"
					+ "Executed Commands: " + CommandListener.getCommandTotal() + "\n"
					+ "Memory: " + (getTotalMemory() - getFreeMemory()) + "MB / " + getMaxMemory() + "MB" + "\n"
					+ "```").queue();
			})
			.help((thiz, event) -> thiz.baseEmbed(event, "Info")
				.setDescription("Gets the bot technical information")
				.build())
			.build());
	}

	@RegisterCommand
	public static void invite(CommandRegistry cr) {
		cr.register("invite", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
				event.getChannel().sendMessage(new EmbedBuilder().setAuthor("Mantaro's Invite URL.", null, event.getJDA().getSelfUser().getAvatarUrl())
					.addField("Invite URL", "http://polr.me/mantaro", false)
					.addField("Support Server", "https://discordapp.com/invite/cMTmuPa", false)
					.addField("Patreon URL", "http://patreon.com/mantaro", false)
					.setDescription("Here are some useful links! If you have any questions about the bot, feel free to join the support guild and tag @Steven#6340." +
						"\nWe provided a patreon link in case you would like to help Mantaro keep running by donating [and getting perks!]. Thanks you in advance for using the bot! <3 from the developers")
					.setFooter("We hope you have fun with the bot.", event.getJDA().getSelfUser().getAvatarUrl())
					.build()).queue();
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "Invite command")
				.setDescription("Gives you a bot OAuth invite link.").build())
			.build());
	}

	@RegisterCommand
	public static void ping(CommandRegistry cr) {
		RateLimiter rateLimiter = new RateLimiter(TimeUnit.SECONDS, 10);

		cr.register("ping", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
				if (!rateLimiter.process(event.getAuthor())) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "Uh-oh. Slowdown buddy.").queue();
					return;
				}

				long start = System.currentTimeMillis();
				event.getChannel().sendTyping().queue(v -> {
					long ping = System.currentTimeMillis() - start;
					event.getChannel().sendMessage(EmoteReference.MEGA + "My ping: " + ping + " ms - " + ratePing(ping) + "  `Websocket:" + event.getJDA().getPing() + "ms`").queue();
				});
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "Ping Command")
				.addField("Description:", "Plays Ping-Pong with Discord and prints out the result.", false)
				.build())
			.build());
	}

	private static String ratePing(long ping) {
		if (ping <= 1) return "supersonic speed! :upside_down:"; //just in case...
		if (ping <= 10) return "faster than Sonic! :smiley:";
		if (ping <= 100) return "great! :smiley:";
		if (ping <= 200) return "nice! :slight_smile:";
		if (ping <= 300) return "decent. :neutral_face:";
		if (ping <= 400) return "average... :confused:";
		if (ping <= 500) return "slightly slow. :slight_frown:";
		if (ping <= 600) return "kinda slow.. :frowning2:";
		if (ping <= 700) return "slow.. :worried:";
		if (ping <= 800) return "too slow. :disappointed:";
		if (ping <= 800) return "awful. :weary:";
		if (ping <= 900) return "bad. :sob: (helpme)";
		if (ping <= 1600) return "#BlameDiscord. :angry:";
		if (ping <= 10000) return "this makes no sense :thinking: #BlameSteven";
		return "slow af. :dizzy_face: ";
	}

	@RegisterCommand
	public static void stats(CommandRegistry cr) {
		cr.register("stats", Commands.newSimple(Category.INFO)

			.code((thiz, event, content, args) -> {
				if (content.isEmpty()) {
					List<Guild> guilds = MantaroSelf.getInstance().getGuilds();

					List<VoiceChannel> voiceChannels = MantaroSelf.getInstance().getVoiceChannels();

					CalculatedIntValues usersPerGuild = calculateInt(guilds, value -> value.getMembers().size());
					CalculatedIntValues onlineUsersPerGuild = calculateInt(guilds, value -> (int) value.getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count());
					CalculatedDoubleValues onlineUsersPerUserPerGuild = calculateDouble(guilds, value -> (double) value.getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() / (double) value.getMembers().size() * 100);
					CalculatedIntValues textChannelsPerGuild = calculateInt(guilds, value -> value.getTextChannels().size());
					CalculatedIntValues voiceChannelsPerGuild = calculateInt(guilds, value -> value.getVoiceChannels().size());
					long bG = MantaroSelf.getInstance().getGuilds().stream().filter(g -> g.getMembers().size() > 500).count();

					event.getChannel().sendMessage(
						new EmbedBuilder()
							.setColor(Color.PINK)
							.setAuthor("Mantaro Statistics", "https://github.com/Kodehawa/MantaroBot/", event.getJDA().getSelfUser().getAvatarUrl())
							.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
							.setDescription("Well... I did my math!")
							.addField("Users per Guild", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", usersPerGuild.min, usersPerGuild.avg, usersPerGuild.max), true)
							.addField("Online Users per Server", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", onlineUsersPerGuild.min, onlineUsersPerGuild.avg, onlineUsersPerGuild.max), true)
							.addField("Online Users per Users per Server", String.format(Locale.ENGLISH, "Min: %.1f%%\nAvg: %.1f%%\nMax: %.1f%%", onlineUsersPerUserPerGuild.min, onlineUsersPerUserPerGuild.avg, onlineUsersPerUserPerGuild.max), true)
							.addField("Text Channels per Server", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", textChannelsPerGuild.min, textChannelsPerGuild.avg, textChannelsPerGuild.max), true)
							.addField("Voice Channels per Server", String.format(Locale.ENGLISH, "Min: %d\nAvg: %.1f\nMax: %d", voiceChannelsPerGuild.min, voiceChannelsPerGuild.avg, voiceChannelsPerGuild.max), true)
							.addField("Total commands (including custom)", String.valueOf(CommandProcessor.REGISTRY.commands().size()), true)
							.addField("Big Servers", String.valueOf(bG), true)
							.build()
					).queue();
					return;
				}

				if (args[0].equals("usage")) {
					event.getChannel().sendMessage(new EmbedBuilder()
						.setAuthor("Mantaro's usage information", null, "https://puu.sh/sMsVC/576856f52b.png")
						.setDescription("Hardware and usage information.")
						.setThumbnail("https://puu.sh/suxQf/e7625cd3cd.png")
						.addField("Threads:", getThreadCount() + " Threads", true)
						.addField("Memory Usage:", getTotalMemory() - getFreeMemory() + "MB/" + getMaxMemory() + "MB", true)
						.addField("CPU Cores:", getAvailableProcessors() + " Cores", true)
						.addField("CPU Usage:", getVpsCPUUsage() + "%", true)
						.addField("Assigned Memory:", getTotalMemory() + "MB", true)
						.addField("Remaining from assigned:", getFreeMemory() + "MB", true)
						.build()
					).queue();
					return;
				}

				if (args[0].equals("host")) {
					EmbedBuilder embedBuilder = new EmbedBuilder()
						.setAuthor("Mantaro's VPS information", null, "https://puu.sh/sMsVC/576856f52b.png")
						.setThumbnail("https://puu.sh/suxQf/e7625cd3cd.png")
						.addField("CPU Usage", String.format("%.2f", getVpsCPUUsage()) + "%", true)
						.addField("RAM (TOTAL/FREE/USED)", String.format("%.2f", getVpsMaxMemory()) + "GB/" + String.format("%.2f", getVpsFreeMemory())
							+ "GB/" + String.format("%.2f", getVpsUsedMemory()) + "GB", false);

					event.getChannel().sendMessage(embedBuilder.build()).queue();
					return;
				}

				if (args[0].equals("cmds")) {
					if (args.length > 1) {
						String what = args[1];
						if (what.equals("total")) {
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.TOTAL_CMDS, thiz.baseEmbed(event, "Command Stats | Total")).build()).queue();
							return;
						}

						if (what.equals("daily")) {
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.DAY_CMDS, thiz.baseEmbed(event, "Command Stats | Daily")).build()).queue();
							return;
						}

						if (what.equals("hourly")) {
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.HOUR_CMDS, thiz.baseEmbed(event, "Command Stats | Hourly")).build()).queue();
							return;
						}

						if (what.equals("now")) {
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.MINUTE_CMDS, thiz.baseEmbed(event, "Command Stats | Now")).build()).queue();
							return;
						}
					}

					//Default
					event.getChannel().sendMessage(thiz.baseEmbed(event, "Command Stats")
						.addField("Now", CommandStatsManager.resume(CommandStatsManager.MINUTE_CMDS), false)
						.addField("Hourly", CommandStatsManager.resume(CommandStatsManager.HOUR_CMDS), false)
						.addField("Daily", CommandStatsManager.resume(CommandStatsManager.DAY_CMDS), false)
						.addField("Total", CommandStatsManager.resume(CommandStatsManager.TOTAL_CMDS), false)
						.build()
					).queue();

					return;
				}

				thiz.onHelp(event);
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "Statistics command")
				.setDescription("See the bot, usage or vps statistics")
				.addField("Usage", prefix() + "stats <usage/host/cmds/guilds>", true)
				.build())
			.build());
	}

	@RegisterCommand
	public static void userinfo(CommandRegistry cr) {
		cr.register("userinfo", Commands.newSimple(Category.INFO)
			.code((thiz, event, content, args) -> {
				List<User> mentionedUsers = usersMentioned(event.getMessage());
				User user = mentionedUsers.size() > 0 ? mentionedUsers.get(0) : event.getAuthor();
				Member member = event.getGuild() == null ? null : event.getGuild().getMember(user);

				EmbedBuilder embed = new EmbedBuilder()
					.setAuthor(String.format("User info for %s#%s", user.getName(), user.getDiscriminator()), null, event.getAuthor().getEffectiveAvatarUrl())
					.setThumbnail(user.getAvatarUrl()).addField("Account Created:", user.getCreationTime().format(DateTimeFormatter.ISO_DATE).replace("Z", ""), true)
					.setFooter("User ID: " + user.getId(), null);

				Game game = DiscordUtils.game(user, member);
				OnlineStatus status = DiscordUtils.status(user, member);

				embed.addField("Playing:", game == null ? "None" : game.getName(), false)
					.addField("Status:", Utils.capitalize((status != null ? status : OnlineStatus.UNKNOWN).getKey().toLowerCase()), true);

				if (member != null) {
					String roles = member.getRoles().stream()
						.map(Role::getName)
						.collect(Collectors.joining(", "));

					if (roles.length() > MessageEmbed.TEXT_MAX_LENGTH)
						roles = roles.substring(0, MessageEmbed.TEXT_MAX_LENGTH - 4) + "...";

					embed.setColor(member.getColor())
						.addField("Join Date:", member.getJoinDate().format(DateTimeFormatter.ISO_DATE).replace("Z", ""), true)
						.addField("Voice Channel:", member.getVoiceState().getChannel() != null ? member.getVoiceState().getChannel().getName() : "None", false)
						.addField("Color:", member.getColor() == null ? "Default" : "#" + Integer.toHexString(member.getColor().getRGB()).substring(2).toUpperCase(), true)
						.addField("Roles: [" + String.valueOf(member.getRoles().size()) + "]", roles, true);
				}

				event.getChannel().sendMessage(embed.build()).queue();
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "UserInfo Command")
				.addField("Description:", "See information about specific users.", false)
				.addField("Usage:", "`" + prefix() + "userinfo @User`: Get information about the specific user.\n`" + prefix() + "userinfo`: Get information about yourself!", false)
				.build())
			.build());
	}

	@Override
	public void onPostLoad() {
		start();
	}
}