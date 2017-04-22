package net.kodehawa.mantaroself.core.listeners.command;

import br.com.brjdevs.highhacks.eventbus.Listener;
import br.com.brjdevs.java.utils.extensions.Async;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.kodehawa.mantaroself.core.CommandProcessor;
import net.kodehawa.mantaroself.utils.Utils;
import net.kodehawa.mantaroself.utils.commands.Cleverbot;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CommandListener implements EventListener {
	private static final Map<String, CommandProcessor> CUSTOM_PROCESSORS = new ConcurrentHashMap<>();
	private static final CommandProcessor DEFAULT_PROCESSOR = new CommandProcessor();

	//Message cache of 2500 messages. If it reaches 2500 it will delete the first one stored, and continue being 2500
	@Getter
	private static final Cache<String, Optional<Message>> MESSAGE_CACHE = CacheBuilder.newBuilder().concurrencyLevel(10).maximumSize(2500).build();
	private static int commandTotal = 0;

	public static void clearCustomProcessor(String channelId) {
		CUSTOM_PROCESSORS.remove(channelId);
	}

	public static String getCommandTotal() {
		return String.valueOf(commandTotal);
	}

	public static void setCustomProcessor(String channelId, CommandProcessor processor) {
		CUSTOM_PROCESSORS.put(channelId, processor);
	}

	private final Random random = new Random();

	@Listener
	@Override
	public void onEvent(Event event) {
		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent e = (MessageReceivedEvent) event;
			Async.thread("CmdThread", () -> onCommand(e));
		}
	}

	private void onCommand(MessageReceivedEvent event) {
		MESSAGE_CACHE.put(event.getMessage().getId(), Optional.of(event.getMessage()));

		if (!event.getJDA().getSelfUser().equals(event.getAuthor())) {
			return;
		}

		//Cleverbot.
		SelfUser self = event.getJDA().getSelfUser();
		if (event.getMessage().getRawContent().startsWith("<@" + self.getId() + '>') || event.getMessage().getRawContent().startsWith("<@!" + self.getId() + '>')) {
			Cleverbot.handle(event);
			return;
		}

		try {
			if (CUSTOM_PROCESSORS.getOrDefault(event.getChannel().getId(), DEFAULT_PROCESSOR).run(event))
				commandTotal++;
		} catch (Exception e) {
			String post = null;
			try {
				post = Utils.paste(Throwables.getStackTraceAsString(e));
			} catch (Exception ignored) {
			}

			event.getChannel().sendMessage(EmoteReference.ERROR + "Error happened. Check logs." + (post == null ? "" : " (Pasted: " + post + ")")).queue();
			log.warn("Error on Command ``" + event.getMessage().getRawContent() + "``: ", e);
		}
	}
}
