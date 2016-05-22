package com.bluetrainsoftware.common.config;

import org.springframework.context.annotation.ImportResource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@ImportResource("classpath:/META-INF/bluetrain/sticky-components.xml")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableStickyConfiguration {
}
