package com.bluetrainsoftware.common.config;

import java.lang.reflect.Method;

/**
 * This should be extracted from the context of the DI framework after the wiring is complete
 * (e.g. in Spring, the first refresh has finished).
 *
 * @author Richard Vowles - https://plus.google.com/+RichardVowles
 */
public interface PreStartRepository {
  /*
   * internal use for registering discovered objects and methods. You can still use this to
   * register things to be called before Start if required.
   */
  void registerPreStart(Object o, Method m);

  /*
   * start the process of pre-start
   */
  void start();
}
