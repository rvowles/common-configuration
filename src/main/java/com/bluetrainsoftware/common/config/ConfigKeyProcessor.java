package com.bluetrainsoftware.common.config;

import net.stickycode.bootstrap.ComponentContainer;
import net.stickycode.coercion.Coercion;
import net.stickycode.coercion.CoercionFinder;
import net.stickycode.coercion.CoercionTarget;
import net.stickycode.coercion.target.CoercionTargets;
import net.stickycode.configuration.ResolvedConfiguration;
import net.stickycode.configured.ConfigurationAttribute;
import net.stickycode.configured.ConfigurationRepository;
import net.stickycode.configured.ConfiguredFieldsMustNotBePrimitiveAsDefaultDerivationIsImpossibleException;
import net.stickycode.configured.MissingConfigurationException;
import net.stickycode.reflector.AnnotatedFieldProcessor;
import net.stickycode.reflector.Fields;
import net.stickycode.stereotype.Configured;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigKeyProcessor extends AnnotatedFieldProcessor {

	private final ConfigurationRepository configuration;

	public ConfigKeyProcessor(ConfigurationRepository configuration) {
		super(ConfigKey.class);

		this.configuration = configuration;
	}

	@Override
	public void processField(final Object target, final Field field) {
		if (field.getType().isPrimitive())
			throw new ConfiguredFieldsMustNotBePrimitiveAsDefaultDerivationIsImpossibleException(target, field);

		configuration.register(new ConfigurationAttribute() {
			private CoercionTarget coercionTarget = CoercionTargets.find(field);
			private ResolvedConfiguration resolution;
			private Object value;

			private Coercion<Object> coercion;
			private Object defaultValue = Fields.get(target, field);

			@Override
			public void applyCoercion(CoercionFinder coercions) {
				this.coercion = coercions.find(coercionTarget);
				this.value = resolveValue();
			}

			private Object resolveValue() {
				if (resolution.hasValue())
					return this.coercion.coerce(coercionTarget, resolution.getValue());

				if (coercion.hasDefaultValue())
					return coercion.getDefaultValue(coercionTarget);

				// no useful values so its still null
				return null;
			}

			@Override
			public void update() {
				if (value != null)
					Fields.set(target, field, value);

				else if (defaultValue == null)
					throw new MissingConfigurationException(this, resolution);
			}

			@Override
			public void invertControl(ComponentContainer container) {
				if (value != null)
					container.inject(value);
			}

			@Override
			public boolean requiresResolution() {
        return true;
				// this change to a weird, inefficient request seems necessary otherwise
				// it won't return the right value!
				//boolean value = (resolution == null);

				//return value;
			}

			@Override
			public ResolvedConfiguration getResolution() {
				return resolution;
			}

			@Override
			public String toString() {
				return join(".").stream().collect(Collectors.joining(","));
			}

			@Override
			public Object getTarget() {
				return target;
			}

			@Override
			public void resolvedWith(ResolvedConfiguration resolved) {
				this.resolution = resolved;
			}

			@Override
			public CoercionTarget getCoercionTarget() {
				return coercionTarget;
			}

			@Override
			public List<String> join(String delimeter) {
				ConfigKey key = field.getAnnotation(ConfigKey.class);

				if (key != null) {
					if (key.value() != null && key.value().length() > 0) {
						return Collections.singletonList(key.value());
					} else if (target != null) {
						return Collections.singletonList(getSimpleName(delimeter));
					}
				}

				Configured configured = field.getAnnotation(Configured.class);
				if (configured != null)
					return Collections.singletonList(getSimpleName(delimeter));

				return new ArrayList<>();
			}

			private String getSimpleName(String delimeter) {
				return target.getClass().getSimpleName().substring(0, 1).toLowerCase() + target.getClass().getSimpleName().substring(1) + delimeter + field.getName();
			}
		});
	}
}
