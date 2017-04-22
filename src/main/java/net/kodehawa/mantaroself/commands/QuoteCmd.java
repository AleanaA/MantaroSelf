package net.kodehawa.mantaroself.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.data.Data.Quote;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.RegisterCommand;
import net.kodehawa.mantaroself.modules.commands.Category;
import net.kodehawa.mantaroself.modules.commands.SimpleCommandCompat;

import java.util.List;

import static br.com.brjdevs.java.utils.extensions.CollectionUtils.random;
import static net.kodehawa.mantaroself.data.MantaroData.data;
import static net.kodehawa.mantaroself.utils.StringUtils.SPLIT_PATTERN;

@Slf4j
@RegisterCommand.Class
public class QuoteCmd {
	//@RegisterCommand
	public static void quote(CommandRegistry registry) {

		registry.register("quote", new SimpleCommandCompat(Category.UTILS) {
			@Override
			public void call(MessageReceivedEvent event, String content, String[] args) {
				if (args.length < 1) {
					onHelp(event);
					return;
				}

				String action = args[0];

				if (action.equals("random")) {
					List<Quote> quotes = data().get().quotes;

					if (quotes.isEmpty()) {
						//TODO ERROR
						return;
					}

					event.getChannel().sendMessage(random(quotes).embed(event)).queue();
				}

				if (action.equals("clear")) {
					List<Quote> quotes = data().get().quotes;

					if (quotes.isEmpty()) {
						//TODO ERROR
						return;
					}

					quotes.clear();
					data().save();

					event.getChannel().sendMessage(random(quotes).embed(event)).queue();
				}

				if (args.length < 2) {
					onHelp(event);
					return;
				}

				String value = args[1];

				/* TODO:
					- save
					- of
					- remove
					- search
				*/
			}

			@Override
			public String[] splitArgs(String content) {
				return SPLIT_PATTERN.split(content, 2);
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return null;
			}
		});
	}
}
