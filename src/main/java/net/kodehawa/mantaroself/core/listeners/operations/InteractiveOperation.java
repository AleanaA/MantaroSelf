package net.kodehawa.mantaroself.core.listeners.operations;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public interface InteractiveOperation {
	boolean run(GuildMessageReceivedEvent event);

	default void onExpire() {
	}
}
