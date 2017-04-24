package net.kodehawa.mantaroself.core.listeners.command;

import br.com.brjdevs.java.utils.extensions.Async;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.kodehawa.mantaroself.core.CommandProcessor;
import net.kodehawa.mantaroself.utils.Utils;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CommandListener implements EventListener {
	private static final CommandProcessor DEFAULT = new CommandProcessor();
	//Message cache of 2500 messages. If it reaches 2500 it will delete the first one stored, and continue being 2500
	@Getter
	private static final Cache<String, Optional<Message>> MESSAGE_CACHE = CacheBuilder.newBuilder().concurrencyLevel(10).maximumSize(2500).build();
	private static final Map<String, CommandProcessor> PROCESSORS = new ConcurrentHashMap<>();
	private static int commandTotal = 0;

	public static void clearCustomProcessor(String channelId) {
		PROCESSORS.remove(channelId);
	}

	public static String getCommandTotal() {
		return String.valueOf(commandTotal);
	}

	public static void setCustomProcessor(String channelId, CommandProcessor processor) {
		PROCESSORS.put(channelId, processor);
	}

	private final Random random = new Random();

	@Override
	public void onEvent(Event event) {
		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent msg = (MessageReceivedEvent) event;
			MESSAGE_CACHE.put(msg.getMessage().getId(), Optional.of(msg.getMessage()));

			if (!msg.getJDA().getSelfUser().equals(msg.getAuthor())) return;

			Async.thread("Cmd:" + msg.getMessage().getRawContent(), () -> onCommand(msg));
		}
	}

	private void onCommand(MessageReceivedEvent event) {
		try {
			if (PROCESSORS.getOrDefault(event.getChannel().getId(), DEFAULT).run(event)) commandTotal++;
		} catch (Exception e) {
			String post = null;
			try {
				post = Utils.paste(Throwables.getStackTraceAsString(e));
			} catch (Exception ignored) {}

			event.getChannel().sendMessage(EmoteReference.ERROR + "Error happened. Check logs." + (post == null ? "" : " (Pasted: " + post + ")")).queue();
			log.warn("Error on Command ``" + event.getMessage().getRawContent() + "``: ", e);
		}
	}
}
