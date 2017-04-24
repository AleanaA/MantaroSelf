package net.kodehawa.mantaroself.commands;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.commands.action.ImageActionCmd;
import net.kodehawa.mantaroself.commands.action.TextActionCmd;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.Event;
import net.kodehawa.mantaroself.modules.Module;
import net.kodehawa.mantaroself.modules.commands.SimpleCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;
import net.kodehawa.mantaroself.utils.data.DataManager;
import net.kodehawa.mantaroself.utils.data.SimpleFileDataManager;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;
import static net.kodehawa.mantaroself.MantaroSelf.prefix;
import static net.kodehawa.mantaroself.utils.DiscordUtils.usersMentioned;

@Module
public class ActionCmds {
	public static final DataManager<List<String>> BLEACH = new SimpleFileDataManager("assets/mantaroself/gifs/bleach.txt");
	public static final DataManager<List<String>> GREETINGS = new SimpleFileDataManager("assets/mantaroself/texts/greetings.txt");
	public static final DataManager<List<String>> HUGS = new SimpleFileDataManager("assets/mantaroself/gifs/hugs.txt");
	public static final DataManager<List<String>> KISSES = new SimpleFileDataManager("assets/mantaroself/gifs/kisses.txt");
	public static final DataManager<List<String>> NOBLE = new SimpleFileDataManager("assets/mantaroself/texts/noble.txt");
	public static final DataManager<List<String>> PATS = new SimpleFileDataManager("assets/mantaroself/gifs/pats.txt");
	public static final DataManager<List<String>> TSUNDERE = new SimpleFileDataManager("assets/mantaroself/texts/tsundere.txt");

	@Event
	public static void action(CommandRegistry registry) {
		registry.register("action", new SimpleCommand(Category.ACTION) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				String noArgs = content.split(" ")[0];
				MessageChannel channel = event.getChannel();
				switch (noArgs) {
					case "facedesk":
						channel.sendMessage("http://puu.sh/rK6E7/0b745e5544.gif").queue();
						break;
					case "nom":
						channel.sendMessage("http://puu.sh/rK7t2/330182c282.gif").queue();
						break;
					case "bleach":
						channel.sendMessage(random(BLEACH.get())).queue();
						break;
					case "noble":
						channel.sendMessage(EmoteReference.TALKING + random(NOBLE.get()) + " -Noble").queue();
						break;
					default:
						onHelp(event);
				}
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Action commands")
					.setDescription("**Usage**\n" +
						prefix() + "action bleach: Random image of someone drinking bleach.\n" +
						prefix() + "action facedesk: Facedesks.\n" +
						prefix() + "action nom: nom nom.\n" +
						prefix() + "action noble: Random Lost Pause quote."
					).build();
			}
		});
	}

	@Event
	public static void bloodsuck(CommandRegistry registry) {
		registry.register("bloodsuck", new SimpleCommand(Category.ACTION) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				List<User> mentionedUsers = usersMentioned(event.getMessage());
				if (mentionedUsers.isEmpty()) {
					event.getChannel().sendFile(ImageActionCmd.CACHE.getInput("http://imgur.com/ZR8Plmd.png"), "suck.png", null).queue();
				} else {
					String bString = mentionedUsers.stream().map(IMentionable::getAsMention).collect(Collectors
						.joining(" "));
					String bs = String.format(EmoteReference.TALKING + "%s sucks the blood of %s", event.getAuthor().getAsMention(),
						bString);
					event.getChannel().sendFile(ImageActionCmd.CACHE.getInput("http://imgur.com/ZR8Plmd.png"), "suck.png",
						new MessageBuilder().append(bs).build()).queue();
				}
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Bloodsuck")
					.setDescription("Suck the blood of the mentioned users")
					.build();
			}
		});
	}

	@Event
	public static void lewd(CommandRegistry registry) {
		registry.register("lewd", new SimpleCommand(Category.ACTION) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				String lood = usersMentioned(event.getMessage()).stream()
					.map(IMentionable::getAsMention)
					.collect(Collectors.joining(" "));

				event.getChannel().sendFile(
					ImageActionCmd.CACHE.getInput("http://imgur.com/LJfZYau.png"),
					"lewd.png",
					new MessageBuilder().append(lood).append(" Y-You lewdie!").build()
				).queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Lewd")
					.setDescription("lewd").build();
			}
		});
	}

	@Event
	public static void meow(CommandRegistry registry) {
		registry.register("meow", new SimpleCommand(Category.ACTION) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				Message receivedMessage = event.getMessage();
				List<User> mentionedUsers = usersMentioned(receivedMessage);
				if (!mentionedUsers.isEmpty()) {
					String mew = mentionedUsers.stream().map(IMentionable::getAsMention).collect(Collectors.joining(" "));
					event.getChannel().sendFile(ImageActionCmd.CACHE.getInput("http://imgur.com/yFGHvVR.gif"), "mew.gif",
						new MessageBuilder().append(EmoteReference.TALKING).append(String.format("*meows at %s.*", mew)).build()).queue();
				} else {
					event.getChannel().sendFile(ImageActionCmd.CACHE.getInput("http://imgur.com/yFGHvVR.gif"), "mew.gif",
						new MessageBuilder().append(":speech_balloon: Meeeeow.").build()).queue();
				}
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Meow command")
					.setDescription("Meow either to a person or the sky.")
					.setColor(Color.cyan)
					.build();
			}
		});
		registry.registerAlias("mew", "meow");
	}

	@Event
	public static void register(CommandRegistry cr) {

		//pat();
		cr.register("pat", new ImageActionCmd(
			"Pat", "Pats the specified user.",
			"pat.gif", EmoteReference.TALKING + "%s you have been patted by %s", PATS.get()));

		//hug();
		cr.register("hug", new ImageActionCmd(
			"Hug", "Hugs the specified user.",
			"hug.gif", EmoteReference.TALKING + "%s you have been hugged by %s", HUGS.get()
		));

		//kiss();
		cr.register("kiss", new ImageActionCmd(
			"Kiss", "Kisses the specified user.",
			"kiss.gif", EmoteReference.TALKING + "%s you have been kissed by %s", KISSES.get()
		));

		//greet();
		cr.register("greet", new TextActionCmd(
			"Greet", "Sends a random greeting",
			EmoteReference.TALKING + "%s", GREETINGS.get()
		));

		//tsundere();
		cr.register("tsundere", new TextActionCmd(
			"Tsundere Command", "Y-You baka!",
			EmoteReference.MEGA + "%s", TSUNDERE.get()
		));

	}
}
