package net.kodehawa.mantaroself.utils.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.MantaroSelf;
import net.kodehawa.mantaroself.utils.MapObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static br.com.brjdevs.java.utils.strings.StringUtils.splitArgs;

@Slf4j
public class Cleverbot {
	public static final Map<Predicate<String>, Consumer<MessageReceivedEvent>> OVERRIDES = new MapObject<>();

	public static void handle(MessageReceivedEvent event) {
		if (MantaroSelf.CLEVERBOT == null) return;

		String input = splitArgs(event.getMessage().getStrippedContent(), 2)[1];

		for (Entry<Predicate<String>, Consumer<MessageReceivedEvent>> override : OVERRIDES.entrySet()) {
			if (override.getKey().test(input)) {
				override.getValue().accept(event);
				return;
			}
		}

		try {

			event.getChannel().sendMessage(MantaroSelf.CLEVERBOT.getResponse(input)).queue();
		} catch (Exception e) {
			if (!(e instanceof UnsupportedOperationException)) {
				log.error("Cleverbot.io API Error: ", e);
			}
			event.getChannel().sendMessage(EmoteReference.CRYING + "I-I don't know what to say! P-please forgive me.").queue();
		}
	}
}
