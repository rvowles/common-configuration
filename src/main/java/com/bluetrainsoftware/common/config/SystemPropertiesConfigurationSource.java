package com.bluetrainsoftware.common.config;


import net.stickycode.configuration.ConfigurationSource;
import net.stickycode.configuration.ConfigurationValue;
import net.stickycode.configuration.ResolvedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class SystemPropertiesConfigurationSource implements ConfigurationSource {
	private static final Logger log = LoggerFactory.getLogger(SystemPropertiesConfigurationSource.class);

  public SystemPropertiesConfigurationSource() {
    log.info("Sticky Configuration is ready to read from System Properties.");
  }

  @Override
	public void apply(net.stickycode.configuration.ConfigurationKey configurationKey, ResolvedConfiguration resolvedConfiguration) {
		final String key = configurationKey.join(".").get(0);

		final String value = System.getProperty(key);

		if (value != null) {
			resolvedConfiguration.add(new ConfigurationValue() {
				@Override
				public String get() {
					String newValue = System.getProperty(key);

          return newValue == null ? value : newValue;
				}

				@Override
				public boolean hasPrecedence(ConfigurationValue v) {
					return false;
				}
			});
		}
	}
}
