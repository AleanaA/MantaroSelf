package net.kodehawa.mantaroself.modules.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import static net.kodehawa.mantaroself.commands.info.CommandStatsManager.log;

public abstract class NoArgsCommand implements Command {
	private final Category category;

	public NoArgsCommand(Category category) {
		this.category = category;
	}

	protected abstract void call(GuildMessageReceivedEvent event, String content);

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
	public void run(GuildMessageReceivedEvent event, String commandName, String content) {
		call(event, content);
		log(commandName);
	}

	@Override
	public boolean hidden() {
		return false;
	}

	protected EmbedBuilder baseEmbed(GuildMessageReceivedEvent event, String name) {
		return baseEmbed(event, name, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
	}

	protected EmbedBuilder baseEmbed(GuildMessageReceivedEvent event, String name, String image) {
		return new EmbedBuilder()
			.setAuthor(name, null, image)
			.setColor(event.getMember().getColor())
			.setFooter("Requested by " + event.getMember().getEffectiveName(), event.getAuthor().getEffectiveAvatarUrl());
	}

	protected void doTimes(int times, Runnable runnable) {
		for (int i = 0; i < times; i++) {
			runnable.run();
		}
	}

	protected EmbedBuilder helpEmbed(GuildMessageReceivedEvent event, String name) {
		return baseEmbed(event, name);
	}

	protected void onHelp(GuildMessageReceivedEvent event) {
		event.getChannel().sendMessage(help(event)).queue();
	}
}
