//package net.kodehawa.mantaroself.commands;
//
//import br.com.brjdevs.java.utils.extensions.CollectionUtils;
//import lombok.extern.slf4j.Slf4j;
//import net.dv8tion.jda.core.EmbedBuilder;
//import net.dv8tion.jda.core.entities.Guild;
//import net.dv8tion.jda.core.entities.Message;
//import net.dv8tion.jda.core.entities.MessageEmbed;
//import net.dv8tion.jda.core.entities.TextChannel;
//import net.kodehawa.mantaroself.data.MantaroData;
//import net.kodehawa.mantaroself.data.db.ManagedDatabase;
//import net.kodehawa.mantaroself.data.entities.Quote;
//import net.kodehawa.mantaroself.modules.CommandRegistry;
//import net.kodehawa.mantaroself.modules.Commands;
//import net.kodehawa.mantaroself.modules.RegisterCommand;
//import net.kodehawa.mantaroself.modules.commands.Category;
//import net.kodehawa.mantaroself.modules.commands.CommandPermission;
//import net.kodehawa.mantaroself.utils.commands.EmoteReference;
//
//import java.awt.Color;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//@Slf4j
//@RegisterCommand.Class
//public class OldQuoteCmd {
//	private static MessageEmbed buildQuoteEmbed(SimpleDateFormat dateFormat, EmbedBuilder builder, Quote quote) {
//		builder.setAuthor(quote.getUserName() + " said: ", null, quote.getUserAvatar())
//			.setDescription("Quote made in server " + quote.getGuildName() + " in channel #" + quote.getChannelName())
//			.addField("Content", quote.getContent(), false)
//			.setThumbnail(quote.getUserAvatar())
//			.setFooter("Date: " + dateFormat.format(new Date(System.currentTimeMillis())), null);
//		return builder.build();
//	}
//
//	@RegisterCommand
//	public static void quote(CommandRegistry cr) {
//		cr.register("quote", Commands.newSimple(Category.MISC)
//			.code((thiz, event, content, args) -> {
//				if (content.isEmpty()) {
//					thiz.onHelp(event);
//					return;
//				}
//
//				String action = args[0];
//				String phrase = content.replace(action + " ", "");
//				Guild guild = event.getGuild();
//				ManagedDatabase db = MantaroData.db();
//				EmbedBuilder builder = new EmbedBuilder();
//				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//				List<Message> messageHistory;
//				try {
//					messageHistory = event.getChannel().getHistory().retrievePast(100).complete();
//				} catch (Exception e) {
//					event.getChannel().sendMessage(EmoteReference.ERROR + "It seems like discord is on fire, as my" +
//						" " +
//						"request to retrieve message history was denied" +
//						"with the error `" + e.getClass().getSimpleName() + "`").queue();
//					log.warn("Shit exploded on Discord's backend.", e);
//					return;
//				}
//
//				if (action.equals("addfrom")) {
//					Message message = messageHistory.stream().filter(
//						msg -> msg.getContent().toLowerCase().contains(phrase.toLowerCase())
//							&& !msg.getContent().startsWith(
//							db.getGuild(guild).getData().getGuildCustomPrefix() == null ? MantaroData.config().get().getPrefix()
//								: db.getGuild(guild).getData().getGuildCustomPrefix())
//							&& !msg.getContent().startsWith(MantaroData.config().get().getPrefix())
//					).findFirst().orElse(null);
//
//					if (message == null) {
//						event.getChannel().sendMessage(EmoteReference.ERROR + "I couldn't find a message matching the specified search" +
//							" criteria. Please try again with a more specific query.").queue();
//						return;
//					}
//
//					TextChannel channel = guild.getTextChannelById(message.getChannel().getId());
//					Quote quote = Quote.of(guild.getMember(message.getAuthor()), channel, message);
//					db.getQuotes(guild).add(quote);
//					event.getChannel().sendMessage(buildQuoteEmbed(dateFormat, builder, quote)).queue();
//					quote.saveAsync();
//					return;
//				}
//
//				if (action.equals("random")) {
//					try {
//						Quote quote = CollectionUtils.random(db.getQuotes(event.getGuild()));
//						event.getChannel().sendMessage(buildQuoteEmbed(dateFormat, builder, quote)).queue();
//					} catch (Exception e) {
//						event.getChannel().sendMessage(EmoteReference.ERROR + "This server has no set quotes!").queue();
//					}
//					return;
//				}
//
//				if (action.equals("readfrom")) {
//					try {
//						List<Quote> quotes = db.getQuotes(guild);
//						for (int i2 = 0; i2 < quotes.size(); i2++) {
//							if (quotes.get(i2).getContent().contains(phrase)) {
//								Quote quote = quotes.get(i2);
//								event.getChannel().sendMessage(buildQuoteEmbed(dateFormat, builder, quote)).queue();
//								break;
//							}
//						}
//					} catch (Exception e) {
//						event.getChannel().sendMessage(EmoteReference.ERROR + "I didn't find any quotes! (no quotes match the criteria).").queue();
//					}
//					return;
//				}
//
//				if (action.equals("removefrom")) {
//					try {
//						List<Quote> quotes = db.getQuotes(guild);
//						for (int i2 = 0; i2 < quotes.size(); i2++) {
//							if (quotes.get(i2).getContent().contains(phrase)) {
//								Quote quote = quotes.get(i2);
//								db.getQuotes(guild).remove(i2);
//								quote.saveAsync();
//								event.getChannel().sendMessage(EmoteReference.CORRECT + "Removed quote with content: " + quote.getContent())
//									.queue();
//								break;
//							}
//						}
//					} catch (Exception e) {
//						event.getChannel().sendMessage(EmoteReference.ERROR + "No quotes match the criteria.").queue();
//					}
//				}
//			})
//			.help((thiz, event) -> thiz.helpEmbed(event, "Quote command")
//				.setDescription("> Usage:\n"
//					+ prefix() + "quote addfrom <phrase>: Add a quote with the content defined by the specified number. For example, providing 1 will quote " +
//					"the last message.\n"
//					+ prefix() + "quote removefrom <phrase>: Remove a quote based on your text query.\n"
//					+ prefix() + "quote readfrom <phrase>: Search for the first quote which matches your search criteria and prints " +
//					"it.\n"
//					+ prefix() + "quote random: Get a random quote. \n"
//					+ "> Parameters:\n"
//					+ "number: Message number to quote. For example, 1 will quote the last message.\n"
//					+ "phrase: A part of the quote phrase.")
//				.setColor(Color.DARK_GRAY)
//				.build())
//			.build());
//	}
//}