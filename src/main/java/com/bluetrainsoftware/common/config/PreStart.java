package com.bluetrainsoftware.common.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Methods annotated with this should be called after context refresh
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
@Target({METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface PreStart {
  // can we call it async?
  boolean async = false;
  // do we have to wait for the result?
  boolean wait = true;
}
