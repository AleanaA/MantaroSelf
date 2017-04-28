package net.kodehawa.mantaroself.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.MantaroInfo;
import net.kodehawa.mantaroself.MantaroSelf;
import net.kodehawa.mantaroself.commands.info.CommandStatsManager;
import net.kodehawa.mantaroself.commands.info.StatsHelper.CalculatedDoubleValues;
import net.kodehawa.mantaroself.commands.info.StatsHelper.CalculatedIntValues;
import net.kodehawa.mantaroself.core.CommandProcessor;
import net.kodehawa.mantaroself.core.listeners.command.CommandListener;
import net.kodehawa.mantaroself.data.MantaroData;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.Event;
import net.kodehawa.mantaroself.modules.Module;
import net.kodehawa.mantaroself.modules.commands.SimpleCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.modules.commands.base.Command;
import net.kodehawa.mantaroself.modules.events.PostLoadEvent;
import net.kodehawa.mantaroself.utils.Utils;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static net.kodehawa.mantaroself.MantaroSelf.prefix;
import static net.kodehawa.mantaroself.commands.info.AsyncInfoMonitor.*;
import static net.kodehawa.mantaroself.commands.info.HelpUtils.forType;
import static net.kodehawa.mantaroself.commands.info.StatsHelper.calculateDouble;
import static net.kodehawa.mantaroself.commands.info.StatsHelper.calculateInt;
import static net.kodehawa.mantaroself.data.MantaroData.data;
import static net.kodehawa.mantaroself.utils.DiscordUtils.*;

@Module
public class InfoCmds {
	@Event
	public static void about(CommandRegistry registry) {
		registry.register("about", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				List<Guild> guilds = MantaroSelf.instance().getGuilds();
				int guildCount = guilds.size();
				int usersCount = MantaroSelf.instance().getUsers().size();
				long onlineCount = guilds.stream()
					.flatMap(guild -> guild.getMembers().stream())
					.filter(user -> !user.getOnlineStatus().equals(OnlineStatus.OFFLINE))
					.map(member -> member.getUser().getId())
					.distinct()
					.count();
				int tcCount = MantaroSelf.instance().getTextChannels().size();
				int vcCount = MantaroSelf.instance().getVoiceChannels().size();
				long millis = ManagementFactory.getRuntimeMXBean().getUptime();
				long seconds = millis / 1000;
				long minutes = seconds / 60;
				long hours = minutes / 60;
				long days = hours / 24;

				event.getChannel().sendMessage(baseEmbed(event, data().get().botInfo.aboutTitle)
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
					.setFooter("Commands this session: " + CommandListener.getCommandTotal(), event.getJDA().getSelfUser().getAvatarUrl())
					.build()
				).queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "About Command")
					.addField("Description:", "Read info about Mantaro!", false)
					.addField("Information", prefix() + "about credits lists everyone who has helped on the bot's development", false)
					.build();
			}
		});
	}

	@Event
	public static void avatar(CommandRegistry registry) {
		registry.register("avatar", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				List<User> mentioned = usersMentioned(event.getMessage());
				User user = mentioned.isEmpty() ? event.getAuthor() : mentioned.get(0);
				event.getChannel().sendMessage(String.format("Avatar for: **%s**\n%s", user.getName(), user.getAvatarUrl())).queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Avatar")
					.setDescription("Get user avatar URLs")
					.addField("Usage",
						prefix() + "avatar - Get your avatar url" +
							"\n " + prefix() + "avatar <mention> - Get a user's avatar url.", false)
					.build();
			}
		});
	}

	@Event
	public static void guildinfo(CommandRegistry registry) {
		registry.register("guildinfo", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
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

				if (roles.length() > 1024) roles = roles.substring(0, 1024 - 4) + "...";

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
					.addField("Region", guild.getRegion() == null ? "Unknown" : guild.getRegion().getName(), true)
					.addField("Roles (" + guild.getRoles().size() + ")", roles, false)
					.setFooter("Server ID: " + String.valueOf(guild.getId()), null)
					.build()
				).queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Guild Info Command")
					.addField("Description:", "See your guild's current stats.\n*Can be only issued from a Guild.*", false)
					.setColor(event.getGuild().getOwner().getColor() == null ? Color.ORANGE : event.getGuild().getOwner().getColor())
					.build();
			}
		});
	}

	@Event
	public static void help(CommandRegistry registry) {
		Random r = new Random();
		List<String> jokes = Collections.unmodifiableList(Arrays.asList(
			"Yo damn I heard you like help, because you just issued the help command to get the help about the help command.",
			"Congratulations, you managed to use the help command.",
			"Helps you to help yourself.",
			"Help Inception.",
			"A help helping helping helping help."
		));

		registry.register("help", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				if (content.isEmpty()) {
					String prefix = MantaroData.config().get().prefix();

					EmbedBuilder embed = baseEmbed(event, "Commands:")
						.setDescription("Command help. For extended usage please use " + String.format("%shelp <command>.", prefix))
						.setFooter(String.format("To check command usage, type %shelp <command> // -> Commands: " +
							CommandProcessor.REGISTRY.commands().values().stream()
								.filter(command -> command.category() != null).count(), prefix), null);

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
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Help Command")
					.addField("Description:", jokes.get(r.nextInt(jokes.size())), false)
					.addField(
						"Usage:",
						"`" + prefix() + "help`: Return information about who issued the command.\n`" + prefix() + "help <command>`: Return information about the command specified.",
						false
					).build();
			}
		});
	}

	@Event
	public static void info(CommandRegistry registry) {
		registry.register("info", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				List<Guild> guilds = MantaroSelf.instance().getGuilds();

				event.getChannel().sendMessage("```prolog\n"
					+ "---Selfbot Technical Information---\n\n"
					+ "Commands: " + CommandProcessor.REGISTRY.commands().values().stream().filter(command -> command.category() != null).count() + "\n"
					+ "Bot Version: " + MantaroInfo.VERSION + "\n"
					+ "JDA Version: " + JDAInfo.VERSION + "\n"
					+ "API Responses: " + MantaroSelf.instance().getResponseTotal() + "\n"
					+ "CPU Usage: " + getVpsCPUUsage() + "%" + "\n"
					+ "CPU Cores: " + getAvailableProcessors() + "\n"
					+ "\n\n ------------------ \n\n"
					+ "Guilds: " + guilds.size() + "\n"
					+ "Users: " + guilds.stream().flatMap(guild -> guild.getMembers().stream()).map(user -> user.getUser().getId()).distinct().count() + "\n"
					+ "Threads: " + Thread.activeCount() + "\n"
					+ "Executed Commands: " + CommandListener.getCommandTotal() + "\n"
					+ "Memory: " + (getTotalMemory() - getFreeMemory()) + "MB / " + getMaxMemory() + "MB" + "\n"
					+ "```").queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return baseEmbed(event, "Info")
					.setDescription("Gets the bot technical information")
					.build();
			}
		});
	}

	@Event
	public static void onPostLoad(PostLoadEvent e) {
		start();
	}

	@Event
	public static void ping(CommandRegistry registry) {
		registry.register("ping", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {

				long start = System.currentTimeMillis();
				event.getChannel().sendTyping().queue(v -> {
					long ping = System.currentTimeMillis() - start;
					event.getChannel().sendMessage(String.format("%s**Ping**: `%dms/%dms` (API/Websocket)", EmoteReference.MEGA, ping, event.getJDA().getPing())).queue();
				});
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Ping Command")
					.addField("Description:", "Plays Ping-Pong with Discord and prints out the result.", false)
					.build();
			}
		});
	}

	@Event
	public static void stats(CommandRegistry registry) {
		registry.register("stats", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				if (content.isEmpty()) {
					List<Guild> guilds = MantaroSelf.instance().getGuilds();

					List<VoiceChannel> voiceChannels = MantaroSelf.instance().getVoiceChannels();

					CalculatedIntValues usersPerGuild = calculateInt(guilds, value -> value.getMembers().size());
					CalculatedIntValues onlineUsersPerGuild = calculateInt(guilds, value -> (int) value.getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count());
					CalculatedDoubleValues onlineUsersPerUserPerGuild = calculateDouble(guilds, value -> (double) value.getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() / (double) value.getMembers().size() * 100);
					CalculatedIntValues textChannelsPerGuild = calculateInt(guilds, value -> value.getTextChannels().size());
					CalculatedIntValues voiceChannelsPerGuild = calculateInt(guilds, value -> value.getVoiceChannels().size());
					long bG = MantaroSelf.instance().getGuilds().stream().filter(g -> g.getMembers().size() > 500).count();

					event.getChannel().sendMessage(
						new EmbedBuilder()
							.setAuthor("Statistics", null, event.getJDA().getSelfUser().getAvatarUrl())
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
						.setAuthor("Selfbot's Host Information", null, "https://puu.sh/sMsVC/576856f52b.png")
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
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.TOTAL_CMDS, baseEmbed(event, "Command Stats | Total")).build()).queue();
							return;
						}

						if (what.equals("daily")) {
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.DAY_CMDS, baseEmbed(event, "Command Stats | Daily")).build()).queue();
							return;
						}

						if (what.equals("hourly")) {
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.HOUR_CMDS, baseEmbed(event, "Command Stats | Hourly")).build()).queue();
							return;
						}

						if (what.equals("now")) {
							event.getChannel().sendMessage(CommandStatsManager.fillEmbed(CommandStatsManager.MINUTE_CMDS, baseEmbed(event, "Command Stats | Now")).build()).queue();
							return;
						}
					}

					//Default
					event.getChannel().sendMessage(baseEmbed(event, "Command Stats")
						.addField("Now", CommandStatsManager.resume(CommandStatsManager.MINUTE_CMDS), false)
						.addField("Hourly", CommandStatsManager.resume(CommandStatsManager.HOUR_CMDS), false)
						.addField("Daily", CommandStatsManager.resume(CommandStatsManager.DAY_CMDS), false)
						.addField("Total", CommandStatsManager.resume(CommandStatsManager.TOTAL_CMDS), false)
						.build()
					).queue();

					return;
				}

				onHelp(event);
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Statistics command")
					.setDescription("See the bot, usage or host statistics")
					.addField("Usage", prefix() + "stats <usage/host/cmds>", true)
					.build();
			}
		});
	}

	@Event
	public static void userinfo(CommandRegistry registry) {
		registry.register("userinfo", new SimpleCommand(Category.INFO) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				List<User> mentionedUsers = usersMentioned(event.getMessage());
				User user = mentionedUsers.size() > 0 ? mentionedUsers.get(0) : event.getAuthor();
				Member member = event.getGuild() == null ? null : event.getGuild().getMember(user);

				EmbedBuilder embed = new EmbedBuilder()
					.setAuthor(String.format("User info for %s#%s", user.getName(), user.getDiscriminator()), null, event.getAuthor().getEffectiveAvatarUrl())
					.setThumbnail(user.getAvatarUrl()).addField("Account Created:", user.getCreationTime().format(DateTimeFormatter.ISO_DATE).replace("Z", ""), true)
					.setFooter("User ID: " + user.getId(), null);

				Game game = game(user, member);
				OnlineStatus status = status(user, member);

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
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "UserInfo Command")
					.addField("Description:", "See information about specific users.", false)
					.addField("Usage:", "`" + prefix() + "userinfo @User`: Get information about the specific user.\n`" + prefix() + "userinfo`: Get information about yourself!", false)
					.build();
			}
		});
	}
}