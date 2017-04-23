package net.kodehawa.mantaroself.commands;

import br.com.brjdevs.java.utils.extensions.Async;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.kodehawa.mantaroself.modules.CommandRegistry;
import net.kodehawa.mantaroself.modules.Event;
import net.kodehawa.mantaroself.modules.Module;
import net.kodehawa.mantaroself.modules.commands.Commands;
import net.kodehawa.mantaroself.modules.commands.SimpleCommand;
import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.utils.commands.EmoteReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Module
public class FunCmds {

	@Event
	public static void coinflip(CommandRegistry cr) {
		cr.register("coinflip", Commands.newSimple(Category.FUN)

			.onCall((thiz, event, content, args) -> {
				int times;
				if (args.length == 0 || content.length() == 0) times = 1;
				else {
					try {
						times = Integer.parseInt(args[0]);
						if (times > 1000) {
							event.getChannel().sendMessage(EmoteReference.ERROR + "Whoah there! The limit is 1,000 coinflips").queue();
							return;
						}
					} catch (NumberFormatException nfe) {
						event.getChannel().sendMessage(EmoteReference.ERROR + "You need to specify an Integer for the amount of " +
							"repetitions").queue();
						return;
					}
				}

				final int[] heads = {0};
				final int[] tails = {0};
				thiz.doTimes(times, () -> {
					if (new Random().nextBoolean()) heads[0]++;
					else tails[0]++;
				});
				String flips = times == 1 ? "time" : "times";
				event.getChannel().sendMessage(EmoteReference.PENNY + " Your result from **" + times + "** " + flips + " yielded " +
					"**" + heads[0] + "** heads and **" + tails[0] + "** tails").queue();
			})
			.help((thiz, event) ->
				thiz.helpEmbed(event, "Coinflip command")
					.setDescription("Flips a coin with a defined number of repetitions")
					.build())
			.build());
	}

	@Event
	public static void dice(CommandRegistry cr) {
		cr.register("roll", Commands.newSimple(Category.FUN)

			.onCall((thiz, event, content, args) -> {
				int roll;
				try {
					roll = Integer.parseInt(args[0]);
				} catch (Exception e) {
					roll = 1;
				}
				if (roll >= 100) roll = 100;
				event.getChannel().sendMessage(EmoteReference.DICE + "You got **" + diceRoll(roll) + "** with a total of **" + roll
					+ "** repetitions.").queue();
			})
			.help((thiz, event) ->
				thiz.helpEmbed(event, "Dice command")
					.setDescription("Roll a 6-sided dice a specified number of times")
					.build()
			)
			.build()
		);
	}

	private static int diceRoll(int repetitions) { //why the fuck was this shit synchronized?
		int num = 0;
		int roll;
		for (int i = 0; i < repetitions; i++) {
			roll = new Random().nextInt(6) + 1;
			num = num + roll;
		}
		return num;
	}

	@Event
	public static void troll(CommandRegistry registry) {
		List<Thread> trollThreads = new ArrayList<>();

		registry.register("troll", new SimpleCommand(Category.FUN) {
			@Override
			public void call(MessageReceivedEvent event, String content, String[] args) {
				if (args.length < 1) {
					onHelp(event);
					return;
				}

				String action = args[0];

				if (action.equals("stop")) {
					trollThreads.forEach(Thread::interrupt);
					trollThreads.clear();
					return;
				}

				if (action.equals("typing")) {
					long _times = 1;
					if (args.length > 1) {
						try {
							String parse = args[1];
							if (parse.equals("forever")) {
								_times = Long.MAX_VALUE;
							} else if (parse.equals("once")) {
								_times = 1;
							} else if (parse.equals("twice")) {
								_times = 2;
							} else {
								_times = Long.parseLong(parse);
							}
						} catch (Exception ignored) {
						}

						long times = _times;

						trollThreads.add(Async.thread("Troll Thread (Typing@" + event.getChannel().getName() + ")", () -> {
							try {
								for (long i = 0; i < times; i++) {
									event.getChannel().sendTyping().complete();
									Thread.sleep(9000);
								}
							} catch (InterruptedException ignored) {
							}
						}));

						return;
					}
				}

				if (args.length < 2) {
					onHelp(event);
					return;
				}

				String value = args[1];

				onHelp(event);
			}

			@Override
			public MessageEmbed help(MessageReceivedEvent event) {
				return helpEmbed(event, "Troll Command").build(); //TODO WIP
			}
		});
	}
}
