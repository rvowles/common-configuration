package com.bluetrainsoftware.common.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * new Configured annotation that provides key override for configuration
 */
@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface ConfigKey {
	/**
	 * If provided this is the key we look up in the configuration source
	 *
	 * @return key used for configuration source
	 */
	String value() default "";
}
