package com.bluetrainsoftware.common.config;

import net.stickycode.bootstrap.StickySystemStartup;
import net.stickycode.bootstrap.spring4.SpringComponentContainer;
import net.stickycode.coercion.Coercions;
import net.stickycode.configuration.StickyConfiguration;
import net.stickycode.configured.ConfigurationSystem;
import net.stickycode.configured.ConfiguredBeanProcessor;
import net.stickycode.configured.ConfiguredConfigurationListener;
import net.stickycode.configured.ConfiguredMetadata;
import net.stickycode.configured.InlineConfigurationRepository;
import net.stickycode.configured.spring3.finder.SpringBeanFinder;
import net.stickycode.configured.spring4.ConfiguredBeanPostProcessor;
import net.stickycode.configured.spring4.SpringStrategyFinder;
import net.stickycode.metadata.ReflectiveMetadataResolverRegistry;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ConfiguredBeanPostProcessor.class, SpringStrategyFinder.class, SpringBeanFinder.class,
  ConfiguredBeanProcessor.class, ConfiguredMetadata.class, InlineConfigurationRepository.class, Coercions.class,
  ReflectiveMetadataResolverRegistry.class, StickySystemStartup.class, StickyConfiguration.class,
  SpringComponentContainer.class, MapCoercion.class, ConfiguredConfigurationListener.class, ConfigurationSystem.class,
  SystemPropertiesConfigurationSource.class, ConfigKeyPostProcessor.class, ConfigurationRefresher.class,
  PreStartRefresher.class})
public @interface EnableStickyConfiguration {
}
