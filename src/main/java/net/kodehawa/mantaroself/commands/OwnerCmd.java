//package net.kodehawa.mantaroself.commands;
//
//import lombok.extern.slf4j.Slf4j;
//import net.dv8tion.jda.core.entities.MessageEmbed;
//import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
//import net.kodehawa.mantaroself.modules.CommandRegistry;
//import net.kodehawa.mantaroself.modules.RegisterCommand;
//import net.kodehawa.mantaroself.modules.commands.Category;
//import net.kodehawa.mantaroself.modules.commands.SimpleCommandCompat;
//import net.kodehawa.mantaroself.utils.commands.EmoteReference;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.Collections;
//import java.util.List;
//
//import static net.kodehawa.mantaroself.MantaroSelf.prefix;
//import static net.kodehawa.mantaroself.utils.StringUtils.SPLIT_PATTERN;
//
//@Slf4j
//@RegisterCommand.Class
//public class OwnerCmd {
//	private static String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
//		boolean first = true;
//		StringBuilder ret = new StringBuilder();
//		for (int size : sizes) {
//			if (first) {
//				first = false;
//				ret.append(left).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
//			} else {
//				ret.append(middle).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
//			}
//		}
//		return ret.append(right).append("\n").toString();
//	}
//
//	public static String getStackTrace(Throwable e) {
//		StringWriter sw = new StringWriter();
//		PrintWriter pw = new PrintWriter(sw);
//		e.printStackTrace(pw);
//		return sw.toString();
//	}
//
//	public static String makeAsciiTable(List<String> headers, List<List<String>> table, List<String> footer) {
//		StringBuilder sb = new StringBuilder();
//		int padding = 1;
//		int[] widths = new int[headers.size()];
//		for (int i = 0; i < widths.length; i++) {
//			widths[i] = 0;
//		}
//		for (int i = 0; i < headers.size(); i++) {
//			if (headers.get(i).length() > widths[i]) {
//				widths[i] = headers.get(i).length();
//				if (footer != null) {
//					widths[i] = Math.max(widths[i], footer.get(i).length());
//				}
//			}
//		}
//		for (List<String> row : table) {
//			for (int i = 0; i < row.size(); i++) {
//				String cell = row.get(i);
//				if (cell.length() > widths[i]) {
//					widths[i] = cell.length();
//				}
//			}
//		}
//		sb.append("```").append("\n");
//		String formatLine = "|";
//		for (int width : widths) {
//			formatLine += " %-" + width + "s |";
//		}
//		formatLine += "\n";
//		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
//		sb.append(String.format(formatLine, headers.toArray()));
//		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
//		for (List<String> row : table) {
//			sb.append(String.format(formatLine, row.toArray()));
//		}
//		if (footer != null) {
//			sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
//			sb.append(String.format(formatLine, footer.toArray()));
//		}
//		sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
//		sb.append("```");
//		return sb.toString();
//	}
//
//	@RegisterCommand
//	public static void owner(CommandRegistry cr) {
//
//		//This command will keep being SimpleCommandCompat.
//		cr.register("owner", new SimpleCommandCompat(Category.SELF) {
//			@Override
//			public void call(MessageReceivedEvent event, String content, String[] args) {
//				if (args.length < 1) {
//					onHelp(event);
//					return;
//				}
//
//				String option = args[0];
//
//				if (args.length < 2) {
//					onHelp(event);
//					return;
//				}
//
//				String value = args[1];
//
//				String[] values = SPLIT_PATTERN.split(value, 2);
//				if (values.length < 2) {
//					onHelp(event);
//					return;
//				}
//
//				String k = values[0], v = values[1];
//
//				if (option.equals("varadd")) {
//					try {
//						String v1 = values[1];
//						switch (values[0]) {
//							case "pat":
//								ActionCmds.PATS.get().add(v1);
//								ActionCmds.PATS.save();
//								event.getChannel().sendMessage(EmoteReference.CORRECT + "Added to pat list: " + v).queue();
//								break;
//							case "hug":
//								ActionCmds.HUGS.get().add(v1);
//								ActionCmds.HUGS.save();
//								event.getChannel().sendMessage(EmoteReference.CORRECT + "Added to hug list: " + v).queue();
//								break;
//							case "greeting":
//								ActionCmds.GREETINGS.get().add(content.replace("varadd greeting ", ""));
//								ActionCmds.GREETINGS.save();
//								event.getChannel().sendMessage(EmoteReference.CORRECT + "Added to greet list: " + content.replace("greeting ", "")).queue();
//								break;
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//					return;
//				}
//
//				onHelp(event);
//			}
//
//			@Override
//			public String[] splitArgs(String content) {
//				return SPLIT_PATTERN.split(content, 2);
//			}
//
//			@Override
//			public MessageEmbed help(MessageReceivedEvent event) {
//				return helpEmbed(event, "Owner command")
//					.setDescription(prefix() + "owner shutdown/forceshutdown: Shutdowns the bot\n" +
//						prefix() + "owner restart/forcerestart: Restarts the bot.\n" +
//						prefix() + "owner varadd <pat/hug/greeting/splash>: Adds a link or phrase to the specified list.\n" +
//						prefix() + "owner eval <bsh/js/groovy> <line of code>: Evals a specified code snippet.\n")
//					.addField("Shush.", "If you aren't Adrian or Kode you shouldn't be looking at this, huh " + EmoteReference.EYES, false)
//					.build();
//			}
//
//		});
//	}
//
//	private static void prepareShutdown(MessageReceivedEvent event) {
//
//	}
//}
