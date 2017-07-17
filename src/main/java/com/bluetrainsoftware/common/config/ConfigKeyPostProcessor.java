package com.bluetrainsoftware.common.config;

import net.stickycode.configured.ConfigurationRepository;
import net.stickycode.configured.ForMethodOnlyBeansDummyAttribute;
import net.stickycode.metadata.MetadataResolverRegistry;
import net.stickycode.reflector.AnnotatedMethodProcessor;
import net.stickycode.reflector.Reflector;
import net.stickycode.stereotype.configured.PostConfigured;
import net.stickycode.stereotype.configured.PreConfigured;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import javax.inject.Inject;
import java.lang.reflect.Method;

public class ConfigKeyPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {
	@Inject
	MetadataResolverRegistry metdataResolverRegistry;

	@Inject
	private ConfigurationRepository configurationRepository;

	@Inject
  protected PreStartRepository preStartRepository;


	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		if (typeIsConfigured(bean.getClass())) {
			// fake a single attribute
			configurationRepository.register(new ForMethodOnlyBeansDummyAttribute(bean));

			new Reflector()
				.forEachField(new ConfigKeyProcessor(configurationRepository))
				.process(bean);

			// find any PreStart methods
			new Reflector()
        .forEachMethod(new AnnotatedMethodProcessor(PreStart.class) {
          @Override
          public void processMethod(Object target, Method method) {
            preStartRepository.registerPreStart(target, method);
          }
        }).process(bean);
		}
		return true;
	}


	private boolean typeIsConfigured(Class<?> type) {
		//noinspection unchecked
		if (metdataResolverRegistry
			.does(type)
			.haveAnyFieldsMetaAnnotatedWith(ConfigKey.class))
			return true;

		//noinspection unchecked
		if (metdataResolverRegistry
			.does(type)
			.haveAnyMethodsMetaAnnotatedWith(PreConfigured.class, PostConfigured.class, PreStart.class))
			return true;

		return false;
	}
}
