package net.kodehawa.mantaroself.commands;

import bsh.Interpreter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.MantaroSelf;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.Event;
import net.kodehawa.mantaroself.modules.Module;
import net.kodehawa.mantaroself.modules.commands.SimpleCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;
import static net.kodehawa.mantaroself.MantaroSelf.prefix;
import static net.kodehawa.mantaroself.data.MantaroData.data;
import static net.kodehawa.mantaroself.utils.StringUtils.SPLIT_PATTERN;

@Module
@Slf4j
public class ConfigCmds {
	public abstract static class Evaluator {
		private final String name;

		public Evaluator(String name) {
			this.name = name;
		}

		public abstract Object eval(MessageReceivedEvent event, String code);

		@Override
		public String toString() {
			return name;
		}
	}

	public static void actions(CommandRegistry registry) {}

	@Event
	public static void eval(CommandRegistry registry) {
		Map<String, Evaluator> evaluators = new HashMap<>();

		evaluators.put("js", new Evaluator("Javascript (Nashorn)") {
			@Override
			public Object eval(MessageReceivedEvent event, String code) {
				ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
				nashorn.put("jda", event.getJDA());
				nashorn.put("event", event);
				nashorn.put("guild", event.getGuild());
				nashorn.put("channel", event.getChannel());
				nashorn.put("evaluators", evaluators);

				try {
					return nashorn.eval(String.join("\n",
						"load(\"nashorn:mozilla_compat.js\");",
						"imports = new JavaImporter(java.util, java.io, java.net);",
						"(function() {",
						"with(imports) {",
						code,
						"}",
						"})()"
					));
				} catch (Exception e) {
					return e;
				}
			}
		});

		evaluators.put("bsh", new Evaluator("Beanshell (Java)") {
			@Override
			public Object eval(MessageReceivedEvent event, String code) {
				Interpreter beanshell = new Interpreter();
				try {
					beanshell.set("jda", event.getJDA());
					beanshell.set("event", event);
					beanshell.set("guild", event.getGuild());
					beanshell.set("channel", event.getChannel());
					beanshell.set("evaluators", evaluators);

					return beanshell.eval(String.join("\n",
						"import *;",
						code
					));
				} catch (Exception e) {
					return e;
				}
			}
		});

//		evaluators.put("groovy", new Evaluator("Groovy") {
//			@Override
//			public Object eval(MessageReceivedEvent event, String code) {
//				Binding groovyEnv = new Binding();
//				groovyEnv.setVariable("jda", event.getJDA());
//				groovyEnv.setVariable("event", event);
//				groovyEnv.setVariable("guild", event.getGuild());
//				groovyEnv.setVariable("channel", event.getChannel());
//				groovyEnv.setVariable("evaluators", evaluators);
//
//				GroovyShell groovy = new GroovyShell(groovyEnv);
//				try {
//					return groovy.evaluate(code);
//				} catch (Exception e) {
//					return e;
//				}
//			}
//		});

		registry.register("eval", new SimpleCommand(Category.UTILS) {
			@Override
			public void call(MessageReceivedEvent event, String content, String[] args) {
				if (args.length < 2) {
					onHelp(event);
					return;
				}

				Evaluator evaluator = evaluators.get(args[0]);
				if (evaluator == null) {
					onHelp(event);
					return;
				}

				Object result = evaluator.eval(event, args[1]);
				boolean errored = result instanceof Throwable;

				event.getChannel().sendMessage(new EmbedBuilder()
					.setAuthor("Evaluated " + (errored ? "and errored" : "with success"), null, event.getAuthor().getAvatarUrl())
					.setColor(errored ? Color.RED : Color.GREEN)
					.setDescription(result == null ? "Executed successfully with no objects returned" : ("Executed " + (errored ? "and errored: " : "successfully and returned: ") + result.toString()))
					.addField("Code:", args[0], false)
					.setFooter("Asked by: " + event.getAuthor().getName(), null)
					.build()
				).queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Eval Command")
					.setDescription("Evaluates Arbitrary code.\n")
					.addField("Usage:", prefix() + "eval <evaluator> <code>", false)
					.addField("Evaluators available:",
						evaluators.entrySet().stream().map(e -> "``" + e.getKey() + "`` - " + e.getValue().toString()).collect(Collectors.joining("\n")),
						false
					)
					.build();
			}

			@Override
			public String[] splitArgs(String content) {
				return SPLIT_PATTERN.split(content, 2);
			}
		});
	}

	@Event
	public static void setters(CommandRegistry registry) {
		registry.register("game", new SimpleCommand(Category.SELF) {

			@Override
			public Category category() {
				return Category.SELF;
			}

			@Override
			public void call(MessageReceivedEvent event, String content, String[] args) {
				if (args.length < 1) {
					onHelp(event);
					return;
				}

				String action = args[0];

				if (action.equals("clear")) {
					event.getJDA().getPresence().setGame(null);
					event.getChannel().sendMessage("Game cleared!").queue();
					return;
				}

				if (args.length < 2) {
					onHelp(event);
					return;
				}

				String value = args[1];

				if (action.equals("set")) {
					event.getJDA().getPresence().setGame(Game.of(value));
					event.getChannel().sendMessage("Game set to ``" + value + "``!").queue();
					return;
				}

				if (action.equals("stream")) {
					event.getJDA().getPresence().setGame(Game.of(value, "https://twitch.tv/ "));
					event.getChannel().sendMessage("Now streaming ``" + value + "``!").queue();
					return;
				}

				onHelp(event);
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Game Command")
					.setDescription("**Usage**:\n" +
						prefix() + "game set <game>: Sets a game\n" +
						prefix() + "game stream <game>: Sets your game to a Stream\n" +
						prefix() + "game clear: Clears your game")
					.build();
			}

			@Override
			public String[] splitArgs(String content) {
				return SPLIT_PATTERN.split(content, 2);
			}
		});
	}

	@Event
	public static void shutdown(CommandRegistry registry) {
		registry.register("shutdown", new SimpleCommand(Category.UTILS) {
			@Override
			public void call(MessageReceivedEvent event, String content, String[] args) {
				if (!content.equals("force")) {
					try {
						List<String> shutdownQuotes = data().get().botInfo.shutdownQuotes;
						if (!shutdownQuotes.isEmpty())
							event.getChannel().sendMessage(random(shutdownQuotes)).complete();

						event.getMessage().delete().complete();

						MantaroSelf.instance().shutdown(true);
					} catch (Exception e) {
						log.warn(EmoteReference.ERROR + "Couldn't prepare shutdown." + e.toString(), e);
						return;
					}
				}

				System.exit(0);
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Shutdown Command")
					.setDescription("Shuts the Selfbot off.\nExecute with ``force`` as parameter to force exit.")
					.build();
			}
		});
	}
}
