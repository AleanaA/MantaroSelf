package net.kodehawa.mantaroself.commands;

import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.Event;
import net.kodehawa.mantaroself.modules.Module;
import net.kodehawa.mantaroself.modules.commands.SimpleCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;
import net.kodehawa.mantaroself.utils.data.DataManager;
import net.kodehawa.mantaroself.utils.data.SimpleFileDataManager;

import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

import static net.kodehawa.mantaroself.MantaroSelf.prefix;

@Module
@Slf4j
public class MiscCmds {
	public static final DataManager<List<String>> facts = new SimpleFileDataManager("assets/mantaroself/texts/facts.txt");

	@Event
	public static void eightBall(CommandRegistry cr) {
		cr.register("8ball", new SimpleCommand(Category.MISC) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				if (content.isEmpty()) {
					onHelp(event);
					return;
				}

				String textEncoded;
				String answer;
				try {
					textEncoded = URLEncoder.encode(content, "UTF-8");
					answer = Unirest.get(String.format("https://8ball.delegator.com/magic/JSON/%1s", textEncoded))
						.asJson()
						.getBody()
						.getObject()
						.getJSONObject("magic")
						.getString("answer");
				} catch (Exception exception) {
					event.getChannel().sendMessage(EmoteReference.ERROR + "I ran into an error while fetching 8ball results. My owners " +
						"have been notified and will resolve this soon.")
						.queue();
					log.warn("Error while processing answer", exception);
					return;
				}

				event.getChannel().sendMessage("\uD83D\uDCAC " + answer + ".").queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "8ball")
					.setDescription("Retrieves an answer from the magic 8Ball.\n"
						+ prefix() + "8ball <question>. Retrieves an answer from 8ball based on the question or sentence provided.")
					.build();
			}
		});
	}

	@Event
	public static void randomFact(CommandRegistry cr) {
		cr.register("randomfact", new SimpleCommand(Category.MISC) {
			@Override
			protected void call(MessageReceivedEvent event, String content, String[] args) {
				event.getChannel().sendMessage(EmoteReference.TALKING + facts.get().get(new Random().nextInt(facts.get().size() - 1))).queue();
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Random Fact")
					.setDescription("Sends a random fact.")
					.build();
			}
		});
	}
}
