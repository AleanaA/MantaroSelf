package net.kodehawa.mantaroself.core.listeners.command;

import br.com.brjdevs.highhacks.eventbus.Listener;
import br.com.brjdevs.java.utils.extensions.Async;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.kodehawa.mantaroself.core.CommandProcessor;
import net.kodehawa.mantaroself.utils.Snow64;
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
	private static final Cache<String, Optional<Message>> messageCache = CacheBuilder.newBuilder().concurrencyLevel(10).maximumSize(2500).build();
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
		if (event instanceof GuildMessageReceivedEvent) {
			GuildMessageReceivedEvent e = (GuildMessageReceivedEvent) event;
			Async.thread("CmdThread", () -> onCommand(e));
		}
	}

	private void onCommand(GuildMessageReceivedEvent event) {
		messageCache.put(event.getMessage().getId(), Optional.of(event.getMessage()));

		if (!event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
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
		} catch (IndexOutOfBoundsException e) {
			event.getChannel().sendMessage(EmoteReference.ERROR + "Your query returned no results or incorrect type arguments. Check the command help.").queue();
			log.warn("Exception catched and alternate message sent. We should look into this, anyway.", e);
		} catch (PermissionException e) {
			event.getChannel().sendMessage(EmoteReference.ERROR + "I don't have permission to do this! I need the permission: " + e.getPermission()).queue();
			log.warn("Exception catched and alternate message sent. We should look into this, anyway.", e);
		} catch (IllegalArgumentException e) { //NumberFormatException == IllegalArgumentException
			event.getChannel().sendMessage(EmoteReference.ERROR + "Incorrect type arguments. Check command help.").queue();
			log.warn("Exception catched and alternate message sent. We should look into this, anyway.", e);
		} catch (Exception e) {
			String id = Snow64.toSnow64(Long.parseLong(event.getMessage().getId()));

			event.getChannel().sendMessage(
				EmoteReference.ERROR + "I ran into an unexpected error. (Error ID: ``" + id + "``)\n" +
					"If you want, **contact ``Kodehawa#3457`` on DiscordBots** (popular bot guild), or join our **support guild** (Link on ``~>about``). Don't forget the Error ID!"
			).queue();

			log.warn("Unexpected Exception on Command ``" + event.getMessage().getRawContent() + "`` (Error ID: ``" + id + "``)", e);
		}
	}
}
