package net.kodehawa.mantaroself.core;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.MantaroSelf;
import net.kodehawa.mantaroself.data.MantaroData;
import net.kodehawa.mantaroself.modules.CommandRegistry;

import static net.kodehawa.mantaroself.utils.StringUtils.splitArgs;

@Slf4j
public class CommandProcessor {
	public static final CommandRegistry REGISTRY = new CommandRegistry();

	public boolean run(MessageReceivedEvent event) {
		if (MantaroSelf.status() != LoadState.POSTLOAD) return false;

		String rawCmd = event.getMessage().getRawContent();
		String prefix = MantaroData.config().get().prefix();
		if (rawCmd.startsWith(prefix)) rawCmd = rawCmd.substring(prefix.length());
		else return false;

		String[] parts = splitArgs(rawCmd, 2);
		String cmdName = parts[0], content = parts[1];

		if (REGISTRY.process(event, cmdName, content)) {
			event.getMessage().delete().queue();
		}
		return true;
	}
}