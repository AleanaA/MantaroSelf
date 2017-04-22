package net.kodehawa.mantaroself.core.listeners.operations;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface InteractiveOperation {
	boolean run(MessageReceivedEvent event);

	default void onExpire() {
	}
}
