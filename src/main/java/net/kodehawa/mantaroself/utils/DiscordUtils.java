package net.kodehawa.mantaroself.utils;

import br.com.brjdevs.java.utils.extensions.CollectionUtils;
import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.MantaroSelf;
import net.kodehawa.mantaroself.core.listeners.operations.InteractiveOperations;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.StringUtils.replaceEach;

public class DiscordUtils {
	private static Pattern userMention = Pattern.compile("<@!?\\d+>");

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

	public static Game game(@Nonnull User user, @Nullable Member member) {
		if (member != null) return member.getGame();

		List<Guild> mutualGuilds = user.getJDA().getMutualGuilds(user);
		if (!mutualGuilds.isEmpty()) return mutualGuilds.get(0).getMember(user).getGame();

		Friend friend = MantaroSelf.instance().asClient().getFriend(user);
		if (friend != null) return friend.getGame();

		return null;
	}

	public static String mention(MessageChannel channel) {
		switch (channel.getType()) {
			case TEXT:
				return ((TextChannel) channel).getAsMention();
			case PRIVATE:
				return ((PrivateChannel) channel).getUser().getAsMention() + "'s DM";
			case GROUP:
				return channel.getName();
			case VOICE:
			case UNKNOWN:
			default:
				throw new IllegalStateException("[JDA Error] What in the Name of fuck.");
		}
	}

	public static String name(MessageReceivedEvent event) {
		return name(event.getAuthor(), event.getMember());
	}

	public static String name(@Nonnull User user, @Nullable Member member) {
		return member == null ? user.getName() : member.getEffectiveName();
	}

	public static String name(MessageChannel channel) {
		switch (channel.getType()) {
			case TEXT:
				return channel.getName();
			case PRIVATE:
				return channel.getName() + "'s DM";
			case GROUP:
				return channel.getName() != null ? channel.getName() : ((Group) channel).getOwner().getName() + "'s Group";
			case VOICE:
			case UNKNOWN:
			default:
				throw new IllegalStateException("[JDA Error] What in the Name of fuck.");
		}
	}

	public static boolean selectInt(MessageReceivedEvent event, int max, IntConsumer valueConsumer) {
		return InteractiveOperations.create(event.getChannel(), "Selection", 10000, OptionalInt.empty(), (e) -> {
			if (!e.getAuthor().equals(event.getAuthor())) return false;

			try {
				int choose = Integer.parseInt(e.getMessage().getContent());
				if (choose < 1 || choose >= max) return false;
				valueConsumer.accept(choose);
				return true;
			} catch (Exception ignored) {}
			return false;
		});
	}

	public static <T> boolean selectList(MessageReceivedEvent event, List<T> list, Function<T, String> toString, Function<String, MessageEmbed> toEmbed, Consumer<T> valueConsumer) {
		Pair<String, Integer> r = embedList(list, toString);
		event.getChannel().sendMessage(toEmbed.apply(r.getLeft())).queue();
		return selectInt(event, r.getRight() + 1, i -> valueConsumer.accept(list.get(i - 1)));
	}

	public static <T> boolean selectList(MessageReceivedEvent event, T[] list, Function<T, String> toString, Function<String, MessageEmbed> toEmbed, Consumer<T> valueConsumer) {
		Pair<String, Integer> r = embedList(Arrays.asList(list), toString);
		event.getChannel().sendMessage(toEmbed.apply(r.getLeft())).queue();
		return selectInt(event, r.getRight() + 1, i -> valueConsumer.accept(list[i - 1]));
	}

	public static OnlineStatus status(@Nonnull User user, @Nullable Member member) {
		if (member != null) return member.getOnlineStatus();

		List<Guild> mutualGuilds = user.getJDA().getMutualGuilds(user);
		if (!mutualGuilds.isEmpty()) return mutualGuilds.get(0).getMember(user).getOnlineStatus();

		Friend friend = MantaroSelf.instance().asClient().getFriend(user);
		if (friend != null) return friend.getOnlineStatus();

		return OnlineStatus.UNKNOWN;
	}

	public static List<User> usersMentioned(Message message) {
		return StreamSupport.stream(CollectionUtils.iterable(userMention, message.getRawContent()).spliterator(), false)
			.map(s -> {
				try {
					return Long.decode(replaceEach(s,
						new String[]{"<@", "!", ">"},
						new String[]{"", "", ""}
					));
				} catch (Exception e) {
					return null;
				}
			}).filter(Objects::nonNull)
			.mapToLong(Long::longValue)
			.mapToObj(MantaroSelf.instance()::getUserById)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
