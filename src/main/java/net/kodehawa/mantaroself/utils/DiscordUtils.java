package net.kodehawa.mantaroself.utils;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantaroself.core.listeners.operations.InteractiveOperations;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class DiscordUtils {
	public static <T> Pair<String, Integer> embedList(List<T> list, Function<T, String> toString) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			String s = toString.apply(list.get(i));
			if (b.length() + s.length() + 5 > MessageEmbed.TEXT_MAX_LENGTH) return Pair.of(b.toString(), i);
			b.append('[').append(i + 1).append("] ");
			b.append(s);
			b.append("\n");
		}

		return Pair.of(b.toString(), list.size());
	}

	public static boolean selectInt(GuildMessageReceivedEvent event, int max, IntConsumer valueConsumer) {
		return InteractiveOperations.create(event.getChannel(), "Selection", 10000, OptionalInt.empty(), (e) -> {
			if (!e.getAuthor().equals(event.getAuthor())) return false;

			try {
				int choose = Integer.parseInt(e.getMessage().getContent());
				if (choose < 1 || choose >= max) return false;
				valueConsumer.accept(choose);
				return true;
			} catch (Exception ignored) {
			}
			return false;
		});
	}

	public static <T> boolean selectList(GuildMessageReceivedEvent event, List<T> list, Function<T, String> toString, Function<String, MessageEmbed> toEmbed, Consumer<T> valueConsumer) {
		Pair<String, Integer> r = embedList(list, toString);
		event.getChannel().sendMessage(toEmbed.apply(r.getLeft())).queue();
		return selectInt(event, r.getRight() + 1, i -> valueConsumer.accept(list.get(i - 1)));
	}

	public static <T> boolean selectList(GuildMessageReceivedEvent event, T[] list, Function<T, String> toString, Function<String, MessageEmbed> toEmbed, Consumer<T> valueConsumer) {
		Pair<String, Integer> r = embedList(Arrays.asList(list), toString);
		event.getChannel().sendMessage(toEmbed.apply(r.getLeft())).queue();
		return selectInt(event, r.getRight() + 1, i -> valueConsumer.accept(list[i - 1]));
	}
}
