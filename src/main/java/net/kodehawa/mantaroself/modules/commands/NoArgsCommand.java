package net.kodehawa.mantaroself.modules.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import static net.kodehawa.mantaroself.commands.info.CommandStatsManager.log;
import static net.kodehawa.mantaroself.utils.DiscordUtils.name;
import static net.kodehawa.mantaroself.utils.Utils.optional;

public abstract class NoArgsCommand implements Command {
	private final Category category;

	public NoArgsCommand(Category category) {
		this.category = category;
	}

	protected abstract void call(MessageReceivedEvent event, String content);

	/**
	 * The Command's {@link Category}
	 *
	 * @return a Nullable {@link Category}
	 */
	@Override
	public Category category() {
		return category;
	}

	@Override
	public void run(MessageReceivedEvent event, String commandName, String content) {
		call(event, content);
		log(commandName);
	}

	@Override
	public boolean hidden() {
		return false;
	}

	protected EmbedBuilder baseEmbed(MessageReceivedEvent event, String name) {
		return baseEmbed(event, name, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
	}

	protected EmbedBuilder baseEmbed(MessageReceivedEvent event, String name, String image) {
		return new EmbedBuilder()
			.setAuthor(name, null, image)
			.setColor(optional(event.getMember()).map(Member::getColor).orElse(null))
			.setFooter("Requested by " + name(event), event.getAuthor().getEffectiveAvatarUrl());
	}

	protected void doTimes(int times, Runnable runnable) {
		for (int i = 0; i < times; i++) {
			runnable.run();
		}
	}

	protected EmbedBuilder helpEmbed(MessageReceivedEvent event, String name) {
		return baseEmbed(event, name);
	}

	protected void onHelp(MessageReceivedEvent event) {
		event.getChannel().sendMessage(help(event)).queue();
	}
}
