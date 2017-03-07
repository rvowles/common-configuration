package com.bluetrainsoftware.common.config;

import net.stickycode.configured.Configuration;
import net.stickycode.configured.ConfigurationRepository;
import net.stickycode.configured.ConfiguredConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public class ConcurrentConfigurationListener extends ConfiguredConfigurationListener {
	protected final static Logger log = LoggerFactory.getLogger(ConcurrentConfigurationListener.class);

	@Inject
	private ConfigurationRepository localConfigurations; // private in the base class

	/**
	 * Extra careful thread factory - logs any uncaught exceptions before the thread exits.
	 * (written by crazyeddie)
	 */
	private static class Factory implements ThreadFactory {

		private final AtomicInteger count = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("stickyconfig-async-#" + count.getAndIncrement());
			thread.setUncaughtExceptionHandler((t, e) -> LoggerFactory.getLogger(t.getName()).error(e.getMessage(), e));
			log.debug("created new thread for async handling {}", thread.getName());
			return thread;
		}
	}

	private final Executor executor = Executors.newCachedThreadPool(new Factory());

	@Override
	public void postConfigure() {
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		localConfigurations.forEach(config -> {
			Runnable asyncActivity = new Runnable() {
				@Override
				public void run() {
					config.postConfigure();
				}
			};

			futures.add(CompletableFuture.runAsync(asyncActivity, executor));
		});

		CompletableFuture[] asyncs = new CompletableFuture[futures.size()];

		CompletableFuture.allOf(futures.toArray(asyncs)).join();
	}
}
