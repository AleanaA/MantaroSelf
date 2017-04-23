package net.kodehawa.mantaroself.modules.commands.builders;

import br.com.brjdevs.java.utils.functions.TriConsumer;
import com.google.common.base.Preconditions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.commands.SimpleCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.modules.commands.base.Command;
import net.kodehawa.mantaroself.utils.QuadConsumer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleCommandBuilder {
	private final Category category;
	private QuadConsumer<SimpleCommand, MessageReceivedEvent, String, String[]> code;
	private BiFunction<SimpleCommand, MessageReceivedEvent, MessageEmbed> help;
	private Function<String, String[]> splitter;

	public SimpleCommandBuilder(Category category) {
		this.category = category;
	}

	public Command build() {
		Preconditions.checkNotNull(code, "code");
		if (help == null)
			help = (t, e) -> new EmbedBuilder().setDescription("No help available for this command").build();
		return new SimpleCommand(category) {
			@Override
			public void call(MessageReceivedEvent event, String content, String[] args) {
				code.accept(this, event, content, args);
			}

			@Override
			public String[] splitArgs(String content) {
				return splitter == null ? super.splitArgs(content) : splitter.apply(content);
			}

			@Override
			public Category category() {
				return category;
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return help.apply(this, event);
			}
		};
	}

	public SimpleCommandBuilder help(BiFunction<SimpleCommand, MessageReceivedEvent, MessageEmbed> help) {
		this.help = Preconditions.checkNotNull(help, "help");
		return this;
	}

	public SimpleCommandBuilder help(Function<MessageReceivedEvent, MessageEmbed> help) {
		Preconditions.checkNotNull(help, "help");
		this.help = (thiz, event) -> help.apply(event);
		return this;
	}

	public SimpleCommandBuilder onCall(BiConsumer<MessageReceivedEvent, String[]> code) {
		Preconditions.checkNotNull(code, "code");
		this.code = (thiz, event, content, args) -> code.accept(event, args);
		return this;
	}

	public SimpleCommandBuilder onCall(Consumer<MessageReceivedEvent> code) {
		Preconditions.checkNotNull(code, "code");
		this.code = (thiz, event, content, args) -> code.accept(event);
		return this;
	}

	public SimpleCommandBuilder onCall(QuadConsumer<SimpleCommand, MessageReceivedEvent, String, String[]> code) {
		this.code = Preconditions.checkNotNull(code, "code");
		return this;
	}

	public SimpleCommandBuilder onCall(TriConsumer<MessageReceivedEvent, String, String[]> code) {
		Preconditions.checkNotNull(code, "code");
		this.code = (thiz, event, content, args) -> code.accept(event, content, args);
		return this;
	}

	public SimpleCommandBuilder splitter(Function<String, String[]> splitter) {
		this.splitter = splitter;
		return this;
	}
}
