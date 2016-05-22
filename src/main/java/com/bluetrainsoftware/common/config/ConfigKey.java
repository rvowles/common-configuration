package com.bluetrainsoftware.common.config;

/**
 * new Configured annotation that provides key override for configuration
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.ANNOTATION_TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Documented
public @interface ConfigKey {
	/**
	 * If provided this is the key we look up in the configuration source
	 *
	 * @return key used for configuration source
	 */
	String value() default "";
}
