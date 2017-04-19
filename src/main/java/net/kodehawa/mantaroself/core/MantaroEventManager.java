package net.kodehawa.mantaroself.core;

import br.com.brjdevs.highhacks.eventbus.ASMEventBus;
import br.com.brjdevs.java.utils.extensions.Async;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MantaroEventManager implements IEventManager {
	public static Logger getLog() {
		return log;
	}

	private final ASMEventBus eventBus = new ASMEventBus(MantaroEventManager.class.getClassLoader(), true);
	private final ExecutorService executor;
	private final List<Object> listeners = new CopyOnWriteArrayList<>();
	public long LAST_EVENT;

	public MantaroEventManager(ExecutorService executor) {
		this.executor = Preconditions.checkNotNull(executor);
	}

	public MantaroEventManager() {
		AtomicInteger number = new AtomicInteger();
		executor = Executors.newCachedThreadPool(r -> {
			Thread t = new Thread(r,
				MoreObjects.toStringHelper("EventManagerThread")
					.add("thread", number.incrementAndGet())
					.toString());
			t.setDaemon(true);
			t.setPriority(Thread.MIN_PRIORITY);
			return t;
		});
	}

	@Override
	public void register(Object listener) {
		eventBus.register(listener);
		listeners.add(listener);
	}

	@Override
	public void unregister(Object listener) {
		eventBus.unregister(listener);
		listeners.remove(listener);
	}

	@Override
	public void handle(Event event) {
		Async.thread("Async EventHandling", () -> handleSync(event));
	}

	@Override
	public List<Object> getRegisteredListeners() {
		return Collections.unmodifiableList(listeners);
	}

	public void clearListeners() {
		for (Object o : getRegisteredListeners())
			unregister(o);
	}

	public void handleSync(Event event) {
		LAST_EVENT = System.currentTimeMillis();
		eventBus.post(event);
	}
}
