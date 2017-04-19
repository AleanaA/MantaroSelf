package net.kodehawa.mantaroself.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolHelper {
	private static final ThreadPoolHelper defaultPool = new ThreadPoolHelper();

	public static ThreadPoolHelper defaultPool() {
		return defaultPool;
	}

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 20,
		60L, TimeUnit.SECONDS,
		new LinkedBlockingQueue<>());
	/*
		private final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
		60L, TimeUnit.SECONDS,
		new SynchronousQueue<Runnable>());
	 */

	public ThreadPoolExecutor getThreadPool() {
		return executor;
	}

	public void purge() {
		executor.purge();
	}

	public void startThread(String task, Runnable thread) {
		executor.execute(thread);
	}

}
