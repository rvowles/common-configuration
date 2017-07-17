package com.bluetrainsoftware.common.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable this if you are using a Bathe Time application with Spring, it will
 * create another instance of the BatheTimeWatcher which will share the instance
 * variables collected at start.
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({BatheTimeWatcher.class})
public @interface EnbaleBatheReload {
}
