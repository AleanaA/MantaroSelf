package net.kodehawa.mantaroself;

import br.com.brjdevs.java.utils.extensions.Async;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.kodehawa.mantaroself.core.CommandProcessor;
import net.kodehawa.mantaroself.core.LoadState;
import net.kodehawa.mantaroself.core.MantaroEventManager;
import net.kodehawa.mantaroself.core.listeners.command.CommandListener;
import net.kodehawa.mantaroself.core.listeners.operations.InteractiveOperations;
import net.kodehawa.mantaroself.data.Config;
import net.kodehawa.mantaroself.data.MantaroData;
import net.kodehawa.mantaroself.log.SimpleLogToSLF4JAdapter;
import net.kodehawa.mantaroself.modules.Event;
import net.kodehawa.mantaroself.modules.Module;
import net.kodehawa.mantaroself.modules.events.EventDispatcher;
import net.kodehawa.mantaroself.modules.events.PostLoadEvent;
import net.kodehawa.mantaroself.utils.CompactPrintStream;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Future;

import static net.kodehawa.mantaroself.MantaroInfo.VERSION;
import static net.kodehawa.mantaroself.core.LoadState.*;
import static net.kodehawa.mantaroself.data.MantaroData.config;

@Slf4j
@Accessors(fluent = true)
public class MantaroSelf implements JDA {

	@Getter
	public static final boolean debug = System.getProperty("mantaro.debug", null) != null;
	@Getter
	private static MantaroSelf instance;
	@Getter
	private static LoadState status = PRELOAD;

	public static void main(String[] args) {
		Thread.currentThread().setName("Main");

		if (System.getProperty("mantaro.verbose", null) != null) {
			System.setOut(new CompactPrintStream(System.out));
			System.setErr(new CompactPrintStream(System.err));
		}

		try {
			instance = new MantaroSelf();
		} catch (Exception e) {
			log.error("Error on Initialization: ", e);
			log.error("Exiting...");
			System.exit(-1);
		}
	}

	public static String prefix() {
		return config().get().prefix();
	}

	@Getter
	@Delegate //I love Lombok so much
	private final JDA jda;

	private MantaroSelf() throws Exception {
		SimpleLogToSLF4JAdapter.install();
		log.info("Starting a Mantaro-based Selfbot...");

		Config config = MantaroData.config().get().ensureNonNull();

		Future<Set<Class<?>>> classes = Async.future("Classes Lookup", () ->
			new Reflections("net.kodehawa.mantaroself.commands").getTypesAnnotatedWith(Module.class)
		);

		status = LOADING;

		jda = new JDABuilder(AccountType.CLIENT)
			.setAudioEnabled(false)
			.setAutoReconnect(true)
			.setToken(config.token())
			.setEventManager(new MantaroEventManager())
			.addEventListener(new CommandListener(), InteractiveOperations.listener())
			.setCorePoolSize(5)
			.buildBlocking();

		status = LOADED;
		log.info("[-=-=-=-=-=- SELFBOT STARTED -=-=-=-=-=-]");
		log.info("Started selfbot v" + VERSION + " on JDA " + JDAInfo.VERSION + " for user " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());

		MantaroData.config().save();
		MantaroData.data().save();

		Set<Method> events = new Reflections(classes.get()).getMethodsAnnotatedWith(Event.class);

		EventDispatcher.dispatch(events, CommandProcessor.REGISTRY);

		status = POSTLOAD;
		log.info("Finished loading basic components. Doing final loading...");

		EventDispatcher.dispatch(events, new PostLoadEvent());

		log.info("Loaded " + CommandProcessor.REGISTRY.commands().size() + " commands.");

		//Free Instances
		EventDispatcher.instances.clear();
	}
}
