package net.kodehawa.mantaroself.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.commands.custom.EmbedJSON;
import net.kodehawa.mantaroself.core.CommandProcessor;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.HasPostLoad;
import net.kodehawa.mantaroself.modules.RegisterCommand;
import net.kodehawa.mantaroself.modules.commands.Category;
import net.kodehawa.mantaroself.modules.commands.Command;
import net.kodehawa.mantaroself.modules.commands.SimpleCommandCompat;
import net.kodehawa.mantaroself.utils.DiscordUtils;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;
import net.kodehawa.mantaroself.utils.data.GsonDataManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;
import static net.kodehawa.mantaroself.MantaroSelf.prefix;
import static net.kodehawa.mantaroself.commands.custom.Mapifier.dynamicResolve;
import static net.kodehawa.mantaroself.commands.custom.Mapifier.map;
import static net.kodehawa.mantaroself.commands.info.CommandStatsManager.log;
import static net.kodehawa.mantaroself.commands.info.HelpUtils.forType;
import static net.kodehawa.mantaroself.data.MantaroData.data;
import static net.kodehawa.mantaroself.utils.StringUtils.SPLIT_PATTERN;
import static net.kodehawa.mantaroself.utils.Utils.optional;

@Slf4j
@RegisterCommand.Class
//This will keep being SCC.
public class CustomCmds implements HasPostLoad {
	private static final Command customCommand = new Command() {
		private Random r = new Random();

		@Override
		public Category category() {
			return null;
		}

		@Override
		public MessageEmbed help(MessageReceivedEvent event) {
			return null;
		}

		private void handle(String cmdName, MessageReceivedEvent event) {
			List<String> values = data().get().custom.get(cmdName);
			if (values == null) return;

			String response = random(values);

			if (response.contains("$(")) {
				Map<String, String> dynamicMap = new HashMap<>();
				map("event", dynamicMap, event);
				response = dynamicResolve(response, dynamicMap);
			}

			int c = response.indexOf(':');
			if (c != -1) {
				String m = response.substring(0, c);
				String v = response.substring(c + 1);

				if (m.equals("embed")) {
					EmbedJSON embed;
					try {
						embed = GsonDataManager.gson(false).fromJson('{' + v + '}', EmbedJSON.class);
					} catch (Exception ignored) {
						event.getChannel().sendMessage(EmoteReference.ERROR2 + "The string ``{" + v + "}`` isn't a valid JSON.").queue();
						return;
					}

					event.getChannel().sendMessage(embed.gen(event)).queue();
					return;
				}

				if (m.equals("img") || m.equals("image") || m.equals("imgembed")) {
					if (!EmbedBuilder.URL_PATTERN.asPredicate().test(v)) {
						event.getChannel().sendMessage(EmoteReference.ERROR2 + "The string ``" + v + "`` isn't a valid link.").queue();
						return;
					}
					event.getChannel().sendMessage(
						new EmbedBuilder()
							.setImage(v)
							.setTitle(cmdName, null)
							.setColor(optional(event.getMember()).map(Member::getColor).orElse(null))
							.build()
					).queue();
					return;
				}
			}

			event.getChannel().sendMessage(response).queue();
		}

		@Override
		public void run(MessageReceivedEvent event, String cmdName, String ignored) {
			try {
				handle(cmdName, event);
			} catch (Exception e) {
				log.error("An exception occurred while processing a custom command:", e);
			}
			log("custom command");
		}

		@Override
		public boolean hidden() {
			return true;
		}

	};

	@RegisterCommand
	public static void custom(CommandRegistry cr) {
		Pattern addPattern = Pattern.compile(";", Pattern.LITERAL);

		cr.register("custom", new SimpleCommandCompat(Category.UTILS) {
			@Override
			public void call(MessageReceivedEvent event, String content, String[] args) {
				if (args.length < 1) {
					onHelp(event);
					return;
				}

				Map<String, List<String>> custom = data().get().custom;

				String action = args[0];

				if (action.equals("list") || action.equals("ls")) {
					List<String> commands = new ArrayList<>(custom.keySet());

					event.getChannel().sendMessage(
						baseEmbed(event, "Custom Commands")
							.setDescription(commands.isEmpty() ? "There is nothing here, just dust." : forType(commands))
							.build()
					).queue();
					return;
				}

				if (action.equals("clear")) {
					if (custom.isEmpty()) {
						event.getChannel().sendMessage(EmoteReference.ERROR + "There's no Custom Commands registered.").queue();
					}

					int size = custom.size();
					custom.clear();

					//save at data
					data().save();

					event.getChannel().sendMessage(EmoteReference.PENCIL + "Cleared **" + size + " Custom Commands**!").queue();
					return;
				}

				if (args.length < 2) {
					onHelp(event);
					return;
				}

				String cmd = args[1];

				if (action.equals("remove") || action.equals("rm")) {
					if (custom.remove(cmd) == null) {
						event.getChannel().sendMessage(EmoteReference.ERROR2 + "There's no Custom Command ``" + cmd + "`` registered.").queue();
					} else {
						//clear commands if none
						if (custom.keySet().stream().noneMatch(s -> s.endsWith(":" + cmd)))
							CommandProcessor.REGISTRY.commands().remove(cmd);

						//save at data
						data().save();

						event.getChannel().sendMessage(EmoteReference.PENCIL + "Removed Custom Command ``" + cmd + "``!").queue();
					}

					return;
				}

				if (action.equals("raw")) {

					List<String> responses = custom.get(cmd);
					if (responses == null) {
						event.getChannel().sendMessage(EmoteReference.ERROR2 + "There's no Custom Command ``" + cmd + "`` registered.").queue();
						return;
					}

					Pair<String, Integer> pair = DiscordUtils.embedList(responses, Object::toString);

					event.getChannel().sendMessage(baseEmbed(event, "Command ``" + cmd + "``:")
						.setDescription(pair.getLeft())
						.setFooter("(Showing " + pair.getRight() + " responses of " + responses.size() + ")", null)
						.build()
					).queue();
					return;
				}

				if (args.length < 3) {
					onHelp(event);
					return;
				}

				String value = args[2];

				if (action.equals("rename")) {
					if (CommandProcessor.REGISTRY.commands().containsKey(value) && !CommandProcessor.REGISTRY.commands().get(value).equals(customCommand)) {
						event.getChannel().sendMessage(EmoteReference.ERROR + "A command already exists with this name!").queue();
						return;
					}

					List<String> oldCustom = custom.remove(cmd);

					if (oldCustom == null) {
						event.getChannel().sendMessage(EmoteReference.ERROR2 + "There's no Custom Command ``" + cmd + "`` registered.").queue();
						return;
					}

					custom.put(value, oldCustom);

					//add mini-hack
					CommandProcessor.REGISTRY.commands().put(cmd, customCommand);

					//clear commands if none
					if (custom.keySet().stream().noneMatch(cmd::equals))
						CommandProcessor.REGISTRY.commands().remove(cmd);

					//save at data
					data().save();

					event.getChannel().sendMessage(EmoteReference.CORRECT + "Renamed command ``" + cmd + "`` to ``" + value + "``!").queue();
				}

				if (action.equals("add")) {
					if (CommandProcessor.REGISTRY.commands().containsKey(cmd) && !CommandProcessor.REGISTRY.commands().get(cmd).equals(customCommand)) {
						event.getChannel().sendMessage(EmoteReference.ERROR + "A command already exists with this name!").queue();
						return;
					}

					custom.put(cmd, Arrays.asList(addPattern.split(value)));

					//add mini-hack
					CommandProcessor.REGISTRY.commands().put(cmd, customCommand);

					//save at data
					data().save();

					event.getChannel().sendMessage(EmoteReference.CORRECT + "Saved to command ``" + cmd + "``!").queue();

					return;
				}

				onHelp(event);
			}

			@Override
			public String[] splitArgs(String content) {
				return SPLIT_PATTERN.split(content, 3);
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "CustomCommand Manager")
					.addField("Description:", "Manages the Custom Commands of the Guild.", false)
					.addField(
						"Usage:",
						"`" + prefix() + "custom`: Shows this help\n" +
							"`" + prefix() + "custom <list|ls> [detailed]`: List all commands. If detailed is supplied, it prints the responses of each command.\n" +
							"`" + prefix() + "custom clear`: Remove all Custom Commands.\n" +
							"`" + prefix() + "custom raw <command>`: Get the raw response of a Custom Command.\n" +
							"`" + prefix() + "custom rename <command> <name>`: Renames a Custom Command.\n" +
							"`" + prefix() + "custom add <name> <responses>`: Add a new Command with the response provided. (A list of modifiers can be found on [here](https://hastebin.com/xolijewitu.http)\n" +
							"`" + prefix() + "custom <remove|rm> <name>`: Removes a command with an specific name.",
						false
					).build();
			}
		});
	}

	@Override
	public void onPostLoad() {
		Map<String, List<String>> custom = data().get().custom;

		//We create a new HashMap to work-around any CME
		new HashMap<>(data().get().custom).forEach((name, responses) -> {
			if (CommandProcessor.REGISTRY.commands().containsKey(name) && !CommandProcessor.REGISTRY.commands().get(name).equals(customCommand)) {
				custom.remove(name);
				custom.put('_' + name, responses);
			}

			//add mini-hack
			CommandProcessor.REGISTRY.commands().put('_' + name, customCommand);
		});

		data().save();
	}
}
