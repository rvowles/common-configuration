package com.bluetrainsoftware.common.config;

import javax.inject.Inject;

import net.stickycode.configured.ConfigurationSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ConfigurationRefresher
	implements ApplicationListener<ContextRefreshedEvent> {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	public ConfigurationSystem system;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("Configure system on event {}", event);
		system.start();
	}

}
