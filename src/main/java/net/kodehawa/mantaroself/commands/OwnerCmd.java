package net.kodehawa.mantaroself.commands;

import bsh.Interpreter;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantaroself.MantaroSelf;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.RegisterCommand;
import net.kodehawa.mantaroself.modules.commands.Category;
import net.kodehawa.mantaroself.modules.commands.SimpleCommandCompat;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;
import static net.kodehawa.mantaroself.utils.StringUtils.SPLIT_PATTERN;

@Slf4j
@RegisterCommand.Class
public class OwnerCmd {
	private interface Evaluator {
		Object eval(GuildMessageReceivedEvent event, String code);
	}

	private static final String[] sleepQuotes = {"*goes to sleep*", "Mama, It's not night yet. *hmph*. okay. bye.", "*grabs pillow*", "*~~goes to sleep~~ goes to dreaming dimension*", "*grabs plushie*", "Momma, where's my Milk cup? *drinks and goes to sleep*"};

	private static String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
		boolean first = true;
		StringBuilder ret = new StringBuilder();
		for (int size : sizes) {
			if (first) {
				first = false;
				ret.append(left).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
			} else {
				ret.append(middle).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
			}
		}
		return ret.append(right).append("\n").toString();
	}

	public static String getStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static String makeAsciiTable(List<String> headers, List<List<String>> table, List<String> footer) {
		StringBuilder sb = new StringBuilder();
		int padding = 1;
		int[] widths = new int[headers.size()];
		for (int i = 0; i < widths.length; i++) {
			widths[i] = 0;
		}
		for (int i = 0; i < headers.size(); i++) {
			if (headers.get(i).length() > widths[i]) {
				widths[i] = headers.get(i).length();
				if (footer != null) {
					widths[i] = Math.max(widths[i], footer.get(i).length());
				}
			}
		}
		for (List<String> row : table) {
			for (int i = 0; i < row.size(); i++) {
				String cell = row.get(i);
				if (cell.length() > widths[i]) {
					widths[i] = cell.length();
				}
			}
		}
		sb.append("```").append("\n");
		String formatLine = "|";
		for (int width : widths) {
			formatLine += " %-" + width + "s |";
		}
		formatLine += "\n";
		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
		sb.append(String.format(formatLine, headers.toArray()));
		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
		for (List<String> row : table) {
			sb.append(String.format(formatLine, row.toArray()));
		}
		if (footer != null) {
			sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
			sb.append(String.format(formatLine, footer.toArray()));
		}
		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
		sb.append("```");
		return sb.toString();
	}

	@RegisterCommand
	public static void owner(CommandRegistry cr) {
		Map<String, Evaluator> evals = new HashMap<>();
		evals.put("js", (event, code) -> {
			ScriptEngine script = new ScriptEngineManager().getEngineByName("nashorn");
			script.put("jda", event.getJDA());
			script.put("event", event);
			script.put("guild", event.getGuild());
			script.put("channel", event.getChannel());

			try {
				return script.eval(String.join("\n",
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
		});

		evals.put("bsh", (event, code) -> {
			Interpreter interpreter = new Interpreter();
			try {
				interpreter.set("jda", event.getJDA());
				interpreter.set("event", event);
				interpreter.set("guild", event.getGuild());
				interpreter.set("channel", event.getChannel());

				return interpreter.eval(String.join("\n",
					"import *;",
					code
				));
			} catch (Exception e) {
				return e;
			}
		});

		evals.put("groovy", (event, code) -> {
			Binding b = new Binding();
			b.setVariable("jda", event.getJDA());
			b.setVariable("event", event);
			b.setVariable("guild", event.getGuild());
			b.setVariable("channel", event.getChannel());
			GroovyShell sh = new GroovyShell(b);
			try {
				return sh.evaluate(code);
			} catch (Exception e) {
				return e;
			}
		});

		//This command will keep being SimpleCommandCompat.
		cr.register("owner", new SimpleCommandCompat(Category.OWNER) {
			@Override
			public void call(GuildMessageReceivedEvent event, String content, String[] args) {
				if (args.length < 1) {
					onHelp(event);
					return;
				}

				String option = args[0];

				if (option.equals("shutdown") || option.equals("restart")) {
					try {
						prepareShutdown(event);
					} catch (Exception e) {
						log.warn(EmoteReference.ERROR + "Couldn't prepare shutdown." + e.toString(), e);
						return;
					}

					//If we manage to get here, there's nothing else except us.

					//Here in Darkness, everything is okay.
					//Listen to the waves, and let them fade away.

					System.exit(0);

					return;
				}

				if (option.equals("forceshutdown") || option.equals("forcerestart")) {

					try {
						prepareShutdown(event);
					} catch (Exception e) {
						log.warn(EmoteReference.ERROR + "Couldn't prepare shutdown. I don't care, I'm gonna restart anyway." + e.toString(), e);
					}

					//If we manage to get here, there's nothing else except us.

					//Here in Darkness, everything is okay.
					//Listen to the waves, and let them fade away.

					System.exit(0);

					return;
				}

				if (args.length < 2) {
					onHelp(event);
					return;
				}

				String value = args[1];

				String[] values = SPLIT_PATTERN.split(value, 2);
				if (values.length < 2) {
					onHelp(event);
					return;
				}

				String k = values[0], v = values[1];

				if (option.equals("varadd")) {
					try {
						String v1 = values[1];
						switch (values[0]) {
							case "pat":
								ActionCmds.PATS.get().add(v1);
								ActionCmds.PATS.save();
								event.getChannel().sendMessage(EmoteReference.CORRECT + "Added to pat list: " + v).queue();
								break;
							case "hug":
								ActionCmds.HUGS.get().add(v1);
								ActionCmds.HUGS.save();
								event.getChannel().sendMessage(EmoteReference.CORRECT + "Added to hug list: " + v).queue();
								break;
							case "greeting":
								ActionCmds.GREETINGS.get().add(content.replace("varadd greeting ", ""));
								ActionCmds.GREETINGS.save();
								event.getChannel().sendMessage(EmoteReference.CORRECT + "Added to greet list: " + content.replace("greeting ", "")).queue();
								break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					return;
				}

				if (option.equals("eval")) {
					Evaluator evaluator = evals.get(k);
					if (evaluator == null) {
						onHelp(event);
						return;
					}

					Object result = evaluator.eval(event, v);
					boolean errored = result instanceof Throwable;

					event.getChannel().sendMessage(new EmbedBuilder()
						.setAuthor("Evaluated " + (errored ? "and errored" : "with success"), null, event.getAuthor().getAvatarUrl())
						.setColor(errored ? Color.RED : Color.GREEN)
						.setDescription(result == null ? "Executed successfully with no objects returned" : ("Executed " + (errored ? "and errored: " : "successfully and returned: ") + result.toString()))
						.setFooter("Asked by: " + event.getAuthor().getName(), null)
						.build()
					).queue();

					return;
				}

				onHelp(event);
			}

			@Override
			public String[] splitArgs(String content) {
				return SPLIT_PATTERN.split(content, 2);
			}

			@Override
			public MessageEmbed help(GuildMessageReceivedEvent event) {
				return helpEmbed(event, "Owner command")
					.setDescription("~>owner shutdown/forceshutdown: Shutdowns the bot\n" +
						"~>owner restart/forcerestart: Restarts the bot.\n" +
						"~>owner scheduleshutdown time <time>: Schedules a fixed amount of seconds the bot will wait to be shutted down.\n" +
						"~>owner varadd <pat/hug/greeting/splash>: Adds a link or phrase to the specified list.\n" +
						"~>owner eval <bsh/js/groovy/m/cw> <line of code>: Evals a specified code snippet.\n" +
						"~>owner cw <info/eval>: Shows info or evals specified code in the Connection Watcher.\n" +
						"~>owner premium add <id> <days>: Adds premium to the specified user for x days.")
					.addField("Shush.", "If you aren't Adrian or Kode you shouldn't be looking at this, huh " + EmoteReference.EYES, false)
					.build();
			}

		});
	}

	private static void prepareShutdown(GuildMessageReceivedEvent event) {
		event.getChannel().sendMessage(random(sleepQuotes)).complete();

		MantaroSelf.getInstance().shutdown(true);
	}
}
