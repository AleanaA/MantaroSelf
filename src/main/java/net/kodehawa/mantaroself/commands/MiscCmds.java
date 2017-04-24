package net.kodehawa.mantaroself.commands;

import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.Event;
import net.kodehawa.mantaroself.modules.Module;
import net.kodehawa.mantaroself.modules.commands.Commands;
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
		cr.register("8ball", Commands.newSimple(Category.MISC)

			.onCall((thiz, event, content, args) -> {
				if (content.isEmpty()) {
					thiz.onHelp(event);
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
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "8ball")
				.setDescription("Retrieves an answer from the magic 8Ball.\n"
					+ prefix() + "8ball <question>. Retrieves an answer from 8ball based on the question or sentence provided.")
				.build())
			.build());
	}

	@Event
	public static void misc(CommandRegistry cr) {
		cr.register("misc", Commands.newSimple(Category.MISC)
			.onCall((thiz, event, content, args) -> {
				MessageChannel channel = event.getChannel();
				String noArgs = content.split(" ")[0];
			})
			.help((thiz, event) -> thiz.helpEmbed(event, "Misc Commands")
				.setDescription("Miscellaneous funny/useful commands.\n"
					+ "Usage:\n"
					+ prefix() + "misc reverse <sentence>: Reverses any given sentence.\n"
					+ prefix() + "misc rndcolor: Gives you a random hex color.\n"
					+ "Parameter explanation:\n"
					+ "sentence: A sentence to reverse."
					+ "@user: A user to mention.")
				.build())
			.build());
	}

	@Event
	public static void randomFact(CommandRegistry cr) {
		cr.register("randomfact", Commands.newSimple(Category.MISC)
			.onCall((thiz, event, content, args) -> event.getChannel().sendMessage(EmoteReference.TALKING + facts.get().get(new Random().nextInt(facts.get().size() - 1))).queue())
			.help((thiz, event) -> thiz.helpEmbed(event, "Random Fact")
				.setDescription("Sends a random fact.")
				.build())
			.build());
	}
}
